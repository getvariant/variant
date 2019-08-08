package com.variant.server.boot

import scala.concurrent.duration._
import scala.collection.mutable
import com.variant.server.api.Configuration
import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import java.time.Instant
import java.time.{ Duration => JavaDuration }
import com.variant.core.util.TimeUtils
import akka.actor.Props

/**
 * Vacuuming actor.
 * Responsible for
 *   1) cleaning out expired sessions from the session store,
 *   2) cleaning out drained schema generations.
 *
 */
object VacuumActor {

   def props(implicit server: VariantServer): Props = Props(new VacuumActor(server))

   /**
    * The only message VacuumActor responds to
    */
   case object VacuumNow
}

class VacuumActor(server: VariantServer) extends Actor with LazyLogging {

   import VacuumActor._

   private val vacuumingFrequency = server.config.sessionVacuumInterval // Seconds

   /**
    * Explicitly schedule the first vacuuming.
    */
   override def preStart() = {
      self ! VacuumNow
   }

   /**
    * Run the vacuum once and schedule next run after externally configured delay.
    */
   override def receive() = {

      case VacuumNow =>

         val top = Instant.now
         logger.trace("Running vacuuming actor.")
         var ssnCount = 0
         try {
            val now = System.currentTimeMillis();

            // Vacuum expired sessions from the session store,
            ssnCount = server.ssnStore.vacuum()

            // Vacuum drained schema generations and dead schemata.
            server.schemata.vacuum()

         } catch {
            case t: Throwable => logger.error(s"Ignored unexpected exception [${t.getMessage}]", t);
         } finally {
            if (ssnCount > 0) logger.debug(s"Vacuumed ${ssnCount} session(s)")
            else logger.trace(s"Vacuumed 0 session(s)")

            // Schedule the next run
            implicit val context = server.actorSystem.dispatcher
            server.actorSystem.scheduler.scheduleOnce(Duration(vacuumingFrequency, SECONDS), self, VacuumNow)
         }

      case msg => logger.error(s"Ignored unknown message ${msg}")
   }
}

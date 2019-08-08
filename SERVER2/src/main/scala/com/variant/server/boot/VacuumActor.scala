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

         try {
            val now = System.currentTimeMillis();

            // Remove expired sessions from the session store,
            val deleteCount = server.ssnStore.vacuum()

            logger.trace(s"Vacuumed $deleteCount session(s)");
            if (deleteCount > 0) logger.debug(s"Vacuumed $deleteCount session(s)");

            // Remove drained schema generations and dead schemata.
            server.schemata.vacuum()

         } catch {
            case t: Throwable => logger.error(s"Ignored unexpected exception [${t.getMessage}]", t);
         } finally {
            logger.trace("Vacuuming actor completed in %s".format(TimeUtils.formatDuration(JavaDuration.between(top, Instant.now))))
            // Schedule the next run
            implicit val context = server.actorSystem.dispatcher
            server.actorSystem.scheduler.scheduleOnce(Duration(vacuumingFrequency, SECONDS), self, VacuumNow)
         }

      case msg => logger.error(s"Ignored unknown message ${msg}")
   }
}

package com.variant.server.schema

import scala.collection.mutable.ListBuffer
import com.variant.server.api.ServerException
import com.variant.server.boot.ServerErrorLocal._
import play.api.Logger
import com.variant.server.boot.ServerExceptionLocal
import com.variant.server.boot.ServerExceptionInternal


/**
 * A mutable set of schema generations, all sharing the same schema name and origin.
 * All generations but the most recent must be undeployed and draining. The most
 * recent may or may not be live.
 * 
 * Created with a seed generation, which provides the schema name and origin all
 * subsequent generations must match.
 * 
 * Cleaned up by the vacuum thread after all generations are gone.
 */
class ServerSchema(private val seed: SchemaGen) {
  
   private[this] val logger = Logger(this.getClass)
   
   // The top of the stack is the live gen.
   private[this] val gens = new ListBuffer[SchemaGen]()
   
   // This schema's name
   val name = seed.getMeta.getName
   
   // This schema's origin
   val origin = seed.origin
   
   deployGen(seed)
   
   /**
    * How many generations does this schema have?
    * If 0, it can be vacuumed.
    */
   def genCount = gens.size
   
   /**
    * Push a new live schema generation. Current live generation is undeployed.
    */
   def deployGen(gen: SchemaGen): Unit = {
      
      // New gen's name must match ours.
      if (gen.getMeta.getName != name)
         throw new ServerExceptionInternal(
               s"Cannot add schema gen [${gen.getMeta.getName}] to schema [${name}]");

      // New gen's origin must match ours.
      if (gen.origin != origin)
         throw new ServerExceptionLocal(SCHEMA_CANNOT_REPLACE, name, origin, gen.origin)

      val oldLive = liveGen      
      gen.state = SchemaGen.State.Live
      gen +=: gens
      oldLive.foreach { undeployGen(_) }
      
   }

   /**
    * Get the live gen.
    */
   def liveGen: Option[SchemaGen] = {
      // Must be at the head.
      gens.headOption match {
         case Some(gen) => if (gen.state == SchemaGen.State.Live) Some(gen) else None
         case None => None
      }
   }

      /**
    * Undeploy a particular gen
    */
   private[this] def undeployGen(gen: SchemaGen) {
      gen.state = SchemaGen.State.Dead
   	logger.info(s"Undeployed schema generation ID [${gen.id}] in schema [${name}]")
   }

   /**
    * Undeploy the live gen
    */
   def undeployLiveGen() {
      liveGen.foreach { gen =>
         gen.state = SchemaGen.State.Dead
         logger.info(s"Undeployed schema generation ID [${gen.id}] in schema [${name}]")
      }
   }

   /**
    * Remove dead drained generations
    */
   def vacuum() {
      gens.filter { gen =>
         gen.state == SchemaGen.State.Dead &&
         gen.sessionCount.get == 0
      }
      .foreach { gen =>
         gens -= gen
         logger.debug(s"Vacuumed schema generation ID [${gen.id}] in schema [${name}]")
      }
   }
   
   override def toString = gens.toString()
}
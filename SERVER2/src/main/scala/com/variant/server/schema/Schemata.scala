package com.variant.server.schema

import scala.collection.JavaConverters._
import scala.collection.concurrent.TrieMap

import com.variant.server.boot.ServerMessageLocal
import com.variant.server.boot.ServerExceptionInternal
import com.typesafe.scalalogging.LazyLogging

/**
 * 
 */
class Schemata extends LazyLogging {
     
   // Map of Schema objects keyed by schema name.
   private[this] val _schemaMap = new TrieMap[String, ServerSchema]()
  
   /**
    * Deploy a schema generation
    */
   def deploy(gen: SchemaGen) {
         
      _schemaMap.get(gen.getMeta.getName) match {
         
         case Some(schema) => schema.deployGen(gen)
         
         case None         => {
            _schemaMap += gen.getMeta.getName -> new ServerSchema(gen)
            logger.debug(s"Created new schema [${gen.getMeta.getName}]")
         }
      }
      
      // Write log message
      val msg = ServerMessageLocal.SCHEMA_DEPLOYED.asMessage(gen.getMeta.getName, gen.origin) + '\n' +
           "Name: " + gen.getMeta.getName + "\n" +
           "   Comment: " + gen.getMeta.getComment + "\n" + 
           "   States: " + gen.getStates.size + "\n" + 
           "   Variations: " + gen.getVariations.size
  
      logger.info(msg)
      
   }
   
   /**
    * Get a schema by name
    */
   def get(name: String): Option[ServerSchema] = { _schemaMap.get(name) }

   /**
    * Get a given schema's live generation as an Option.
    */
   def getLiveGen(name: String): Option[SchemaGen] = {
      
      _schemaMap.get(name) match {
         case Some(schema) => schema.liveGen
         case None => None
      }
   }
   
   /**
    * Get a list of all live generations.
    */
   def getLiveGens(): List[SchemaGen] = {
      
      _schemaMap.values.map(_.liveGen).flatten.toList

   }

   /**
    * Undeploy schema by origin.
    * When a schema file is deleted, all we have if the file name.
    * There must always be a schema with this origin.
    */
   def undeploy(origin: String) {
      
      val schemaToUndeploy = _schemaMap.filter ( e => { e._2.origin == origin } )
      
      if (schemaToUndeploy.size > 1)
         throw new ServerExceptionInternal(s"Found ${schemaToUndeploy.size} schemata with origin ${origin}")
      
      schemaToUndeploy.foreach { e => e._2.undeployLiveGen() }
   }
   
   /**
    * Count of live schemata
    */
   def size = _schemaMap.filter(_._2.liveGen.isDefined).size
   
   /**
    * Undeploy all schemata.
    */
   def undeployAll() {
      _schemaMap.foreach { _._2.undeployLiveGen() }
   }
   
   /**
    * Remove dead generations and empty schemata.
    */
   def vacuum() {
      
      // Let each schema vacuum.
      _schemaMap.values.foreach { _.vacuum() }
      
      // Remove empty schemata
      val toDelete = _schemaMap.filter( _._2.genCount == 0)
      toDelete.foreach { e =>
         _schemaMap -= e._1
         logger.debug(s"Vacuumed schema [${e._2.name}]")
      }
   }
   
   override def toString = _schemaMap.toString()
}
    

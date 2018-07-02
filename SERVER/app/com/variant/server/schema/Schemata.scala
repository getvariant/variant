package com.variant.server.schema

import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap
import com.variant.server.api.ServerException
import com.variant.server.boot.ServerErrorLocal
import play.api.Logger
import scala.collection.concurrent.TrieMap

/**
 * 
 */
class Schemata () {
  
   private[this] val logger = Logger(this.getClass)
   
   // Map of Schema objects keyed by schema name.
   private[this] val _schemaMap = new TrieMap[String, Schema]()
  
   /**
    * Deploy a schema generation
    */
   def deploy(gen: SchemaGen) {
         
      _schemaMap.get(gen.getName()) match {
         case Some(schema) => schema.deployGen(gen)
         case None         => _schemaMap += gen.getName() -> new Schema(gen)
      }
      
      // Write log message
      val msg = new StringBuilder()
      msg.append("Deployed schema [%s] ID [%s], from [%s]:".format(gen.getName, gen.getId, gen.origin));
      for (test <- gen.getTests) {
         msg.append("\n   ").append(test.getName()).append(":[");
         var first = true;
      for (exp <- test.getExperiences()) {
        if (first) first = false;
        else msg.append(", ");
        msg.append(exp.getName);
        if (exp.isControl) msg.append(" (control)");
      }
      msg.append("]");
      msg.append(" (").append(if (test.isOn()) "ON" else "OFF").append(")");
    }

    logger.info(msg.toString())

   }
   
   /**
    * Get a schema by name
    */
   def get(name: String): Option[Schema] = { _schemaMap.get(name) }

   
   /**
    * Undeploy schema by origin.
    * When a schema file is deleted, all we have if the file name.
    * There must always be a schema with this origin.
    */
   def undeploy(origin: String) {
      
      val schemaToRemove = _schemaMap.filter ( e => { e._2.origin == origin } )
      
      if (schemaToRemove.size > 1)
         throw new ServerException.Internal(s"Found ${schemaToRemove.size} schemata with origin ${origin}")
      
      schemaToRemove.foreach { e => 
         _schemaMap -= e._1
         e._2.undeployLiveGen() 
      }
   }
   
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
      // Remove dead generations
      _schemaMap.values.foreach { _.vacuum() }
      // Remove empty schemata
      val toDelete = _schemaMap.filter( _._2.genCount == 0)
      toDelete.foreach { e =>
         _schemaMap -= e._1
         logger.debug(s"Vacuumed schemata [${e._2.name}]")
      }
   }
   
   /**
    * Contents as an immutable map
    *
   def toMap = synchronized { _schemaMap.toMap }
   * 
   */
}
    

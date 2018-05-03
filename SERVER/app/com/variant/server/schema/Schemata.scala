package com.variant.server.schema

import scala.collection.mutable.HashMap
import com.variant.server.api.ServerException
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.conn.ConnectionStore

/**
 * 
 */
class Schemata () {
  
   private[this] val _schemaMap = HashMap[String, ServerSchema]()
   
   /**
    * Get a schema by name
    */
   def get(name: String) = synchronized { _schemaMap.get(name) }
   
   /**
    * Atomically swap old schema with new schema.
    */
   def put(newSchema: ServerSchema): Unit = synchronized {
      
      // If already have a schema with that name, only replace if origins match.
      _schemaMap.get(newSchema.getName) match {   
         case Some(oldSchema) => {
            if (oldSchema.origin != newSchema.origin) {
              throw new ServerException.Local(ServerErrorLocal.SCHEMA_CANNOT_REPLACE, newSchema.getName(), oldSchema.origin, newSchema.origin)
           }
           oldSchema.undeploy()
         }
         case None => // There wasn't a schema by this name already.
      }
      
      newSchema.state = State.Deployed
      _schemaMap += (newSchema.getName -> newSchema)
   }
   
   /**
    * Delete a schema by origin.
    * Remove schema from this map
    */
   def undeploy(origin: String) = synchronized {
      
      // There should be at most one existing schema with the given origin.
      val schemaToRemove = _schemaMap.filter ( e => { e._2.origin == origin } )
      if (schemaToRemove.size > 1)
         throw new ServerException.Internal(s"Found ${schemaToRemove.size} schemata with origin ${origin}")
      
      schemaToRemove.foreach { e => 
         _schemaMap -= e._1
         e._2.undeploy() 
      }
   }
   
   /**
    * Contents as an immutable map
    */
   def toMap = synchronized { _schemaMap.toMap }
}
    

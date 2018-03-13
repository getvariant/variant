package com.variant.server.schema

import scala.collection.mutable.HashMap
import com.variant.server.api.ServerException
import com.variant.server.boot.ServerErrorLocal

/**
 * 
 */
class Schemata() {
  
   private[this] val _schemata = HashMap[String, ServerSchema]()

   /**
    * Get a schema by name
    */
   def get(name: String) = synchronized { _schemata.get(name) }
   
   /**
    * Atomically swap old schema with new schema.
    */
   def put(newSchema: ServerSchema): Unit = synchronized {
      
      // If already have a schema with that name, only replace if origins match.
      _schemata.get(newSchema.getName) match {   
         case Some(oldSchema) => {
            if (oldSchema.origin != newSchema.origin) {
              throw new ServerException.User(ServerErrorLocal.SCHEMA_CANNOT_REPLACE, newSchema.getName(), oldSchema.origin, newSchema.origin)
           }
           oldSchema.undeploy()
         }
         case None => // There wasn't a schema by this name already.
      }
      
      newSchema.state = State.Deployed
      _schemata += (newSchema.getName -> newSchema)
   }
   
   /**
    * Delete by origin.
    */
   def delete(origin: String) {
      // There should be at most one existing schema with the given origin.
      val schemaToRemove = _schemata.filter ( e => { e._2.origin == origin } )
      if (schemaToRemove.size > 1)
         throw new ServerException.Internal(s"Found ${schemaToRemove.size} schemata with origin ${origin}")
      
      schemaToRemove.foreach { e => 
         _schemata -= e._1
         e._2.undeploy()
      }
   }
   
   /**
    * Contents as an immutable map
    */
   def toMap = synchronized { _schemata.toMap }
}
    

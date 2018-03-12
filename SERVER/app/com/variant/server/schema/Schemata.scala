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
            if (oldSchema.origin != oldSchema.origin) {
              throw new ServerException.User(ServerErrorLocal.SCHEMA_CANNOT_REPLACE)
           }
           // Gone schemas are cleaned out by the vacuum thread.
           oldSchema.state = State.Gone
         }
         case None => // There wasn't a schema by this name already.
      }
      
      newSchema.state = State.Deployed
      _schemata += (newSchema.getName -> newSchema)
   }
   
   /**
    * Contents as an immutable map
    */
   def toMap = synchronized { _schemata.toMap }
}
    

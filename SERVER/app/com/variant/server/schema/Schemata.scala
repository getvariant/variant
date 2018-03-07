package com.variant.server.schema

import scala.collection.mutable.HashMap

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
    * Atomically replace given schema.
    */
   def replace(schema: ServerSchema): Option[ServerSchema] = synchronized {
      val result = _schemata.get(schema.getName)
      _schemata += (schema.getName -> schema)
      result
   }
   
   /**
    * Contents as an immutable map
    */
   def toMap = synchronized { _schemata.toMap }
}
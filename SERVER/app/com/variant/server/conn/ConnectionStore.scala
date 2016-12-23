package com.variant.server.conn;

import java.util.Map
import java.util.Set
import java.util.concurrent.ConcurrentHashMap
import com.variant.server.ConfigKeys._
import com.variant.server.boot.VariantServer
import javax.inject.Inject
import javax.inject.Singleton
import play.api.Logger
import com.variant.server.boot.VariantServer
import com.variant.server.Connection

/**
 * Connection Store.
 * Connections are not expired, unless explicitely closed by the client.
 * Only a configurable max number of them is accepted.
 * 
 * @author Igor
 *
 */
trait ConnectionStore {

   /**
    * Save a connection in this store.
    * Can be retrieved by its ID.
    * @returns true if stored or false if connection table is full.
    */
	def put(conn: Connection): Boolean
		
	/**
	 * Retrieve a connection from this store.
	 */
	def get(id: String) : Option[Connection]
	
	/**
	 * Delete a connection from this store.
	 */
	def remove(id: String): Option[Connection]
}

@Singleton
class ConnectionStoreImpl() extends ConnectionStore {

   private val logger = Logger(this.getClass)	
	private val connMap = new ConcurrentHashMap[String, Connection]()
	//println("*********************** " + VariantServer.server)
	private lazy val maxSize = VariantServer.server.config.getInt(MAX_CONCURRENT_CONNECTIONS)
   
	/**
	 */
	override def put(conn: Connection): Boolean = {
      if (connMap.size() >= maxSize) {
         false
      }
      else {
         connMap.put(conn.id, conn)
         true
      }
   }
	
	/**
	 */
   override def get(id: String): Option[Connection] = {
      val result = connMap.get(id)
      if (result == null) None
      else Some(result)
	}
   
   /**
    */
   override def remove(id: String): Option[Connection] = {
      val result = connMap.remove(id)
      if (result == null) None
      else Some(result)      
   }

}


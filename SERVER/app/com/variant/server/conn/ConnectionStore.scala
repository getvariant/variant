package com.variant.server.conn;

import java.util.Map
import java.util.Set

import com.variant.server.api.ConfigKeys._

import javax.inject.Inject
import javax.inject.Singleton

import play.api.Logger

import com.variant.server.boot.VariantServer

import scala.collection.concurrent.TrieMap

import com.typesafe.config.Config

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
    * Returns true if stored or false if connection table is full.
    */
	def put(conn: Connection): Boolean
		
	/**
	 * Retrieve a connection from this store by its ID.
	 */
	def get(id: String) : Option[Connection]
	
	/**
	 * Close a connection and remove it from this store by its ID.
	 */
	def close(id: String): Option[Connection]
	
}

@Singleton
class ConnectionStoreImpl @Inject() (server: VariantServer) extends ConnectionStore {

   private val logger = Logger(this.getClass)
	private lazy val maxSize = server.config.getInt(MAX_CONCURRENT_CONNECTIONS)
   private val connMap = new TrieMap[String, Connection]()
   
	/**
	 */
	override def put(conn: Connection): Boolean = {
      if (connMap.size >= maxSize) {
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
      connMap.get(id)
	}
   
   /**
    */
   override def close(id: String): Option[Connection] = {
      connMap.remove(id).map { x => x.close(); x }
   }

}

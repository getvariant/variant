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
import com.variant.server.api.ServerException
import com.variant.core.ServerError

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
	 * Retrieve a connection from this store or throw an exception if it doesn't exist.
	 */
	def getOrBust(cid: String): Connection
	
   /**
	 * Delete a connection from this store or throw an exception if it doesn't exist.
	 */
	def deleteOrBust(cid: String): Connection

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
   override def get(cid: String): Option[Connection] = {
      connMap.get(cid)
   }
   
   /**
	 */
	override def getOrBust(cid: String): Connection = {
      val result	= get(cid).getOrElse {
         logger.debug(s"Not found connection [${cid}]")      
         throw new ServerException.Remote(ServerError.UnknownConnection, cid)
      }
      logger.debug(s"Found connection [${cid}]")            
      result
	}

   /**
	 */
	override def deleteOrBust(cid: String): Connection = {
      val conn = connMap.remove(cid).getOrElse {
         logger.debug(s"Not found connection [${cid}]")      
         throw new ServerException.Remote(ServerError.UnknownConnection, cid)
      }
      conn.close()
      logger.debug(s"Found connection [${cid}]")           
      conn
	}

}

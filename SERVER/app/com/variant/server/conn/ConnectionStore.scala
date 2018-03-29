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
	 * Close a connection. Client-side op, so we can't assume the connection
	 * to exist. Rather, if it does not, throw a client side error.
	 * If exists, connection is removed from this store, though references to it
	 * may still exist in sessions opened via it, which continue to be valid until
	 * they expire. Once the last session is expired and garbage collected, so will
	 * be the closed connection object.
	 */
	def closeOrBust(cid: String): Connection
	
	/**
	 * Close all connection to a given schema. 
	 * Server-side operation so we don't check existence.
	 * Attempts to close a connection that's already closed are noops.
	 * See comments above.
	 */
	def closeAll(schid: String)

}

@Singleton
class ConnectionStoreImpl () extends ConnectionStore {

   private val logger = Logger(this.getClass)
	private lazy val maxSize = VariantServer.instance.config.getInt(MAX_CONCURRENT_CONNECTIONS)
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
	override def closeOrBust(cid: String): Connection = {
      val conn = connMap.remove(cid).getOrElse {
         logger.debug(s"Not found connection [${cid}]")      
         throw new ServerException.Remote(ServerError.UnknownConnection, cid)
      }
      conn.close()
      logger.debug(s"Found connection [${cid}]")           
      conn
	}

	/**
	 * Close all connections to a schema, by schema id.  
	 */
	override def closeAll(schid: String) {
	   
	   connMap
	      .filter { e => 
	         e._2.schema.getId == schid 
	      }
	      .foreach { e => 
	         e._2.close()
	         connMap -= e._1
	         logger.debug("Closed connection ID [%s] to schema [%s]".format(e._2.id, e._2.schema.getName()))
	      }
	 
	}
}

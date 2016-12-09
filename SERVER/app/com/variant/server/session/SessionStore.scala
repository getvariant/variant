package com.variant.server.session;

import java.util.Map
import java.util.Set
import java.util.concurrent.ConcurrentHashMap
import com.variant.server.ServerPropertiesKey._
import com.variant.server.boot.VariantServer
import javax.inject.Inject
import javax.inject.Singleton
import play.api.Logger
import com.variant.server.boot.VariantServer
import com.variant.core.VariantProperties

/**
 * Sessions are stored serialized as unparsed JSON strings and only deserialized when are needed
 * on the server.
 * 
 * @author Igor
 *
 */
trait SessionStore {
   /**
    * Save session in the store.
    * 
    * Returns session entry of the session previously associated with this SID, 
    * or null if no existing session with this SID.
    */
	def put(sid: String, json: String) : SessionStoreEntry
	
	/**
	 * Retrieve a session from the store as a JSON string.
	 */
	def asString(sid: String) : Option[String]
	
	/**
	 * Retrieve a session from this store as a ServerSession object.
	 */
	def asSession(sid: String) : Option[ServerSession]
	//def getAll() : Set[Map.Entry[String, SessionStoreEntry]]
}

@Singleton
class SessionStoreImpl @Inject() (server: VariantServer) extends SessionStore {

   private val logger = Logger(this.getClass)	
	private val cacheMap = new ConcurrentHashMap[String, SessionStoreEntry]();

   new VacuumThread(server.properties, cacheMap).start();

   /**
	 */
	override def put(sid: String, json: String) = cacheMap.put(sid, new SessionStoreEntry(json))
	
	/**
	 */
	override def asString(sid: String) = {		
		val result = cacheMap.get(sid);
		Option(
		   if (result != null) {
		      result.lastAccessTimestamp = System.currentTimeMillis()
		      result.json
		   }
		   else null  
	    )
	}

	/**
	 * 
	 */
   override def asSession(sid: String) = {		
		val result = cacheMap.get(sid);
		Option(
		   if (result != null) {
		      result.lastAccessTimestamp = System.currentTimeMillis()
		      result.asSession(server)
		   }
		   else null  
	    )
	}
}

/**
 * @author Igor
 */
class SessionStoreEntry (val json: String) {

   private var session: ServerSession = null;
	var lastAccessTimestamp = System.currentTimeMillis();
		
	/**
	 * Lazily deserialize from json, if we have it.
	 * @return
	 */
	def asSession(server: VariantServer) = {
		if (session == null && json != null) session = ServerSession.fromJson(json)
		session
	}
}

/**
 * Background thread deletes cache entries older than the keep-alive interval.
 */
class VacuumThread(props: VariantProperties, storeMap: Map[String, SessionStoreEntry]) extends Thread {

   private val logger = Logger(this.getClass)	
   private val sessionTimeoutMillis = props.getInt(SESSION_TIMEOUT) * 1000
   private val vacuumingFrequencyMillis = props.getInt(SESSION_STORE_VACUUM_INTERVAL) * 1000
	setName("VariantSessionVacuum");
   setDaemon(true);


	override def run() {

      logger.info(s"Vacuum thread $getName started")		
		var interrupted = false
		
		while (true) {			
			
			try {
				val now = System.currentTimeMillis();
				var count = 0;
				val iter = storeMap.entrySet().iterator();
				while(iter.hasNext()) {
					val e = iter.next();
					if (sessionTimeoutMillis > 0 && e.getValue().lastAccessTimestamp + sessionTimeoutMillis < now) {
						iter.remove();
						count += 1;
   			      logger.trace(String.format("Vacuumed expired session ID [%s]", e.getKey()));
					}
				}
				
				if (logger.isDebugEnabled && count > 0) logger.debug(s"Vacuumed $count session(s)");
				if (logger.isTraceEnabled) logger.trace(s"Vacuumed $count session(s)");

				Thread.sleep(vacuumingFrequencyMillis)
				logger.trace("Vacuum thread woke up after %s millis".format(System.currentTimeMillis() - now))
			}
			catch {
			   case _: InterruptedException => interrupted = true;
			   case t: Throwable =>	logger.error("Unexpected exception in vacuuming thread.", t);
			}
			
			if (interrupted || isInterrupted()) {
				logger.info("Vacuum thread " + Thread.currentThread().getName() + " interrupted and exited.");
				storeMap.clear()
				return;
			}
		}
	}
}


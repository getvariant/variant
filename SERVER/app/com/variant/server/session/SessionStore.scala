package com.variant.server.session;

import java.util.concurrent.ConcurrentHashMap
import com.variant.core.impl.VariantCore
import com.variant.server.boot.Bootstrap
import javax.inject.Inject
import javax.inject.Singleton
import play.api.Logger
import java.util.Set
import java.util.Map
import com.variant.core.VariantProperties
import play.api.Configuration
import com.fasterxml.jackson.databind.ObjectMapper
import com.variant.core.exception.VariantRuntimeException
import com.variant.core.exception.VariantInternalException

/**
 * Sessions are stored serialized as unparsed JSON strings and only deserialized when are needed
 * on the server.
 * 
 * @author Igor
 *
 */
trait SessionStore {
	def put(sid: String, json: String) : SessionStoreEntry
	def asString(sid: String) : Option[String]
	def asSession(sid: String) : Option[ServerSession]
	//def getAll() : Set[Map.Entry[String, SessionStoreEntry]]
}

@Singleton
class SessionStoreImpl @Inject() (boot: Bootstrap) extends SessionStore {

   private val logger = Logger(this.getClass)	
	private val cacheMap = new ConcurrentHashMap[String, SessionStoreEntry]();

   new VacuumThread(boot.config, cacheMap).start();

   /**
	 */
	override def put(sid: String, json: String) = cacheMap.put(sid, new SessionStoreEntry(json, boot))
	
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
		      result.asSession
		   }
		   else null  
	    )
	}

}

/**
 * @author Igor
 */
class SessionStoreEntry (val json: String, boot: Bootstrap) {

   private var session: ServerSession = null;
	var lastAccessTimestamp = System.currentTimeMillis();
		
	/**
	 * Lazily deserialize from json, if we have it.
	 * @return
	 */
	def asSession = {
		if (session == null && json != null) {
			session = new ServerSession(json, boot);
		}
		session;
	}
}

/**
 * Background thread deletes cache entries older than the keep-alive interval.
 */
class VacuumThread(config: Configuration, storeMap: Map[String, SessionStoreEntry]) extends Thread {

   private val logger = Logger(this.getClass)	
   private val sessionTimeoutMillis = config.getInt("variant.session.timeout.secs").get * 1000;
   private val vacuumingFrequencyMillis = config.getInt("variant.session.store.vacuum.interval.secs").get * 1000;
	setName("VariantSessionVacuum");
   setDaemon(true);


	override def run() {

      logger.info(s"Vacuuming thread $getName started")		
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
				
				if (count > 0) logger.debug(s"Vacuumed $count session(s)");
				else           logger.trace(s"Vacuumed $count session(s)");

				Thread.sleep(vacuumingFrequencyMillis);

			}
			catch {
			   case _: InterruptedException => interrupted = true;
			   case t: Throwable =>	logger.error("Unexpected exception in vacuuming thread.", t);
			}
			
			if (interrupted || isInterrupted()) {
				logger.info("Vacuuming thread " + Thread.currentThread().getName() + " interrupted and exited.");
				storeMap.clear()
				return;
			}
		}
	}
}


package com.variant.server.session

import com.variant.core.session.CoreSession
import com.variant.core.event.VariantEvent
import javax.inject.Inject
import com.variant.core.schema.Schema
import com.variant.server.event.FlushableEventImpl
import com.variant.server.boot.VariantServer

/**
 * Server session knows how to trigger an event.
 * @author Igor
 */
/*
object ServerSession {
   def apply(jsonStr: String, core: VariantCore) {
      new ServerSession(jsonStr, core)
   }
}
*/
class ServerSession (rawJson: String) extends {
   
      // TODO Find a way to inject server.
      val server: VariantServer = null
   
   } with CoreSession(rawJson, server.schema) {
   
   
   /**
    */
	def triggerEvent(event: VariantEvent) {
		if (event == null) throw new IllegalArgumentException("Event cannot be null");		
		server.eventWriter.write(new FlushableEventImpl(event, this));
	}
}
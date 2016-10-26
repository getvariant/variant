package com.variant.server.session

import com.variant.core.impl.CoreSessionImpl
import com.variant.core.impl.VariantCore
import com.variant.core.event.VariantEvent
import com.variant.server.boot.Bootstrap
import javax.inject.Inject
import com.variant.server.event.PersistableEventImpl

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
class ServerSession (rawJson: String, boot: Bootstrap) extends CoreSessionImpl(rawJson, boot.core) {
  
   /**
    */
	def triggerEvent(event: VariantEvent) {
		if (event == null) throw new IllegalArgumentException("Event cannot be null");		
		boot.eventWriter.write(new PersistableEventImpl(event, this));
	}
}
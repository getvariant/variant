package com.variant.server.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.ConfigException;
import com.variant.core.schema.Flusher;
import com.variant.core.schema.parser.FlusherService;
import com.variant.core.schema.parser.ParserResponseImpl;
import com.variant.server.boot.ServerErrorLocal;
import com.variant.server.boot.VariantClassLoader;
import com.variant.server.api.EventFlusher;
import com.variant.server.api.EventFlusherNull;

public class ServerFlusherService implements FlusherService {

	private static final Logger LOG = LoggerFactory.getLogger(ServerFlusherService.class);

	// Default flusher is NULL flusher
	private EventFlusher flusher = new EventFlusherNull();
	
	private ServerSchema schema = null;
	
	
	/**
	 * Package instantiation only.
	 */
	ServerFlusherService() {}

	@Override
	public void initFlusher(Flusher flusher, ParserResponseImpl parserResponse) {

		try {
			// Create the Class object for the supplied UserHook implementation.
			Object flusherObj = VariantClassLoader.instantiate(flusher.getClassName(), flusher.getInit());
			
			if (flusherObj == null) {
				parserResponse.addMessage(ServerErrorLocal.OBJECT_CONSTRUCTOR_ERROR, flusher.getClassName());
				return;
			}			
			
			// It must implement the right interface.
			if (! (flusherObj instanceof EventFlusher)) {
				parserResponse.addMessage(ServerErrorLocal.FLUSHER_CLASS_NO_INTERFACE, flusherObj.getClass().getName(), EventFlusher.class.getName());
				return;
			}
			
			this.flusher = (EventFlusher) flusherObj;
						
		}
		catch (ConfigException.Parse e) {
			parserResponse.addMessage(ServerErrorLocal.OBJECT_INSTANTIATION_ERROR, flusher.getClassName(), e.getClass().getName());
		}
		catch (Exception e) {
			LOG.error(ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage(flusher.getClassName(), e.getClass().getName()), e);
			parserResponse.addMessage(ServerErrorLocal.OBJECT_INSTANTIATION_ERROR, flusher.getClassName(), e.getClass().getName());
		}
	}

	/**
	 * The event flusher underlying this service.
	 * @return
	 */
	public EventFlusher getFlusher() {
		return flusher;
	}
	
	/**
	 * 
	 * @param schema
	 */
	public void setSchema(ServerSchema schema) {
		this.schema = schema;
	}
	
	/**
	 * 
	 * @return
	 */
	public ServerSchema getSchema() {
		return this.schema;
	}
}

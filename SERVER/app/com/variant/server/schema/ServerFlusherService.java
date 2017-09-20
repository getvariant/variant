package com.variant.server.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.variant.core.schema.Flusher;
import com.variant.core.schema.parser.FlusherService;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.ParserResponseImpl;
import com.variant.core.schema.parser.SchemaParser;
import com.variant.server.boot.ServerErrorLocal;
import com.variant.server.boot.VariantClassLoader;
import com.variant.server.boot.VariantServer$;

import static com.variant.server.api.ConfigKeys.*;

import com.variant.server.api.EventFlusher;

public class ServerFlusherService implements FlusherService {

	private static final Logger logger = LoggerFactory.getLogger(ServerFlusherService.class);

	// Schema parser in whose scope we operate
	private final SchemaParser parser;
	
	// If flusher is not defined, it will be instantiated lazily by getFlusher
	private EventFlusher flusher = null;	
	private ServerSchema schema = null;
	
		
	private void initDefaultFlusher() {
		
		// This is how we access companion objects's fields from java.
		Config config = VariantServer$.MODULE$.instance().config();
		
		initFlusher(
				
				new Flusher() {
					
					@Override public String getClassName() {
						return config.getString(EVENT_FLUSHER_CLASS_NAME);
					}
					
					@Override public String getInit() {
						return config.getString(EVENT_FLUSHER_CLASS_INIT);
					}			
				});
	}
	
	/**
	 * Package instantiation only.
	 */
	ServerFlusherService(SchemaParser parser) {
		this.parser = parser;
	}

	@Override
	public void initFlusher(Flusher flusher) {

		ParserResponseImpl response = (ParserResponseImpl) parser.responseInProgress();
		
		try {
			// Create the Class object for the supplied UserHook implementation.
			Object flusherObj = VariantClassLoader.instantiate(flusher.getClassName(), flusher.getInit());
			
			if (flusherObj == null) {
				response.addMessage(ServerErrorLocal.OBJECT_CONSTRUCTOR_ERROR, flusher.getClassName());
				return;
			}			
			
			// It must implement the right interface.
			if (! (flusherObj instanceof EventFlusher)) {
				response.addMessage(ServerErrorLocal.FLUSHER_CLASS_NO_INTERFACE, flusherObj.getClass().getName(), EventFlusher.class.getName());
				return;
			}
			
			this.flusher = (EventFlusher) flusherObj;
			logger.info(String.format("Registered custom event logger [%s] for schema [%s]", flusher.getClassName(), schema.getName()));
						
		}
		catch (ConfigException.Parse e) {
			response.addMessage(ServerErrorLocal.OBJECT_INSTANTIATION_ERROR, flusher.getClassName(), e.getClass().getName());
		}
		catch (Exception e) {
			logger.error(ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage(flusher.getClassName(), e.getClass().getName()), e);
			response.addMessage(ServerErrorLocal.OBJECT_INSTANTIATION_ERROR, flusher.getClassName(), e.getClass().getName());
		}
	}

	/**
	 * The event flusher underlying this service.
	 * If none regustered at schema parse time, default to externally configured.
	 * @return
	 */
	public EventFlusher getFlusher() {
		if (flusher == null)  initDefaultFlusher();
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

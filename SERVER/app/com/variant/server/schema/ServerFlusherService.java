package com.variant.server.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.variant.core.schema.Flusher;
import com.variant.core.schema.Schema;
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
	
		
	private Flusher defaultFlusher() {
		
		// This is how we access companion objects's fields from java.
		Config config = VariantServer$.MODULE$.instance().config();
		
		return new Flusher() {
					
					@Override public String getClassName() {
						return config.getString(EVENT_FLUSHER_CLASS_NAME);
					}
					
					@Override public String getInit() {
						return config.getObject(EVENT_FLUSHER_CLASS_INIT).render();
					}			
		};
	}
	
	/**
	 * Package instantiation only.
	 */
	ServerFlusherService(SchemaParser parser) {
		this.parser = parser;
	}

	/**
	 * If we're invoked with null parameter, we take it that caller wants default flusher, 
	 * as externally configured in variant.conf.
	 */
	@Override
	public void initFlusher(Flusher flusher) {

		if (flusher == null) flusher = defaultFlusher();
		
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
			logger.info(String.format(
					"Registered event logger [%s] for schema [%s]", 
					flusher.getClassName(), parser.responseInProgress().getSchema().getName()));
						
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
		return flusher;
	}
	
	/**
	 * 
	 * @return
	 */
	public Schema getSchema() {
		return parser.responseInProgress().getSchema();
	}
}

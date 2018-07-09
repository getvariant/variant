package com.variant.server.schema;

import static com.variant.server.api.ConfigKeys.EVENT_FLUSHER_CLASS_INIT;
import static com.variant.server.api.ConfigKeys.EVENT_FLUSHER_CLASS_NAME;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.variant.core.schema.Flusher;
import com.variant.core.schema.Schema;
import com.variant.core.schema.parser.FlusherService;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.SchemaParser;
import com.variant.server.api.EventFlusher;
import com.variant.server.boot.ServerErrorLocal;
import com.variant.server.boot.VariantServer;
import com.variant.server.boot.VariantServer$;

public class ServerFlusherService implements FlusherService {

	private static final Logger logger = LoggerFactory.getLogger(ServerFlusherService.class);

	// Schema parser in whose scope we operate
	private final SchemaParser parser;
	
	// This is how we access companion objects's fields from java.
	private final VariantServer server = VariantServer$.MODULE$.instance();
	
	// If flusher is not defined, it will be instantiated lazily by getFlusher
	private EventFlusher flusher = null;	
	
		
	private Flusher defaultFlusher() {

		Config config = server.config();

		if (!config.hasPath(EVENT_FLUSHER_CLASS_NAME)) {
			parser.responseInProgress().addMessage(ServerErrorLocal.FLUSHER_NOT_CONFIGURED);
			return null;
		}

		return new Flusher() {
					
					@Override public String getClassName() {
						return config.getString(EVENT_FLUSHER_CLASS_NAME);
					}
					
					@Override public String getInit() {
						return config.getIsNull(EVENT_FLUSHER_CLASS_INIT) ? "null" :
								config.getObject(EVENT_FLUSHER_CLASS_INIT).render();
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
		
		// If default flusher is misconfigured, it's still null -- quit.
		if (flusher == null) return;
		
		ParserResponse response = parser.responseInProgress();
		
		try {
			// Create the Class object for the supplied LifecycleHook implementation.
			Object flusherObj = server.classloader().instantiate(flusher.getClassName(), flusher.getInit());
			
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
					"Registered event flusher [%s] for schema [%s]", 
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

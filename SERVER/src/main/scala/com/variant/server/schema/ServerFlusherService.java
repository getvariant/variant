package com.variant.server.schema;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.schema.Flusher;
import com.variant.core.schema.Schema;
import com.variant.core.schema.parser.FlusherService;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.SchemaParser;
import com.variant.server.api.Configuration;
import com.variant.server.api.TraceEventFlusher;
import com.variant.server.boot.ServerMessageLocal;
import com.variant.server.util.ClassUtil;

public class ServerFlusherService implements FlusherService {

	private static final Logger logger = LoggerFactory.getLogger(ServerFlusherService.class);

   // Schema parser in whose scope we operate
   private final Configuration config;

   // Schema parser in whose scope we operate
	private final SchemaParser parser;
		
	// If flusher is not defined, it will be instantiated lazily by getFlusher
	private TraceEventFlusher flusher = null;	
	
	// Get the default flusher. 
	private Flusher defaultFlusher() {
	   
		return new Flusher() {
					
					@Override public String getClassName() {
						return config.defaultEventFlusherClassName();
					}
					
					@Override public Optional<String> getInit() {
						return config.defaultEventFlusherClassInit();
					}			
		};
	}
	
	/**
	 * Package instantiation only.
	 */
	ServerFlusherService(Configuration config, SchemaParser parser) {
		this.parser = parser;
		this.config = config;
	}

	/**
	 * If we're invoked with empty arg, we take it that caller wants default flusher, 
	 * as externally configured in variant.conf.
	 */
	@Override
	public void initFlusher(Optional<Flusher> flusherArg) {

		Flusher f = flusherArg.orElse(defaultFlusher());
		
		// If default flusher is misconfigured, it's still null -- quit.
		if (f == null) return;
		
		ParserResponse response = parser.responseInProgress();
		
		try {

		   // Create the Class object for the supplied LifecycleHook implementation.
			Object flusherObj = ClassUtil.instantiate(f.getClassName(), f.getInit());
			
			if (flusherObj == null) {
				response.addMessage(ServerMessageLocal.OBJECT_CONSTRUCTOR_ERROR(), f.getClassName());
				return;
			}			
			
			// It must implement the right interface.
			if (! (flusherObj instanceof TraceEventFlusher)) {
				response.addMessage(ServerMessageLocal.FLUSHER_CLASS_NO_INTERFACE(), flusherObj.getClass().getName(), TraceEventFlusher.class.getName());
				return;
			}
			
			flusher = (TraceEventFlusher) flusherObj;
			logger.debug(String.format(
					"Registered event flusher [%s] for schema [%s]", 
					f.getClassName(), parser.responseInProgress().getSchema().getMeta().getName()));
						
		}
		catch (Exception e) {
			response.addMessage(ServerMessageLocal.OBJECT_INSTANTIATION_ERROR(), e, f.getClassName(), e.getClass().getName());
		}
	}

	/**
	 * The event flusher underlying this service.
	 * If none regustered at schema parse time, default to externally configured.
	 * @return
	 */
	public TraceEventFlusher getFlusher() {
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

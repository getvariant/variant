package com.variant.server.schema;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.ConfigException;
import com.variant.core.schema.Flusher;
import com.variant.core.schema.Schema;
import com.variant.core.schema.parser.FlusherService;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.SchemaParser;
import com.variant.server.api.Configuration;
import com.variant.server.api.TraceEventFlusher;
import com.variant.server.boot.ServerErrorLocal;
import com.variant.server.boot.VariantServer;
import com.variant.server.boot.VariantServer$;
import com.variant.server.util.ClassUtil;

public class ServerFlusherService implements FlusherService {

	private static final Logger logger = LoggerFactory.getLogger(ServerFlusherService.class);

	// Schema parser in whose scope we operate
	private final SchemaParser parser;
	
	// This is how we access companion objects's fields from java.
	private final VariantServer server = VariantServer$.MODULE$.instance();
	
	// If flusher is not defined, it will be instantiated lazily by getFlusher
	private TraceEventFlusher flusher = null;	
	
	// Get the default flusher. 
	private Flusher defaultFlusher() {

		Configuration config = server.config();

		return new Flusher() {
					
					@Override public String getClassName() {
						return config.getEventFlusherClassName();
					}
					
					@Override public Optional<String> getInit() {
						return config.getEventFlusherClassInit();
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
	public void initFlusher(Optional<Flusher> flusherArg) {

		Flusher f = flusherArg.orElse(defaultFlusher());
		
		// If default flusher is misconfigured, it's still null -- quit.
		if (f == null) return;
		
		ParserResponse response = parser.responseInProgress();
		
		try {
			// Create the Class object for the supplied LifecycleHook implementation.
			Object flusherObj = ClassUtil.instantiate(f.getClassName(), f.getInit());
			
			if (flusherObj == null) {
				response.addMessage(ServerErrorLocal.OBJECT_CONSTRUCTOR_ERROR, f.getClassName());
				return;
			}			
			
			// It must implement the right interface.
			if (! (flusherObj instanceof TraceEventFlusher)) {
				response.addMessage(ServerErrorLocal.FLUSHER_CLASS_NO_INTERFACE, flusherObj.getClass().getName(), TraceEventFlusher.class.getName());
				return;
			}
			
			flusher = (TraceEventFlusher) flusherObj;
			logger.info(String.format(
					"Registered event flusher [%s] for schema [%s]", 
					f.getClassName(), parser.responseInProgress().getSchema().getMeta().getName()));
						
		}
		catch (ConfigException.Parse e) {
			logger.error(ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage(f.getClassName(), e.getClass().getName()), e);
			response.addMessage(ServerErrorLocal.OBJECT_INSTANTIATION_ERROR, f.getClassName(), e.getClass().getName());
		}
		catch (Exception e) {
			// We log here, even though the schema deployer will echo the error again, in order to log e's call stack.
			logger.error(ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage(f.getClassName(), e.getClass().getName()), e);
			response.addMessage(ServerErrorLocal.OBJECT_INSTANTIATION_ERROR, f.getClassName(), e.getClass().getName());
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

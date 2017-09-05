package com.variant.server.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.variant.core.EventFlusher;
import com.variant.core.LifecycleEvent;
import com.variant.core.UserHook;
import com.variant.core.schema.Flusher;
import com.variant.core.schema.Hook;
import com.variant.core.schema.ParseTimeLifecycleEvent;
import com.variant.core.schema.parser.FlusherService;
import com.variant.core.schema.parser.ParserResponseImpl;
import com.variant.server.api.ServerException;
import com.variant.server.api.hook.TestScopedLifecycleEvent;
import com.variant.server.boot.ServerErrorLocal;
import com.variant.server.boot.VariantClassLoader;

public class ServerFlusherService implements FlusherService {

	private static final Logger LOG = LoggerFactory.getLogger(ServerFlusherService.class);

	/**
	 * Package instantiation only.
	 */
	ServerFlusherService() {}

	@Override
	public void initFlusher(Flusher flusher, ParserResponseImpl parserResponse) {

		try {
			// Create the Class object for the supplied UserHook implementation.
			Class<?> flusherClass = VariantClassLoader.instance.loadClass(flusher.getClassName());
			
			
			OBJECT_CONSTRUCTOR_ERROR
			Object flusherObject = flusherClass.newInstance();
					
			// It must implement the right interface.
			if (! (flusherObject instanceof UserHook)) {
				parserResponse.addMessage(ServerErrorLocal.FLUSHER_CLASS_NO_INTERFACE, flusherClass.getName(), EventFlusher.class.getName());
				return;
			}
						
			// Parse init JSON string
			ConfigValue config = null;
			if (flusher.getInit() != null) {
				config = ConfigFactory.parseString("{init:"  + flusher.getInit() + "}").getValue("init"); 
			}


			LOG.debug("Init'ed Hook [" + flusher.getClassName() + "]");
		}
		catch (ConfigException.Parse e) {
			parserResponse.addMessage(ServerErrorLocal.HOOK_INSTANTIATION_ERROR, hook.getClassName(), e.getClass().getName());
		}
		catch (Exception e) {
			LOG.error(ServerErrorLocal.HOOK_INSTANTIATION_ERROR.asMessage(hook.getClassName(), e.getClass().getName()), e);
			parserResponse.addMessage(ServerErrorLocal.HOOK_INSTANTIATION_ERROR, hook.getClassName(), e.getClass().getName());
		}
		
	}

}

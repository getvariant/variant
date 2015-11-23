package com.variant.core.session;

import com.variant.core.VariantBootstrapException;
import com.variant.core.VariantSessionStore;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.schema.parser.MessageTemplate;

/**
 * 
 * @author Igor
 *
 */
public class SessionStoreFactory {
			
	/**
	 * 
	 * @return
	 */
	public static VariantSessionStore getInstance(String className) {
		
		try {
			Class<?> persisterClass = Class.forName(className);
			Object persisterObject = persisterClass.newInstance();
			if (persisterObject instanceof VariantSessionStore) {
				return (VariantSessionStore) persisterObject;
			}
			else {
				throw new VariantBootstrapException(MessageTemplate.BOOT_SESSION_STORE_NO_INTERFACE, className, VariantSessionStore.class.getName());
			}
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to instantiate session store class [" + className + "]", e);
		}
	}
	
}

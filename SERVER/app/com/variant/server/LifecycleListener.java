package com.variant.server;

import com.variant.core.schema.Schema;


/**
 * Variant Server lifecycle listeners.
 * User may provide one or more implementations of this interface. They will be discovered
 * at startup and posted when an event of interest takes place.
 * 
 * @author Igor Urisman
 * @since 0.7
 */
public interface LifecycleListener {

	void serverStart();
	
	void serverShutdown();
	
	void schemaDeploy(Schema schema);
	
	void schemaUndeploy(Schema schema);

}

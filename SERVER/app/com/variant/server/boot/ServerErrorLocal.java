package com.variant.server.boot;

import com.variant.core.impl.CommonError;

/**
 * Local Server Errors, i.e. ones thrown to the server log and not sent back to client.
 * These are raised by UserErrorException.
 */
public class ServerErrorLocal extends CommonError {

	//
	// 401-420 Server bootstrap
	//
	public final static ServerErrorLocal SCHEMATA_DIR_MISSING = 
			new ServerErrorLocal(401, Severity.FATAL, "Schemata deploy directory [%s] does not exist");

	public final static ServerErrorLocal SCHEMATA_DIR_NOT_DIR = 
			new ServerErrorLocal(402, Severity.FATAL, "Schemata deploy directory file [%s] exists but is not a directory");
	
	public final static ServerErrorLocal EVENT_FLUSHER_NO_INTERFACE =
			new ServerErrorLocal(403, Severity.FATAL, "Event flusher class [%s] must implement interface [%s]"); 

	//
	// 421-440 Schema deployment
	//
	public final static ServerErrorLocal SCHEMA_CANNOT_REPLACE = 
			new ServerErrorLocal(421, Severity.ERROR, "Cannot replace existing schema [%s] defined in file [%s] with another schema with the same name defined in file [%s]");

	public final static ServerErrorLocal OBJECT_CONSTRUCTOR_ERROR = 
			new ServerErrorLocal(422, Severity.ERROR, "Unable to instantiate class [%s] becuase no suitable constructor was found");

	public final static ServerErrorLocal OBJECT_INSTANTIATION_ERROR = 
			new ServerErrorLocal(423, Severity.ERROR, "Unable to instantiate class [%s] due to error [%s]. See log for details");

	public final static ServerErrorLocal HOOK_CLASS_NO_INTERFACE = 
			new ServerErrorLocal(424, Severity.ERROR, "Lifecycle hook class [%s] must implement interface [%s]");

	public final static ServerErrorLocal HOOK_STATE_SCOPE_VIOLATION = 
			new ServerErrorLocal(425, Severity.ERROR, "Lifecycle hook [%s], defined at state [%s] cannot listen to life cycle event [%s]");
	
	public final static ServerErrorLocal HOOK_TEST_SCOPE_VIOLATION = 
			new ServerErrorLocal(426, Severity.ERROR, "Lifecycle hook [%s], defined at test [%s] cannot listen to life cycle event [%s]");

	public final static ServerErrorLocal FLUSHER_NOT_CONFIGURED = 
			new ServerErrorLocal(427, Severity.ERROR, "No event flusher defined in experiment schema, and no default event flusher is configured");

	public final static ServerErrorLocal FLUSHER_CLASS_NO_INTERFACE = 
			new ServerErrorLocal(428, Severity.ERROR, "Event flusher class [%s] must implement interface [%s]");

	public final static ServerErrorLocal SCHEMA_FAILED = 
			new ServerErrorLocal(429, Severity.WARN, "Schema [%s] was not deployed from [%s] due to previous errors");

	public final static ServerErrorLocal EMPTY_SCHEMATA = 
			new ServerErrorLocal(430, Severity.INFO, "No schemata found in [%s]");

	public final static ServerErrorLocal SERVER_BOOT_OK = 
			new ServerErrorLocal(431, Severity.INFO, "%s bootstrapped on :%s%s in %s");

	public final static ServerErrorLocal SERVER_BOOT_FAILED = 
			new ServerErrorLocal(432, Severity.INFO, "%s failed to bootstrap due to following errors:");

	public final static ServerErrorLocal SERVER_SHUTDOWN = 
			new ServerErrorLocal(433, Severity.INFO, "%s shutdown on :%s%s, uptime %s.");


	
   /**
    * 
    */
   private ServerErrorLocal(int code, Severity severity, String format) {
		super(code, severity, format);
	}

}

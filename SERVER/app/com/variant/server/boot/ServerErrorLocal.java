package com.variant.server.boot;

import com.variant.core.CommonError;

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
	public final static ServerErrorLocal MULTIPLE_SCHEMATA_NOT_SUPPORTED = 
			new ServerErrorLocal(421, Severity.FATAL, "Schemata directory [%s] cannot contain multiple files");

	//
	// 461-480 Server API
	//

	public final static ServerErrorLocal HOOK_CLASS_NO_INTERFACE = 
			new ServerErrorLocal(461, Severity.ERROR, "Hook class [%s] must implement interface [%s]");

	public final static ServerErrorLocal HOOK_INSTANTIATION_ERROR = 
			new ServerErrorLocal(462, Severity.ERROR, "Unable to instantiate hook class [%s] due to error [%s]. See log for details");

	public final static ServerErrorLocal HOOK_TARGETING_BAD_EXPERIENCE = 
			new ServerErrorLocal(463, Severity.ERROR, "Targeting hook [%s] for test [%s] cannot set experience [%s]");

	public final static ServerErrorLocal HOOK_SCHEMA_DOMAIN_DEFINED_AT_TEST = 
			new ServerErrorLocal(464, Severity.ERROR, "User hook [%s], which listens to the schema-scoped life cycle event [%s], cannot be defined at test level. (Test [%s]");
	
	public final static ServerErrorLocal HOOK_TEST_DOMAIN_DEFINED_AT_SCHEMA = 
			new ServerErrorLocal(465, Severity.ERROR, "User hook [%s], which listens to the test-scoped life cycle event [%s], cannot be defined at schema level");

	//
	// 481-500 Other server runtime
	//
	public final static ServerErrorLocal STATE_UNDEFINED_IN_EXPERIENCE =
			new ServerErrorLocal(481, Severity.ERROR, "Currently active experience [%s] is undefined on state [%s]");

	public static final ServerErrorLocal EXPERIENCE_WEIGHT_MISSING = 
			new ServerErrorLocal(482, Severity.ERROR, "No weight specified for Test [%s], Experience [%s] and no custom targeter found");

	
   /**
    * 
    */
   private ServerErrorLocal(int code, Severity severity, String format) {
		super(code, severity, format);
	}

}

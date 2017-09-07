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

	public final static ServerErrorLocal OBJECT_CONSTRUCTOR_ERROR = 
			new ServerErrorLocal(422, Severity.ERROR, "Unable to instantiate class [%s] becuase no suitable constructor was found.");

	public final static ServerErrorLocal OBJECT_INSTANTIATION_ERROR = 
			new ServerErrorLocal(423, Severity.ERROR, "Unable to instantiate class [%s] due to error [%s]. See log for details");

	public final static ServerErrorLocal HOOK_CLASS_NO_INTERFACE = 
			new ServerErrorLocal(424, Severity.ERROR, "Hook class [%s] must implement interface [%s]");

	public final static ServerErrorLocal HOOK_STATE_SCOPE_VIOLATION = 
			new ServerErrorLocal(425, Severity.ERROR, "User hook [%s], defined at state [%s] cannot listen to life cycle event [%s].");
	
	public final static ServerErrorLocal HOOK_TEST_SCOPE_VIOLATION = 
			new ServerErrorLocal(426, Severity.ERROR, "User hook [%s], defined at test [%s] cannot listen to life cycle event [%s].");

	public final static ServerErrorLocal FLUSHER_CLASS_NO_INTERFACE = 
			new ServerErrorLocal(427, Severity.ERROR, "Event flusher class [%s] must implement interface [%s]");

	//
	// 461-480 Server API
	//

	public final static ServerErrorLocal HOOK_TARGETING_BAD_EXPERIENCE = 
			new ServerErrorLocal(461, Severity.ERROR, "Targeting hook [%s] for test [%s] cannot set experience [%s]");


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

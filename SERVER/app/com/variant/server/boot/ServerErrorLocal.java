package com.variant.server.boot;

import com.variant.core.exception.CommonError;

/**
 * Local Server Errors, i.e. ones thrown to the server log and not sent back to client.
 * These are raised by UserErrorException.
 */
public class ServerErrorLocal extends CommonError {

//	public final static ServerError UNEXPECTED_FATAL_ERROR = 
//			new ServerError(Severity.FATAL, "Unexpected FATAL error: [%s]. See application log for details");

	//
	// 401-420 Server bootstrap
	//
	public final static ServerErrorLocal SCHEMAS_DIR_MISSING = 
			new ServerErrorLocal(401, Severity.FATAL, "Schema deploy directory [%s] does not exist");

	public final static ServerErrorLocal SCHEMAS_DIR_NOT_DIR = 
			new ServerErrorLocal(402, Severity.FATAL, "Schema deploy directory file [%s] exists but is not a directory");
	
	public final static ServerErrorLocal EVENT_FLUSHER_NO_INTERFACE =
			new ServerErrorLocal(403, Severity.FATAL, "Event flusher class [%s] must implement interface [%s]"); 

	//
	// 421-440 Schema deployment
	//
	public final static ServerErrorLocal MULTIPLE_SCHEMAS_NOT_SUPPORTED = 
			new ServerErrorLocal(421, Severity.FATAL, "Schema deploy directory [%s] cannot contain multiple files");

	//
	// 461-480 Other server runtime
	//

	public final static ServerErrorLocal HOOK_TARGETING_BAD_EXPERIENCE = 
			new ServerErrorLocal(461, Severity.ERROR, "Targeting hook listener [%s] for test [%s] cannot set experience [%s]");

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
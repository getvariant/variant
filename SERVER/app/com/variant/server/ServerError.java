package com.variant.server;

import com.variant.core.exception.RuntimeError;

/**
 * 
 */
public class ServerError extends RuntimeError {

	public final static ServerError CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN = 
			new ServerError(Severity.FATAL, "Cannot pass both -Dvariant.config.resource and -Dvariant.config.file parameters");

	//CONFIG_RESOURCE_NOT_FOUND                        (Severity.FATAL, "Class path resource [%s] is not found"); 
	//CONFIG_FILE_NOT_FOUND                            (Severity.FATAL, "OS file [%s] is not found"); 
	
	public final static ServerError EVENT_FLUSHER_NO_INTERFACE =
         new ServerError(Severity.FATAL, "Event flusher class [%s] must implement interface [%s]"); 
	//SID_TRACKER_NO_INTERFACE                         (Severity.FATAL, "Session ID tracker class [%s] must implement interface [%s]");
	//SESSION_STORE_NO_INTERFACE                       (Severity.FATAL, "Session store class [%s] must implement interface [%s]");
	//SESSION_ID_TRACKER_NO_INTERFACE                  (Severity.FATAL, "Session ID tracker class [%s] must implement interface [%s]");
	//TARGETING_TRACKER_NO_INTERFACE                   (Severity.FATAL, "Targeting tracker class [%s] must implement interface [%s]");
	//PARSER_LISTENER_NO_INTERFACE                     (Severity.FATAL, "Class [%s], annotated as [%s] must implement interface [%s]");

   /**
    * 
    */
   protected ServerError(Severity severity, String format) {
		super(severity, format);
	}

}

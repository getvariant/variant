package com.variant.core.exception;

/**
 * The immutable, uncontextualized part of a system message.
 * 
 * @author Igor
 */
public class Error {
	
	//------------------------------------------------------------------------------------------------------------------------//
	//                                                BOOTSTRAP MESSAGES                                                      //
	//------------------------------------------------------------------------------------------------------------------------//
 
	public static final Error BOOT_CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN = 
			new Error(Severity.FATAL, "Cannot pass both -Dvariant.config.resource and -Dvariant.config.file parameters");

	// BOOT_CONFIG_RESOURCE_NOT_FOUND                        (Severity.FATAL, "Class path resource [%s] is not found"), 
	// BOOT_CONFIG_FILE_NOT_FOUND                            (Severity.FATAL, "OS file [%s] is not found"), 
	// BOOT_EVENT_FLUSHER_NO_INTERFACE                       (Severity.FATAL, "Event flusher class [%s] must implement interface [%s]"), 
	// BOOT_SID_TRACKER_NO_INTERFACE                         (Severity.FATAL, "Session ID tracker class [%s] must implement interface [%s]"),
	// BOOT_SESSION_STORE_NO_INTERFACE                       (Severity.FATAL, "Session store class [%s] must implement interface [%s]"),
	// BOOT_SESSION_ID_TRACKER_NO_INTERFACE                  (Severity.FATAL, "Session ID tracker class [%s] must implement interface [%s]"),
	// BOOT_TARGETING_TRACKER_NO_INTERFACE                   (Severity.FATAL, "Targeting tracker class [%s] must implement interface [%s]"),
	// BOOT_PARSER_LISTENER_NO_INTERFACE                     (Severity.FATAL, "Class [%s], annotated as [%s] must implement interface [%s]"),
	
	//------------------------------------------------------------------------------------------------------------------------//
	//                                                  PARSER MESSAGES                                                       //
	//------------------------------------------------------------------------------------------------------------------------//


	//------------------------------------------------------------------------------------------------------------------------//
	//                                                 RUN TIME MESSAGES                                                      //
	//------------------------------------------------------------------------------------------------------------------------//
	
	// RUN_PROPERTY_BAD_CLASS                                (Severity.ERROR, "Don't know how to convert to class [%s]"),
	// RUN_PROPERTY_INIT_INVALID_JSON                        (Severity.ERROR, "Invalid JSON [%s] in system property [%s]"),
	// RUN_PROPERTY_INIT_PROPERTY_NOT_SET                    (Severity.ERROR, "Init property [%s] is required by class [%s] but is missing in system property [%s]"),
	// RUN_ACTIVE_REQUEST                                    (Severity.ERROR, "Commit current state request first"),
	// RUN_SCHEMA_OBSOLETE                                   (Severity.ERROR, "Operation failed becuase this schema (ID [%s]) has been undeployed"),
	// RUN_SCHEMA_MODIFIED                                   (Severity.ERROR, "Opereation failed because the current schema ID [%s] differs from that with which this session was created [%s]"),
	// RUN_SCHEMA_UNDEFINED                                  (Severity.ERROR, "Cannot create a session on an idle Variant instance. Deploy a schema first"),
	// RUN_SESSION_EXPIRED                                   (Severity.ERROR, "This session has expired"), 
	// RUN_METHOD_UNSUPPORTED                                (Severity.ERROR, "Method unsupported in Core"),
	// RUN_STATE_UNDEFINED_IN_EXPERIENCE                     (Severity.ERROR, "Currently active experience [%s] is undefined on state [%s]"),
	// RUN_HOOK_TARGETING_BAD_EXPERIENCE                     (Severity.ERROR, "Targeting hook [%s] for test [%s] cannot set experience [%s]"),
	// RUN_HOOK_TARGETING_UNDEFINED_EXPERIENCE               (Severity.ERROR, "Targeting hook [%s] for test [%s] cannot set experience [%s], which is undefined on state [%s]"),
 
	//------------------------------------------------------------------------------------------------------------------------//
	//                                                  GENERAL MESSAGES                                                      //
	//------------------------------------------------------------------------------------------------------------------------//

	public static final Error  HOOK_LISTENER_EXCEPTION =
			new Error(Severity.ERROR, "User hook listener class [%s] threw an exception [%s]. See logs for details.");
	
	// INTERNAL                                              (Severity.FATAL, "Unexpectged error [%s]. See log for details.");

	//------------------------------------------------------------------------------------------------------------------------//

	public final Severity severity;
	public final String format;
	public final String code;
	
	protected Error(Severity severity, String format) {
		this.severity = severity;
		this.format = format;
		this.code = getClass().getSimpleName();
	}

	/**
	 * Severity of a {@link ParserMessage}.
	 * 
	 * @since 0.5
	 */
	public enum Severity {

		/**
		 * Information only message.
		 * @since 0.5
		 */
		INFO,
		/**
		 * Warning. Current operation will proceed.
		 * @since 0.5
		 */
		WARN,
		/**
		 * Error. If received at parse time, parser will proceed, but Variant will not deploy the schema.
		 * If received at run time, current operation will fail. 
		 * @since 0.5
		 */
		ERROR,
		/**
		 * Fatal Error. Current operation will fail.
		 * @since 0.5
		 */
		FATAL;

		/**
		 * Is other severity greater than this?
		 * @param other The other severity.
		 * @return True if other severity is greater than this.
		 * @since 0.5
		 */
		public boolean greaterThan(Severity other) {
			return ordinal() > other.ordinal();
		}

		/**
		 * Is other severity greater or equal than this?
		 * @param other The other severity.
		 * @return True if other severity is greater or equal to this.
		 * @since 0.5
		 */
		public boolean greaterOrEqualThan(Severity other) {
			return ordinal() >= other.ordinal();
		}

		/**
		 * Is other severity less than this?
		 * @param other The other severity.
		 * @return True if other severity is less than this.
		 * @since 0.5
		 */
		public boolean lessThan(Severity other) {
			return ordinal() < other.ordinal();
		}

		/**
		 * Is other severity less than this?
		 * @param other The other severity.
		 * @return True if other severity is less or equal to this.
		 * @since 0.5
		 */
		public boolean lessOrEqualThan(Severity other) {
			return ordinal() <= other.ordinal();
		}
	}

}

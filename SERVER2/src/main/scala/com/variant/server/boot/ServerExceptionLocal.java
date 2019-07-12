package com.variant.server.boot;

import com.variant.core.error.UserError;
import com.variant.core.error.UserError.Severity;
import com.variant.server.api.ServerException;

/**
 * Server local user exceptions. These are the result of an invalid user action committed over the server extension API.
 * 
 * @since 0.7
 */
@SuppressWarnings("serial")
public class ServerExceptionLocal extends ServerException {

	public final UserError error;
	public final Object[] args;
	
	/**
	 */
	public ServerExceptionLocal(UserError error, Object...args) {
		super();
		this.error = error;
		this.args = args;
	}

	/**
	 */
	public ServerExceptionLocal(UserError error, Throwable t, Object...args) {
		super(t);
		this.error = error;
		this.args = args;
	}

	/**
	 */
	public Severity getSeverity() {
		return error.getSeverity();
	}
	
	/**
	 */
	@Override
	public String getMessage() {
		return error.asMessage(args);
	}

}

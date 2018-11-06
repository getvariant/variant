package com.variant.server.boot;

import com.variant.core.UserError.Severity;
import com.variant.core.impl.CommonError;
import com.variant.server.api.ServerException;

/**
 * Server local user exceptions. These are the result of an invalid user action committed over the server extension API.
 * 
 * @since 0.7
 */
@SuppressWarnings("serial")
public class ServerExceptionLocal extends ServerException {

	public final CommonError error;
	public final String[] args;
	
	/**
	 */
	public ServerExceptionLocal(CommonError error, String...args) {
		super();
		this.error = error;
		this.args = args;
	}

	/**
	 */
	public ServerExceptionLocal(CommonError error, Throwable t, String...args) {
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
		return error.asMessage((Object[])args);
	}

}

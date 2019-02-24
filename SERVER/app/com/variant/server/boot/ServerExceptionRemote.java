package com.variant.server.boot;

import com.variant.core.error.UserError.Severity;
import com.variant.core.error.ServerError;
import com.variant.server.api.ServerException;

/**
 * Server remote user exceptions. These are the result of an invalid user action committed over a client API.
 * 
 * @since 0.7
 */
@SuppressWarnings("serial")
public class ServerExceptionRemote extends ServerException {

	public final ServerError error;
	public final String[] args;
	
	/**
	 * 
	 * @param template
	 * @param args
	 */
	public ServerExceptionRemote(ServerError error, String...args) {
		super();
		this.error = error;
		this.args = args;
	}

	/**
	 * Remote errors take severity of the underlying error.
	 * @return
	 */
	@Override
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

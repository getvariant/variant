package com.variant.server.api;

import com.variant.share.error.VariantException;
/**
 * The super-type for of Variant server exception. 
 * 
 * @author Igor Urisman
 * @since 0.7
 */
@SuppressWarnings("serial")
public class ServerException extends VariantException {

	public ServerException() {
		super();
	}
	
	public ServerException(String msg) {
		super(msg);
	}

	public ServerException(Throwable t) {
		super(t);
	}

	public ServerException(String msg, Throwable t) {
		super(msg, t);
	}

	/**
	 * The default severity level.
	 * @return
	 *
	public Severity getSeverity() {
		return Severity.ERROR;
	}
   */
}

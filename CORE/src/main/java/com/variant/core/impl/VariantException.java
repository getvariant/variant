package com.variant.core.impl;

/**
 * The super-type for all Variant exception. 
 * All Variant exceptions, server and client, inherit from here.
 * 
 * @author Igor Urisman
 * @since 0.5
 *
 */
@SuppressWarnings("serial")
public class VariantException extends RuntimeException {

	/**
	 * 
	 */
	public VariantException() {
		super();
	}
	
	/**
	 * 
	 */
	public VariantException(Throwable t) {
		super(t);
	}

	/**
	 * 
	 * @param msg
	 */
	public VariantException(String msg) {
		super(msg);
	}
	
	/**
	 * 
	 * @param msg
	 * @param t
	 */
	public VariantException(String msg, Throwable t) {
		super(msg, t);
	}

	/**
	 * An Internal exception, most likely a bug.
	 * 
	 * @since 0.9
	 */
	public static class Internal extends VariantException {
		
		public Internal(Throwable t) {
			super(t);
		}

		public Internal(String msg) {
			super(msg);
		}

		public Internal(String msg, Throwable t) {
			super(msg, t);
		}
		
		public Internal(CommonError error, Object...args) {
			super(error.asMessage(args));
		}

	}


}

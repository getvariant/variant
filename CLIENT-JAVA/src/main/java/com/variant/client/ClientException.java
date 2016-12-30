package com.variant.client;

/**
 * Superclass for all Variant client exceptions.
 * 
 * @author Igor Urisman
 * @since 0.7
 */
@SuppressWarnings("serial")
public class ClientException extends RuntimeException {

	private int code;
	private String comment;
	
	public ClientException(int code, String message, String comment) {
		super(message);
		this.code = code;
		this.comment = comment;
	}

	public int getCode() {
		return code;
	}
	
	public String getComment() {
		return comment;
	}

}

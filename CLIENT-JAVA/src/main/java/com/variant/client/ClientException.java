package com.variant.client;

public class ClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private int code;
	private String comment;
	
	/**
	 * 
	 * @param httpResponse
	 */
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

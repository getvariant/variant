package com.variant.client.impl;

import com.variant.client.ClientException;

@SuppressWarnings("serial")
public class ClientRemoteErrorException extends ClientException {

	private int code;
	private String comment;
	
	public ClientRemoteErrorException(int code, String message, String comment) {
		super("[" + code + "] " + message);
		this.code = code;
		this.comment = comment;
	}

	public ClientRemoteErrorException(int code, String message) {
		super("[" + code + "] " + message);
		this.code = code;
		this.comment = null;
	}

	public ClientRemoteErrorException(int code, String message, String comment, Throwable t) {
		super("[" + code + "] " + message, t);
		this.code = code;
		this.comment = comment;
	}

	public ClientRemoteErrorException(int code, String message, Throwable t) {
		super("[" + code + "] " + message, t);
		this.code = code;
		this.comment = null;
	}

	public int getCode() {
		return code;
	}
	
	public String getComment() {
		return comment;
	}

}

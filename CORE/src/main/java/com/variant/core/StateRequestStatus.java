package com.variant.core;

public enum StateRequestStatus {
	
	/**
	 * No host errors have been encountered.
	 */
	OK, 
	
	/**
	 * Host application has marked this state request as failed and it should be excluded from analysis.
	 */
	FAIL
}

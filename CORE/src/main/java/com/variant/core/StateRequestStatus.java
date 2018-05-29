package com.variant.core;

/**
 * The status of a state request. Host application can mark a state request as {@link #FAIL} in order to
 * exclude it from subsequent analysis.
 * 
 * @since 0.5
 */
public enum StateRequestStatus {
	
	/**
	 * No errors.
	 */
	OK, 
	
	/**
	 * Host application has marked this state request as failed and it should be excluded from analysis.
	 */
	FAIL
}

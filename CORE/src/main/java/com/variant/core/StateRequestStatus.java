package com.variant.core;

/**
 * State of a state request.
 * 
 * @since 0.9
 */

public enum StateRequestStatus {

	InProgress, Committed, Failed;

	/**
	 * Is a value one of the given values?
	 * 
	 * @param statuses
	 * @return ture if this value is one of the given values, false otherwise.
	 */
	public boolean isIn(StateRequestStatus... statuses) {
		
		for (StateRequestStatus s: statuses) 
			if (this == s) return true;
		return false;
	}
}

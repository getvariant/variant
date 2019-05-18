package com.variant.core.session;

/**
 * State of a state request.  We replicate this on both client and server,
 * so as not to have any public core classes other than schema. 
 * 
 * @since 0.9
 *

public enum StateRequestStatus {

	InProgress, Committed, Failed;

	/**
	 * Is a value one of the given values?
	 * 
	 * @param statuses
	 * @return ture if this value is one of the given values, false otherwise.
	 *
	public boolean isIn(StateRequestStatus... statuses) {
		
		for (StateRequestStatus s: statuses) 
			if (this == s) return true;
		return false;
	}
}
*/
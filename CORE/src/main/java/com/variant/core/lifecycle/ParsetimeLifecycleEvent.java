package com.variant.core.lifecycle;

import com.variant.core.error.UserError.Severity;


/**
 * <p>Super-type of all life-cycle event types that post their hooks at schema parse time.
 * 
 * @author Igor Urisman.
 * @since 0.8
 *
 */
public interface ParsetimeLifecycleEvent extends LifecycleEvent {
		
	/**
	 * Add a custom message to the parser response in progress.
	 * 
	 * @param severity Severity of the custom message. Error and above will
	 *        prevent this schema from being deployed.
	 * @param message Text of the custom message.
	 * @since 0.8
	 */
	void addMessage(Severity severity, String message);
}

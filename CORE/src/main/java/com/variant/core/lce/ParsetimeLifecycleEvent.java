package com.variant.core.lce;

import com.variant.core.UserError.Severity;


/**
 * <p>Super-interface for all life cycle event types that post their hooks at schema parse time.
 * 
 * @author Igor Urisman.
 * @since 0.8
 *
 */
public interface ParsetimeLifecycleEvent extends LifecycleEvent {
	
	/**
	 * The pending parser response.
	 * 
	 * @return Object of type {@link ParserResponse}
	 * @since 0.8
	 *
	ParserResponse getParserResponse();
	*/
	
	/**
	 * Add a custom message to the pending parser resonse.
	 * 
	 * @param severity Severity of the custom message. Error and above will
	 *        prevent this schema from being deployed.
	 * @param message Text of the custom message.
	 * @since 0.8
	 */
	void addMessage(Severity severity, String message);
}

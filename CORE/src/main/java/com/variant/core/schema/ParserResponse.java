package com.variant.core.schema;

import java.util.List;

import com.variant.core.schema.ParserMessage.Severity;

/**
 * Represents the outcome of the XDM schema parsing operation.
 * Returned by {@link com.variant.core.Variant#parseSchema(java.io.InputStream, boolean)}.
 * Host code can obtain information about the outcome of the parsing operation.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface ParserResponse {
	
	/**
	 * List of all parse messages in order they were emitted.
	 * @return A list of objects of type {@link ParserMessage}.
	 * @since 0.5
	 */
	public List<ParserMessage> getMessages();

	/**
	 * List of all parse messages of a given severity or higher, in order they were emitted.
	 * @return A list of objects of type {@link ParserMessage}.
	 * @since 0.5
	 */
	public List<ParserMessage> getMessages(Severity severity);

	/**
	 * Equivalent to <code>!{@link #getMessages()}.isEmpty()</code>.
	 * @return True there are messages of any severity, or false otherwise.
	 * @since 0.5
	 */
	public boolean hasMessages();

	/**
	 * Highest message severity.
	 * @return Highest {@link Severity} if there are messages, or null otherwise.
	 * @since 0.5
	 */
	public Severity highestMessageSeverity();
	
}

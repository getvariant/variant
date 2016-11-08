package com.variant.core.schema;

import java.util.List;

import com.variant.core.exception.Error.Severity;

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
	 * Equivalent to <code>!{@link #getMessages()}.isEmpty()</code>.
	 * @return True there are messages of any severity, or false otherwise.
	 * @since 0.5
	 */
	public boolean hasMessages();

	/**
	 * List of all parse messages of given severity or higher, in order they were emitted.
	 * @return A list of objects of type {@link ParserMessage}.
	 * @since 0.5
	 */
	public List<ParserMessage> getMessages(Severity severity);

	/**
	 * Equivalent to <code>!{@link #getMessages(severity)}.isEmpty()</code>.
	 * @return True there are messages of given severity or higher, false otherwise.
	 * @since 0.5
	 */
	public boolean hasMessages(Severity severity);

}

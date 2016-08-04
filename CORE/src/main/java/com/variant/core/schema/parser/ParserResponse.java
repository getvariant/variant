package com.variant.core.schema.parser;

import java.util.List;

import com.variant.core.hook.HookListener;
import com.variant.core.schema.parser.ParserMessage.Severity;

/**
 * Return type of {@link com.variant.core.Variant#parseSchema(java.io.InputStream, boolean)}.
 * Client code can obtain information about the outcome of the parse opeeration that generated
 * this object.
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

	/**
	 * Add a message to this response object. This may be useful to call from
	 * a {@link HookListener} to augment core parsing semantics.
	 * 
	 * @param severity The severity of the message as an instance of {@link Severity}.
	 * @param text The text of the error message.
	 * @since 0.5
	 */
	public void addMessage(Severity severity, String text);
	
}

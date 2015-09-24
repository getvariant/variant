package com.variant.core.schema.parser;

import java.util.List;

import com.variant.core.schema.Schema;

public interface ParserResponse {

	/**
	 * Get the current test schema.
	 * @param view
	 */
	public Schema getSchema();
	
	/**
	 * Equivalent to <code>!getMessages().isEmpty()</code>
	 * @return true there are messages of any severity, or false otherwise.
	 */
	public boolean hasMessages();

	/**
	 * All parse messages in order they were emitted, 
	 * regardless of their severity, as an unmodifiable list.
	 * @return
	 */
	public List<ParserMessage> getMessages();

	/**
	 * Add a message to this response object. Call this to add a message
	 * from a parser related flashpoint listener.
	 * 
	 * @param severity
	 * @param text
	 */
	public void addMessage(Severity severity, String text);
	
	/**
	 * All parse messages of a given severity or higher,
	 * in order they were emitted, as an unmodifiable list.
	 * @return
	 */
	public List<ParserMessage> getMessages(Severity severity);

	/**
	 * Highest message severity
	 * @return Highest severity if there are messages, or null otherwise.
	 */
	public Severity highestMessageSeverity();

}

package com.variant.core.schema;

import com.variant.core.UserError.Severity;

/**
 * Represents a message generated by Variant schema parser. These are made available to
 * host code via {@link ParserResponse#getMessages()}.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface ParserMessage {
	
	/**
	 * Severity of the message.
	 * 
	 * @return An object of type {@link Severity}.
	 * @since 0.5
	 */
	public Severity getSeverity();
	
	/**
	 * Text of the message.
	 * 
	 * @return The text of the message.
	 * @since 0.5
	 */
	public String getText();
	
	/**
	 * Internally assigned code of this message. For reference purposes only.
	 * 
	 * @return Code string.
	 * @since 0.5
	 */
	public int getCode();

	/**
	 * Line number in the source document where the syntax error was encountered.
	 * 
	 * @return Line number, if this is a syntax error, or null if this is a semantical error.
	 * @since 0.5
	 */
	public Integer getLine();
	
	/**
	 * Column number in the source document where the syntax error was encountered.
	 * 
	 * @return Column number if this is a syntax error, or null if this is a semantical error.
	 * @since 0.5
	 */
	public Integer getColumn();
	
}

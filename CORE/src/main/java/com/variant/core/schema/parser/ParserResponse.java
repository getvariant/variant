package com.variant.core.schema.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.variant.core.error.UserError;
import com.variant.core.error.UserError.Severity;
import com.variant.core.schema.Schema;
import com.variant.core.schema.impl.SchemaImpl;
import com.variant.core.schema.parser.error.SemanticError;
import com.variant.core.schema.parser.error.SyntaxError;

public class ParserResponse {

	private final SchemaParser parser; // The parser that created this response.
	
	private ArrayList<ParserMessage> messages = new ArrayList<ParserMessage>();
	
	// schema name will be set so long as we can parse it out.
	// we keep it separate from the schema because schema object will be invalidated
	// at the end of an unsuccessful parse, but we need the name for error messages
	private String schemaName = null;
	
	// schema and schema source will be blank if parser errors.
	private SchemaImpl schema = new SchemaImpl();
	private String schemaSrc = null;
	
	private MessageListener messageListener = null;
	
	/**
	 * Private common part of adding the message which posts the parser listener, if any.
	 * @param message
	 */
	private void addMessageCommon(ParserMessage message) {
		messages.add(message);
		if (messageListener != null) messageListener.messageAdded(message);
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	public static interface MessageListener {

		void messageAdded(ParserMessage message);
	}

	public ParserResponse(SchemaParser parser) {
		this.parser = parser;
	}
	
	/**
	 * 
	 * @return
	 */
	public SchemaParser getParser() {
		return parser;
	}
	
   /**
	 * @return
	 */
	public List<ParserMessage> getMessages() {
		return Collections.unmodifiableList(messages);
	}

	public boolean hasMessages() {
		return ! messages.isEmpty();
	}

	/**
	 * @return
	 */
	public List<ParserMessage> getMessages(Severity severity) {
		ArrayList<ParserMessage> response = new ArrayList<ParserMessage>();
		for (ParserMessage msg: messages) {
			if (msg.getSeverity().greaterOrEqual(severity)) response.add(msg);
		}
		return Collections.unmodifiableList(response);
	}

	public boolean hasMessages(Severity severity) {
		return ! getMessages(severity).isEmpty();
	}

	public String getSchemaName() {
		return schemaName;
	}

	public Schema getSchema() {
		return schema;
	}

	public String getSchemaSrc() {
		return schemaSrc;
	}

	/**
	 * Add a message generated externally by a parser time life-cycle hook.
	 *
    public void addMessage(Severity severity, String message) {
		ParserMessage result = new ParserMessageImpl(new ParserHookError(severity, message));
		addMessageCommon(result);
	}

	/**
	 * Add a syntax error.
	 */
	public ParserMessage addMessage(SyntaxError.Location location, SyntaxError error, String...args) {
		
		ParserMessage result = new ParserMessageImpl(location, error, args);
		addMessageCommon(result);
		return result;
	}

	/**
	 * Add a simantic error.
	 */
	public ParserMessage addMessage(SemanticError.Location location, SemanticError error, String...args) {
		
		ParserMessage result = new ParserMessageImpl(location, error, args);
		addMessageCommon(result);
		return result;
	}

	/**
	 * Runtime user errors which are not emitted by the parser,
	 * but are reported with the ParserResponse.
	 * @param error
	 */
	public ParserMessage addMessage(UserError error, String...args) {
		ParserMessage result = new ParserMessageImpl(error, args);
		addMessageCommon(result);
		return result;
	}

   /**
    * Same, plus a stack trace we don't want to lose.
    * @param error
    */
   public ParserMessage addMessage(UserError error, Exception e, String...args) {
      ParserMessage result = new ParserMessageImpl(error, e, args);
      addMessageCommon(result);
      return result;
   }

   /**
	 * 
	 * @param schemaName
	 */
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	/**
	 * 
	 * @param schemaSrc
	 */
	public void setSchemaSrc(String schemaSrc) {
		this.schemaSrc = schemaSrc;
	}
	
	/**
	 * Clear the schema in progress, at the end of an unsuccessful parse.
	 */
	public void clearSchema() {
		schema = null;
		schemaSrc = null;
	}

	/**
	 * 
	 * @param listener
	 */
	public void setMessageListener(MessageListener listener) {
		this.messageListener = listener;
	}
}

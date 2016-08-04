package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.variant.core.schema.Schema;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.ParserMessage.Severity;

public class ParserResponseImpl implements ParserResponse {

	private ArrayList<ParserMessage> messages = new ArrayList<ParserMessage>();
	private SchemaImpl schema = new SchemaImpl();
	
	public ParserResponseImpl() {}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public void addMessage(Severity severity, String message) {
		ParserMessage result = new ParserMessageImpl(severity, message);
		messages.add(result);
	}

	/**
	 * 
	 * @return Highest severity if there are any messages, or null otherwise.
	 */
	@Override
	public Severity highestMessageSeverity() {
		
		Severity result = null;
		for (ParserMessage error: messages) {
			if (result == null || result.lessThan(error.getSeverity()))
				result = error.getSeverity();					
		}
		return result;
	}
	
	/**
	 * @return
	 */
	@Override
	public List<ParserMessage> getMessages() {
		return Collections.unmodifiableList(messages);
	}

	/**
	 * @return
	 */
	@Override
	public List<ParserMessage> getMessages(Severity severity) {
		ArrayList<ParserMessage> response = new ArrayList<ParserMessage>();
		for (ParserMessage msg: messages) {
			if (msg.getSeverity().greaterOrEqualThan(severity)) response.add(msg);
		}
		return Collections.unmodifiableList(response);
	}

	@Override 
	public boolean hasMessages() {
		return ! messages.isEmpty();
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                    PUBLIC EXTENDED                                          //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Get the schema in progress, built by the current invocation of the parser.
	 * @param view
	 */
	public Schema getSchema() {
		return schema;
	}

	/**
	 * 
	 * @param error
	 */
	public ParserMessage addMessage(MessageTemplate template, int line, int column, String...args) {
		
		ParserMessage result;
		
		if (template.equals(MessageTemplate.PARSER_JSON_PARSE)) {
			result = new SyntaxError(template, line, column, args);
		}
		else {
			result = new ParserMessageImpl(template, line, column, args);
		}
		messages.add(result);
		return result;
	}
	
	/**
	 * 
	 * @param error
	 */
	public ParserMessage addMessage(MessageTemplate template, String...args) {
		ParserMessage result = new ParserMessageImpl(template, args);
		messages.add(result);
		return result;
	}

}

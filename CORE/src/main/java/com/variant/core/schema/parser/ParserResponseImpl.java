package com.variant.core.schema.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.variant.core.schema.parser.ParserError.*;

import com.variant.core.UserError;
import com.variant.core.UserError.Severity;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.Schema;
import com.variant.core.schema.impl.SchemaImpl;

public class ParserResponseImpl implements ParserResponse {

	private ArrayList<ParserMessage> messages = new ArrayList<ParserMessage>();
	private SchemaImpl schema = new SchemaImpl();
	private String schemaSrc = null;
	
	public ParserResponseImpl() {}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * @return
	 */
	@Override
	public List<ParserMessage> getMessages() {
		return Collections.unmodifiableList(messages);
	}

	@Override 
	public boolean hasMessages() {
		return ! messages.isEmpty();
	}

	/**
	 * @return
	 */
	@Override
	public List<ParserMessage> getMessages(Severity severity) {
		ArrayList<ParserMessage> response = new ArrayList<ParserMessage>();
		for (ParserMessage msg: messages) {
			if (msg.getSeverity().greaterOrEqual(severity)) response.add(msg);
		}
		return Collections.unmodifiableList(response);
	}

	@Override 
	public boolean hasMessages(Severity severity) {
		return ! getMessages(severity).isEmpty();
	}

	@Override
	public Schema getSchema() {
		return schema;
	}

	@Override
	public String getSchemaSrc() {
		return schemaSrc;
	}

	//---------------------------------------------------------------------------------------------//
	//                                    PUBLIC EXTENDED                                          //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Add externally generated message.
	 */
    public void addMessage(Severity severity, String message) {
		ParserMessage result = new ParserMessageImpl(severity, message);
		messages.add(result);
	}

	/**
	 * Add internally generated messsage
	 */
	public ParserMessage addMessage(ParserError error, int line, int column, String...args) {
		
		ParserMessage result;
		
		if (error.equals(JSON_PARSE)) {
			result = new SyntaxError(error, line, column, args);
		}
		else {
			result = new ParserMessageImpl(error, line, column, args);
		}
		messages.add(result);
		return result;
	}
	
	/**
	 * 
	 * @param error
	 */
	public ParserMessage addMessage(UserError error, String...args) {
		ParserMessage result = new ParserMessageImpl(error, args);
		messages.add(result);
		return result;
	}

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
}

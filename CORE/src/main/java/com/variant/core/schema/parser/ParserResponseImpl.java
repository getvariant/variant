package com.variant.core.schema.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.variant.core.schema.parser.ParserError.*;
import com.variant.core.exception.Error;
import com.variant.core.exception.Error.Severity;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.Schema;
import com.variant.core.schema.impl.SchemaImpl;

public class ParserResponseImpl implements ParserResponse {

	private ArrayList<ParserMessage> messages = new ArrayList<ParserMessage>();
	private SchemaImpl schema = new SchemaImpl();
	
	public ParserResponseImpl() {}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * CLEANUP 
	 * @return Highest severity if there are any messages, or null otherwise.
	 *
	@Override
	public Severity highestMessageSeverity() {
		
		Severity result = null;
		for (ParserMessage error: messages) {
			if (result == null || result.lessThan(error.getSeverity()))
				result = error.getSeverity();					
		}
		return result;
	}
	*/
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
			if (msg.getSeverity().greaterOrEqualThan(severity)) response.add(msg);
		}
		return Collections.unmodifiableList(response);
	}

	@Override 
	public boolean hasMessages(Severity severity) {
		return ! getMessages(severity).isEmpty();
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
	 * Add externally generated message.
	 */
    public void addMessage(Severity severity, String message) {
		ParserMessage result = new ParserMessageImpl(severity, message);
		messages.add(result);
	}

	/**
	 * Add internally generated messsage
	 */
	public ParserMessage addMessage(Error error, int line, int column, String...args) {
		
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
	public ParserMessage addMessage(Error error, String...args) {
		ParserMessage result = new ParserMessageImpl(error, args);
		messages.add(result);
		return result;
	}

}

package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.variant.core.ParserResponse;
import com.variant.core.error.ErrorTemplate;
import com.variant.core.error.ParserError;
import com.variant.core.error.Severity;
import com.variant.core.error.SyntaxError;

public class ParserResponseImpl implements ParserResponse {

	private ArrayList<ParserError> errors = new ArrayList<ParserError>();
	private SchemaImpl config = new SchemaImpl();
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Get the schema in progress, built by the current invocation of the parser.
	 * @param view
	 */
	@Override
	public SchemaImpl getSchema() {
		return config;
	}

	/**
	 * 
	 * @return Highest severity if there are errors or null otherwise.
	 */
	@Override
	public Severity highestErrorSeverity() {
		
		Severity result = Severity.NONE;
		for (ParserError error: errors) {
			if (result.compareTo(error.getSeverity()) < 0)
				result = error.getSeverity();					
		}
		return result;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean hasErrors() {
		return errors.size() > 0;
	}
	
	/**
	 * All parse errors in order they were produced as an unmodifiable list.
	 * @return
	 */
	@Override
	public List<ParserError> getErrors() {
		return Collections.unmodifiableList(errors);
	}

	//---------------------------------------------------------------------------------------------//
	//                                    PUBLIC EXTENDED                                          //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @param error
	 */
	public void addError(ErrorTemplate template, int line, int column, String...args) {
		if (template.equals(ErrorTemplate.PARSER_JSON_PARSE)) {
			errors.add(new SyntaxError(template, line, column, args));
		}
		else {
			errors.add(new ParserError(template, line, column, args));
		}
	}
	
	/**
	 * 
	 * @param error
	 */
	public void addError(ErrorTemplate template, String...args) {
		errors.add(new ParserError(template, args));
	}

}

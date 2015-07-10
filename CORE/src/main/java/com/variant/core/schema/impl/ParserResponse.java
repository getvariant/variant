package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.variant.core.error.ErrorTemplate;
import com.variant.core.error.Severity;
import com.variant.core.schema.Schema;

public class ParserResponse {

	private ArrayList<ParserError> errors = new ArrayList<ParserError>();
	private SchemaImpl config = new SchemaImpl();
	
	/**
	 * 
	 * @param error
	 */
	void addError(ErrorTemplate template, int line, int column, String...args) {
		errors.add(new ParserError(template, line, column, args));
	}
	
	/**
	 * 
	 * @param error
	 */
	void addError(ErrorTemplate template, String...args) {
		errors.add(new ParserError(template, args));
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Get the schema in progress, built by the current invocation of the parser.
	 * @param view
	 */
	public Schema getSchema() {
		return config;
	}

	/**
	 * 
	 * @return Highest severity if there are errors or null otherwise.
	 */
	public Severity highestSeverity() {
		
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
	public boolean hasErrors() {
		return errors.size() > 0;
	}
	
	/**
	 * All parse errors in order they were produced as an unmodifiable list.
	 * @return
	 */
	public List<ParserError> getErrors() {
		return Collections.unmodifiableList(errors);
	}
	
}

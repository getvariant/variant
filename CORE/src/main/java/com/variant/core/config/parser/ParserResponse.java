package com.variant.core.config.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.variant.core.config.TestConfig;

public class ParserResponse {

	private ArrayList<ParserError> errors = new ArrayList<ParserError>();
	private ConfigImpl config = new ConfigImpl();
	
	/**
	 * 
	 * @param error
	 */
	void addError(ParserErrorTemplate template, int line, int column, String...args) {
		errors.add(new ParserError(template, line, column, args));
	}
	
	/**
	 * 
	 * @param error
	 */
	void addError(ParserErrorTemplate template, String...args) {
		errors.add(new ParserError(template, args));
	}

	/**
	 * Get the config in progress, built by the current invocation of the parser.
	 * @param view
	 */
	TestConfig getConfig() {
		return config;
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @return Highest severity if there are errors or null otherwise.
	 */
	public ParserError.Severity highestSeverity() {
		
		ParserError.Severity result = ParserError.Severity.NONE;
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

package com.variant.core.config.parser;

/**
 * Exposes package methods to tests.
 * 
 * @author Igor.
 *
 */
public class ParserErrorTestFacade extends ParserError {

	public ParserErrorTestFacade(ParserErrorTemplate template, String...args) {
		super(template, args);
	}
	
	ParserErrorTestFacade(ParserErrorTemplate template, int line, int column, String...args) {
		super(template, line, column, args);
	}

}

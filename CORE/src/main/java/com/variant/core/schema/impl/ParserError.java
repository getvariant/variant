package com.variant.core.schema.impl;

import com.variant.core.error.BaseError;
import com.variant.core.error.ErrorTemplate;

/**
 * Parser error.
 * 
 * @author Igor
 */
public class ParserError extends BaseError {
	
	private Integer line = null, column = null;

	/**
	 * 
	 * @param template
	 * @param args
	 */
	public ParserError(ErrorTemplate template, String...args) {
		super(template, args);
	}

	/**
	 * 
	 * @param template
	 * @param line
	 * @param column
	 * @param args
	 */
	public ParserError(ErrorTemplate template, int line, int column, String...args) {
		super(template, args);
		this.line = line;
		this.column = column;
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @return
	 */
	public Integer getLine() {
		return line;
	}
	
	/**
	 * 
	 * @return
	 */
	public Integer getColumn() {
		return column;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		return super.toString() + (line == null ? "" : String.format(" @ line %s, column %s", line, column));
	}

}

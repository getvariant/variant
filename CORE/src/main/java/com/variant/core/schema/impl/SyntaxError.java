package com.variant.core.schema.impl;

import org.apache.commons.lang3.StringUtils;

/**
 * JSON syntax error.
 * 
 * @author Igor
 */
public class SyntaxError extends ParserMessageImpl {
	
	String rawInput;
	
	/**
	 * Only use this for JSON suntax error.  Pass raw input as second arg.
	 * @param template
	 * @param line
	 * @param column
	 * @param args
	 */
	public SyntaxError(MessageTemplate template, int line, int column, String...args) {
		super(template, line, column, args);
		rawInput = args[1];
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//	
	/**
	 * For syntax error, add the offending line to the message along with the previous and the
	 * following lines.
	 */
	@Override
	public String toString() {
		
		String lines[] = rawInput.split("\n");
		
		StringBuilder result = new StringBuilder(super.toString()); 
		
		if (getLine() > 3) {
			result.append("\n     ...");
		}
		if (getLine() > 2) {
			result.append("\n     >").append(lines[getLine() - 3]);
		}
		if (getLine() > 1) {
			result.append("\n     >").append(lines[getLine() - 2]);
		}
		result.append("\n     >").append(lines[getLine() - 1]);
		result.append("\n      ").append(StringUtils.repeat(" ", getColumn() - 2)).append("^");
		if (getLine() < lines.length) {
			result.append("\n     >").append(lines[getLine()]);
		}
		if (getLine() < lines.length - 1) {
			result.append("\n     >").append(lines[getLine() + 1]);
		}
		if (getLine() < lines.length - 2) {
			result.append("\n     ...");
		}
		
		return result.toString();
	}

}

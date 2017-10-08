package com.variant.core.schema.parser;

import org.apache.commons.lang3.StringUtils;

import com.variant.core.schema.ParserMessage.Location;

/**
 * JSON syntax error.
 * 
 * @author Igor
 */
public class SyntaxErrorLocation implements Location {
	
	private  final String schemaSrc;
	
	public final int line, column;
	
	SyntaxErrorLocation(String schemaSrc, int line, int column) {
		this.schemaSrc = schemaSrc;
		this.line = line;
		this.column = column;
	}
	
	@Override
	public String asString() {
		return String.format("line [%s], column [%s]:\n%s", line, column, getSourceFragment());
	}
	
	public String getSourceFragment() {
		
		String lines[] = schemaSrc.split("\n");
		
		StringBuilder result = new StringBuilder(super.toString()); 
		
		if (line > 3) {
			result.append("\n     ...");
		}
		if (line > 2) {
			result.append("\n     >").append(lines[line - 3]);
		}
		if (line > 1) {
			result.append("\n     >").append(lines[line - 2]);
		}
		result.append("\n     >").append(lines[line - 1]);
		result.append("\n      ").append(StringUtils.repeat(" ", column - 2)).append("^");
		if (line < lines.length) {
			result.append("\n     >").append(lines[line]);
		}
		if (line < lines.length - 1) {
			result.append("\n     >").append(lines[line + 1]);
		}
		if (line < lines.length - 2) {
			result.append("\n     ...");
		}
		
		return result.toString();
	}

}

package com.variant.core.schema.parser.error;

import com.variant.core.util.VariantStringUtils;

public class SyntaxError extends ParserError {

	/**
	 * Constructor
	 */
	protected SyntaxError(int code, Severity severity, String format) {
		super(code, severity, format);
	}

	/**
	 * Sytanx error location implementation.
	 */
	public static class Location implements com.variant.core.schema.ParserMessage.Location {
		
		private  final String schemaSrc;
		
		public final int line, column;
		
		public Location(String schemaSrc, int line, int column) {
			this.schemaSrc = schemaSrc;
			this.line = line;
			this.column = column;
		}
		
		@Override
		public String getPath() {
			return null;
		}
		
		@Override
		public String toString() {
			return String.format("line [%s], column [%s]:\n%s", line, column, getSourceFragment());
		}

		@Override
		public boolean equals(Object other) {
			return (other instanceof Location)
					&& ((Location)other).toString().equals(toString());
		}

		private String getSourceFragment() {
			
			String lines[] = schemaSrc.split("\n");
			
			StringBuilder result = new StringBuilder(); 
			
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
			result.append("\n      ").append(VariantStringUtils.repeat(" ", column - 2)).append("^");
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

	// 
	// 171-180 Schema parser Syntax
	//
	public static final SyntaxError JSON_SYNTAX_ERROR =
			new SyntaxError(171, Severity.FATAL, "Invalid JSON syntax: [%s]");

}

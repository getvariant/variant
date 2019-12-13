package com.variant.share.schema.parser.error;

import com.variant.share.util.StringUtils;

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
	public static class Location implements com.variant.share.schema.parser.ParserMessage.Location {
		
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
			String format = "\n%" + String.valueOf(line + 1).length() + "d:%s";
			StringBuilder result = new StringBuilder(); 
			
			if (line > 3) {
				result.append("\n...");
			}
			if (line > 2) {
				result.append(String.format(format, line-2, lines[line - 3]));
			}
			if (line > 1) {
				result.append(String.format(format, line-1, lines[line - 2]));
			}
			result.append(String.format(format, line, lines[line - 1]));
			result.append("\n").append(StringUtils.repeat(" ", String.valueOf(line + 1).length() + column - 1)).append("^");
			if (line < lines.length) {
				result.append(String.format(format, line+1, lines[line]));
			}
			if (line < lines.length - 1) {
				result.append(String.format(format, line+2, lines[line + 1]));
			}
			if (line < lines.length - 2) {
				result.append("\n...");
			}
			
			return result.toString();
		}

	}

	// 
	// 171-180 Schema parser Syntax
	//
	public static final SyntaxError JSON_SYNTAX_ERROR =
			new SyntaxError(171, Severity.ERROR, "Invalid JSON syntax: [%s]");

}

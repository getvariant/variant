package com.variant.core.schema.parser;


/**
 * Parser Message.
 * 
 * @author Igor
 */
public class ParserMessage {
	
	private MessageTemplate template;
	private String[] args;
	private Integer line = null, column = null;

	/**
	 * 
	 * @param template
	 * @param args
	 */
	public ParserMessage(MessageTemplate template, String...args) {
		this.template = template;
		this.args = args;
	}

	/**
	 * 
	 * @param template
	 * @param line
	 * @param column
	 * @param args
	 */
	public ParserMessage(MessageTemplate template, int line, int column, String...args) {
		this(template, args);
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
	public Severity getSeverity() {
		return template.getSeverity();
	}

	/**
	 * 
	 * @return
	 */
	public String getMessage() {
		return String.format(template.getFormat(), (Object[]) args);
	}
	
	/**
	 * Error code
	 * @return
	 */
	public String getCode() {
		return template.toString();
	}

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
		StringBuilder result = new StringBuilder();
		result.append(getSeverity().name()).append(" - [").append(getCode()).append("] ").append(getMessage());
		if (line != null) result.append(String.format(" @ line %s, column %s", line, column));
		return result.toString();
	}

}

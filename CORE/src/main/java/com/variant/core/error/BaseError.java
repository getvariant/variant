package com.variant.core.error;

/**
 * Parser error.
 * 
 * @author Igor
 */
public class BaseError {
	
	private ErrorTemplate template;
	private String[] args;
	
	/**
	 * 
	 * @param template
	 * @param args
	 */
	public BaseError(ErrorTemplate template, String...args) {
		this.template = template;
		this.args = args;
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
	 */
	@Override
	public String toString() {
		return getSeverity().name() + " - [" + getCode() + "] "+ getMessage();
	}

}

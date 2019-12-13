package com.variant.share.schema.parser;

import com.variant.share.error.UserError;
import com.variant.share.error.UserError.Severity;
import com.variant.share.schema.parser.error.ParserError;

/**
 * ParserMessage implementation.
 * 
 * @author Igor Urisman
 */
public class ParserMessageImpl implements ParserMessage {
	
	private final Severity severity;
	private final String message;
	private final int code;	
	private final Exception exception;
	private Location location = null;

	/**
	 * Create parser message from a parser error.
	 */
	 public ParserMessageImpl(Location location, ParserError error, String...args) {
		severity = error.getSeverity();
		message = error.asMessage((Object[])args);
		code = error.getCode();
		exception = null;
		this.location = location;
	}

	/**
	 * Create parser message from a runtime error.  These are not emitted by the parser,
	 * but are reported with the ParserResponse.
	 */
	public ParserMessageImpl(UserError error, String...args) {
		severity = error.getSeverity();
		message = error.asMessage((Object[])args);
		code = error.getCode();
		exception = null;
	}

	/**
	 * Same as above, but with an exception we don't want to lose.
	 * @param error
	 * @param exception
	 * @param args
	 */
   public ParserMessageImpl(UserError error, Exception exception, String...args) {
      severity = error.getSeverity();
      message = error.asMessage((Object[])args);
      code = error.getCode();
      this.exception = exception;
   }

   //---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 */
	@Override
	public Severity getSeverity() {
		return severity;
	}

	/**
	 */
	@Override
	public String getText() {
		return message;
	}
	
	/**
	 */
	@Override
	public int getCode() {
		return code;
	}
	
	/**
	 */
	@Override
	public Location getLocation() {
		return location;
	}
	
   /**
    */
   @Override
   public Exception getException() {
      return exception;
   }

   //---------------------------------------------------------------------------------------------//
	//                                         PUBLIC EXT                                          //
	//---------------------------------------------------------------------------------------------//
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(getText());
		if (location != null) result.append("\nLocation: ").append(location);
		return result.toString();
	}

}

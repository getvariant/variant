package com.variant.core;

/**
 * <p>Represents a Variant user session.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface VariantSession {

	/**
	 * <p>Get this session's ID.
	 * 
	 * @return Session ID as a 128 bit binary number converted to hexadecimal representation.
	 * @since 0.5
	 */
	public String getId();
	
}

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
	
	/**
	 * <p>Get the current state request, which may be still in progress or already committed.
	 * 
	 * @return An object of type {@link VariantStateRequest}, or null, if none yet for this
	 *         session.
	 *  
	 * @since 0.5
	 */
	public VariantStateRequest getStateRequest();
}

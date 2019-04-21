package com.variant.client.impl;

import static com.variant.client.impl.ClientUserError.PARAM_CANNOT_BE_NULL;

import java.util.Map;

import com.variant.client.SessionAttributes;
import com.variant.client.VariantException;
import com.variant.client.impl.SessionImpl;
import com.variant.client.util.MethodTimingWrapper;

/**
 * A map-like collection of session attributes, returned by {@link Session#getAttributes()}. 
 * All methods, trigger a session refresh from server.
 *  
 * @since 0.9
 */
class SessionAttributesImpl implements SessionAttributes {

	private final SessionImpl ssn;

	/**
	 * Constructor.
	 */
	SessionAttributesImpl(SessionImpl ssn) {
		this.ssn = ssn;
	}

	/**
	 */
	@Override 
	public int size() {
		return ssn.getCoreSession().getAttributes().size();
	}

	/**
	 */
	@Override
	public String get(String name) {
		if (name == null) throw new VariantException(PARAM_CANNOT_BE_NULL, "name");
		return ssn.getCoreSession().getAttributes().get(name);
	}

	/**
	 * Add a parameter to this object.
	 * This change is reflected in both local and remote states.
	 * 
	 * @since 0.10
	 * @param name The attribute name to set.
	 * @param value The attribute value to set.
	 */
	@Override
	public void put(String name, String value) {

		if (name == null) throw new VariantException(PARAM_CANNOT_BE_NULL, "name");
		if (value == null) throw new VariantException(PARAM_CANNOT_BE_NULL, "value");
		
		ssn.preChecks();
		
		// We are not returning the replaced value for simplicity,
		// as the meaningful replaced value would be in the remote state.
		new MethodTimingWrapper<Map<String,String>>().exec( () -> {
			ssn.getCoreSession().getAttributes().put(name, value);
			ssn.getServer().sessionAttrMapSend(ssn);
			return null;
		});

	}

	/**
	 * Add a set of parameters to this object.
	 * This change is reflected in both local and remote states.
	 * 
	 * @since 0.10
	 * @param attributes A map of key/value pairs.
	 */
	@Override
	public void putAll(Map<String, String> attributes) {

		if (attributes == null) throw new VariantException(PARAM_CANNOT_BE_NULL, "attributes");
		
		ssn.preChecks();
		
		new MethodTimingWrapper<Map<String,String>>().exec( () -> {
			ssn.getCoreSession().getAttributes().putAll(attributes);
			ssn.getServer().sessionAttrMapSend(ssn);
			return null;
		});
	}

	/**
	 * Remove one or more attributes from the underlying session.
	 * This change is reflected in both local and remote states.
	 * 
	 * @since 0.10
	 * @param names The names of the attributes to be removed.
	 */
	@Override
	public void remove(String... names) {
		
		ssn.preChecks();
		if (names == null) throw new VariantException(PARAM_CANNOT_BE_NULL, "names");

		new MethodTimingWrapper<Map<String,String>>().exec( () -> {
			ssn.getServer().sessionAttrRemoveAll(ssn, names);
			return null;
		});
	}

}

package com.variant.client;

import java.util.Map;

/**
 * A map-like collection of session attributes, returned by {@link Session#getAttributes()}.
 * Attribute names and values are arbitrary non-null strings.
 *  
 * The state of this object reflects the shared state at the time of creation. All non-mutating methods are
 * local and may not reflect the shared state at the time of their invocation. If you need
 * to refresh from the shared state, get a new copy of this object with {@link Session#getAttributes()}.
 * 
 * Conversely, all mutating methods update the shared state on the server. By the time
 * mutating methods return, this object is refreshed with the current shared state.
 *  
 * @since 0.10
 */
public interface SessionAttributes {

	/**
	 * The number of session attributes tracked by this object. Does not refresh from the server.
	 * 
	 * @since 0.10
	 * @return The number of session attributes tracked by this object.
	 */
	int size();

	/**
	 * Retrieve the string value, currently associated with the given key. 
	 * Does not refresh from the server.
	 * 
	 * @since 0.10
	 * @param name The attribute name.
	 * @return The attribute's value, or null if an attribute with the given name does not exist.
	 */
	String get(String name);

	/**
	 * Add a session attribute to the underlying session. If the attribute by
	 * the given name already exists its value is replaced.
	 * This operation updates the shared state on the server. By the time it returns,
     * this object is refreshed with the current shared state.
	 * 
	 * @since 0.10
	 * @param name The attribute name to set. Cannot be null.
	 * @param value The attribute value to set. Cannot be null.
	 */
	void put(String name, String value);

	/**
	 * Add multiple session attributes to the underlying session, possibly replacing values
	 * of those attributes which already exist.
	 * This operation updates the shared state on the server. By the time it returns,
     * this object is refreshed with the current shared state.
	 * 
	 * @since 0.10
	 * @param attributes A map of name/value pairs.  Neither names nor values can be nulls.
	 */
	public void putAll(Map<String, String> attributes);

	/**
	 * Remove one or more attributes from the underlying session.
	 * This operation updates the shared state on the server. By the time it returns,
     * this object is refreshed with the current shared state.
	 * 
	 * @since 0.10
	 * @param names The names of the attributes to be removed.
	 */
	public void remove(String... names);

}

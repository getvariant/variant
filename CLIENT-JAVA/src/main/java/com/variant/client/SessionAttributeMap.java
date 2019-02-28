package com.variant.client;

import static com.variant.client.impl.ClientUserError.PARAM_CANNOT_BE_NULL;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.variant.client.impl.SessionImpl;
import com.variant.client.util.MethodTimingWrapper;

/**
 * A map of session attributes, returned by {@link Session#getAttributes()}. The initial
 * state of this object reflects the remote state. All methods, both read and update, 
 * are local and are not reflected on the server, until #synchronize() is called. 
 * 
 * Note that this class intentionally does not extend the {@code java.util.Map} interface,
 * to avoid its backward compatibility baggage. 
 * 
 * @since 0.9
 */
public class SessionAttributeMap {

	private SessionImpl ssn;

	/**
	 * Constructor.
	 * 
	 * @param ssn
	 * 
	 * @since 0.9
	 */
	public SessionAttributeMap(SessionImpl ssn) {
		this.ssn = ssn;
	}
	
	/**
	 * Send this entire object to the server.
	 * 
	 * @since 0.10
	 */
	public void synchronize() {
		ssn.preChecks();
		new MethodTimingWrapper<Object>().exec( () -> {
			ssn.getServer().sessionAttrMapSync(ssn, this);
			return null;
		});
	}
	
	/**
	 * The size of this map.
	 * 
	 * @see java.util.Map#size()
	 */
	public int size() {
		return ssn.getCoreSession().getAttributes().size();
	}

	/**
	 * Is this map empty?
	 * 
	 * @since 0.9
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		ssn.preChecks();
		return new MethodTimingWrapper<Boolean>().exec( () -> {
			ssn.refreshFromServer();
			return ssn.getCoreSession().getAttributes().isEmpty();
		});
	}

	/**
	 * Does this map contain a given key?
	 * 
	 * @since 0.9
	 * @see java.util.Map#containsKey(Object)
	 */
	public boolean containsKey(String key) {
		return ssn.getCoreSession().getAttributes().containsKey(key);
	}

	/**
	 * Does this map contain a given value?
	 * 
	 * @since 0.9
	 * @see java.util.Map#containsValue(Object)
	 */
	public boolean containsValue(Object value) {
		if (value == null) throw new VariantException(PARAM_CANNOT_BE_NULL, "value");
		return ssn.getCoreSession().getAttributes().containsValue(value);
	}

	/**
	 * Retrieve the string value currently associated with the given key.
	 * 
	 * @since 0.9
	 * @see java.util.Map#get(Object)
	 */
	public String get(String key) {
		if (key == null) throw new VariantException(PARAM_CANNOT_BE_NULL, "key");
		return ssn.getCoreSession().getAttributes().get(key);
	}

	/**
	 * Associate given string value with a given string key.
	 * 
	 * @since 0.9
	 * @see java.util.Map#put(Object, Object)
	 */
	public String put(String key, String value) {
		if (key == null) throw new VariantException(PARAM_CANNOT_BE_NULL, "key");
		if (value == null) throw new VariantException(PARAM_CANNOT_BE_NULL, "value");
		return ssn.getCoreSession().getAttributes().put(key, value);
	}

	/**
	 * Remove a given key from this map.
	 * 
	 * @since 0.9
	 * @see java.util.Map#remove(Object)
	 */
	public String remove(String key) {
		if (key == null) throw new VariantException(PARAM_CANNOT_BE_NULL, "key");
		return ssn.getCoreSession().getAttributes().remove(key);
	}

	/**
	 * All all given key/value pairs to this map.
	 * 
	 * @since 0.9
	 * @see java.util.Map#putAll(Map)
	 */
	public void putAll(Map<String, String> map) {
		ssn.getCoreSession().getAttributes().putAll(map);
	}

	/**
	 * Remove all entries from this map.
	 * 
	 * @since 0.9
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		ssn.getCoreSession().getAttributes().clear();
	}

	/**
	 * A set of all keys in this map.
	 * 
	 * @since 0.9
	 * @see java.util.Map#keySet()
	 */
	public Set<String> keySet() {
		return ssn.getCoreSession().getAttributes().keySet();
	}

	/**
	 * A collection of all values in this map.
	 * 
	 * @since 0.9
	 * @see java.util.Map#values()
	 */
	public Collection<String> values() {
		return ssn.getCoreSession().getAttributes().values();
	}

	/**
	 * A set of entries.
	 * 
	 * @since 0.9
	 * @see java.util.Map#entrySet()
	 */
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		return ssn.getCoreSession().getAttributes().entrySet();
	}

	
	
}

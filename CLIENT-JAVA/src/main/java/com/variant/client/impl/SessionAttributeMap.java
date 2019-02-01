package com.variant.client.impl;

import static com.variant.client.impl.ClientUserError.PARAM_CANNOT_BE_NULL;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.variant.client.Session;
import com.variant.client.VariantException;
import com.variant.client.util.MethodTimingWrapper;

/**
 * Client side replica of a distributed attribute map that stays in sync with
 * remote shared state.
 */
public class SessionAttributeMap implements Map<String, String> {

	private SessionImpl ssn;

	public SessionAttributeMap(SessionImpl ssn) {
		this.ssn = ssn;
	}
	
	@Override
	public int size() {
		return ssn.getCoreSession().getAttributes().size();
	}

	@Override
	public boolean isEmpty() {
		return ssn.getCoreSession().getAttributes().isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return ssn.getCoreSession().getAttributes().containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return ssn.getCoreSession().getAttributes().containsValue(value);
	}

	@Override
	public String get(Object key) {
		if (key == null) throw new VariantException(PARAM_CANNOT_BE_NULL, "key");
		ssn.preChecks();
		return new MethodTimingWrapper<String>().exec( () -> {
			return ssn.getCoreSession().getAttributes().get(key);
		});
	}

	@Override
	public String put(String key, String value) {
		if (key == null) throw new VariantException(PARAM_CANNOT_BE_NULL, "key");
		if (value == null) throw new VariantException(PARAM_CANNOT_BE_NULL, "value");
		ssn.preChecks();
		return new MethodTimingWrapper<String>().exec( () -> {
			ssn.getServer().sessionAttrSet(ssn, key, value);
			return ssn.getCoreSession().getAttributes().put(key, value);
		});
	}

	@Override
	public String remove(Object key) {
		if (key == null) throw new VariantException(PARAM_CANNOT_BE_NULL, "key");
		ssn.preChecks();
		return new MethodTimingWrapper<String>().exec( () -> {
			ssn.getServer().sessionAttrClear(ssn, key.toString());
			return ssn.getCoreSession().getAttributes().remove(key);
		});
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		
		new MethodTimingWrapper<String>().exec( () -> {
			for (Map.Entry<? extends String, ? extends String> e: m.entrySet()) {
				put(e.getKey(), e.getValue());
			}
			return null;
		});
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
	}

	@Override
	public Set<String> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}

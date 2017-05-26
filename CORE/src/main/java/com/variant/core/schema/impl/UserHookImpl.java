package com.variant.core.schema.impl;

import com.variant.core.schema.UserHook;

/**
 * 
 * @author Igor
 *
 */
public class UserHookImpl implements UserHook {

	private final String name;
	private final String className;
	
	public UserHookImpl(String name, String className) {
		this.name = name;
		this.className = className;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getClsName() {
		return className;
	}

	@Override
	public boolean equals(Object other) {
		return (other instanceof UserHookImpl) && ((UserHookImpl)other).name.equals(this.name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}

}

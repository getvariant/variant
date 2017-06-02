package com.variant.core.schema.impl;

import com.variant.core.schema.Hook;

/**
 * 
 * @author Igor
 *
 */
public class HookImpl implements Hook {

	private final String name;
	private final String className;
	
	public HookImpl(String name, String className) {
		this.name = name;
		this.className = className;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getClassName() {
		return className;
	}

	/**
	 * Hook names are unique within a schema. 
	 */
	@Override
	public boolean equals(Object other) {
		return (other instanceof HookImpl) && ((HookImpl)other).name.equals(this.name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}

}

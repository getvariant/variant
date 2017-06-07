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
	private final String init;
	private final Domain domain;
	
	public HookImpl(Domain domain, String name, String className, String init) {
		this.name = name;
		this.className = className;
		this.init = init;
		this.domain = domain;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public String getInit() {
		return init;
	}

	@Override
	public Domain getDomain() {
		return domain;
	}

	/**
	 * Hook names are unique within a domain. 
	 */
	@Override
	public boolean equals(Object other) {
		return (other instanceof HookImpl) && 
				((HookImpl)other).name.equals(this.name) &&
				((HookImpl)other).domain.equals(this.domain);
	}
	
	@Override
	public int hashCode() {
		return (domain.toString() + name).hashCode();
	}

}

package com.variant.core.schema.impl;

import com.variant.core.schema.Hook;

/**
 * 
 * @author Igor
 *
 */
public class SchemaHookImpl extends BaseHookImpl implements Hook.Schema {
	
	public SchemaHookImpl(String name, String className, String init) {
		super(name, className, init);
	}

	/**
	 * Schema hook names must be globally unique. 
	 */
	@Override
	public boolean equals(Object other) {
		return (other instanceof SchemaHookImpl) && 
				((SchemaHookImpl)other).name.equals(this.name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}

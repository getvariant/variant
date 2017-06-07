package com.variant.core.schema.impl;

import com.variant.core.schema.Hook;

/**
 * 
 * @author Igor
 *
 */
public class SchemaHookImpl implements Hook.Schema {

	private final String name;
	private final String className;
	private final String init;
	
	public SchemaHookImpl(String name, String className, String init) {
		this.name = name;
		this.className = className;
		this.init = init;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

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

	/**
	 * Hook names are unique within a domain. 
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

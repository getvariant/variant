package com.variant.core.schema.impl;

import com.variant.core.schema.Hook;

/**
 * 
 * @author Igor
 *
 */
abstract class BaseHookImpl implements Hook {

	protected final String name;
    protected final String className;
	protected final String init;
	
	public BaseHookImpl(String name, String className, String init) {
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

}

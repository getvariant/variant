package com.variant.core.schema.impl;

import com.variant.core.schema.Hook;

/**
 * 
 * @author Igor
 *
 */
abstract class BaseHookImpl implements Hook {

    protected final String className;
	protected final String init;
	
	public BaseHookImpl(String className, String init) {
		this.className = className;
		this.init = init;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public String getInit() {
		return init;
	}

}

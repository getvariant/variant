package com.variant.core.schema.impl;

import com.variant.core.schema.Hook;

/**
 * 
 * @author Igor
 *
 */
public class TestHookImpl extends SchemaHookImpl implements Hook.Test {

	private final com.variant.core.schema.Test test;
	
	public TestHookImpl(String name, String className, String init, com.variant.core.schema.Test test) {
		super(name, className, init);
		this.test = test;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public com.variant.core.schema.Test getTest() {
		return test;
	}

}

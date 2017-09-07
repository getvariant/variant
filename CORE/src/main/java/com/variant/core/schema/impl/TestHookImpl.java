package com.variant.core.schema.impl;

import com.variant.core.schema.Hook;

/**
 * 
 * @author Igor
 *
 */
public class TestHookImpl extends BaseHookImpl implements Hook.Test {

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

	/**
	 * Test hook names must be unique within the scope of the test. 
	 */
	@Override
	public boolean equals(Object other) {
		return (other instanceof TestHookImpl) && 
				((TestHookImpl)other).name.equals(name) &&
				((TestHookImpl)other).test.equals(test);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode() + test.hashCode();
	}

}

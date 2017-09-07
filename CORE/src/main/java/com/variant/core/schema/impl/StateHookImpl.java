package com.variant.core.schema.impl;

import com.variant.core.schema.Hook;

/**
 * 
 * @author Igor
 *
 */
public class StateHookImpl extends BaseHookImpl implements Hook.State {

	private final com.variant.core.schema.State state;
	
	public StateHookImpl(String name, String className, String init, com.variant.core.schema.State state) {
		super(name, className, init);
		this.state = state;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public com.variant.core.schema.State getState() {
		return state;
	}

	/**
	 * Test hook names must be unique within the scope of the test. 
	 */
	@Override
	public boolean equals(Object other) {
		return (other instanceof StateHookImpl) && 
				((StateHookImpl)other).name.equals(name) &&
				((StateHookImpl)other).state.equals(state);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode() + state.hashCode();
	}

}

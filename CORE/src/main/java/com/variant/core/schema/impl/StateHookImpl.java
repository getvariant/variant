package com.variant.core.schema.impl;

import com.variant.core.schema.Hook;

/**
 * 
 * @author Igor
 *
 */
public class StateHookImpl extends BaseHookImpl implements Hook.State {

	private final com.variant.core.schema.State state;
	
	public StateHookImpl(String className, String init, com.variant.core.schema.State state) {
		super(className, init);
		this.state = state;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public com.variant.core.schema.State getState() {
		return state;
	}


}

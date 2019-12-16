package com.variant.share.schema.impl;

import java.util.Optional;

import com.variant.share.schema.State;
import com.variant.share.schema.StateScopedHook;
import com.variant.share.schema.parser.error.SemanticError.Location;

/**
 * 
 * @author Igor
 *
 */
public class StateScopedHookImpl extends BaseHookImpl implements StateScopedHook {

	private final com.variant.share.schema.State state;
	
	public StateScopedHookImpl(String className, Optional<String> init, Location location, com.variant.share.schema.State state) {
		super(className, init, location);
		this.state = state;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public State getState() {
		return state;
	}


}

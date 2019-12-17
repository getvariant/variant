package com.variant.share.schema.impl;

import java.util.Optional;

import com.variant.share.schema.Hook;
import com.variant.share.schema.parser.error.SemanticError.Location;

/**
 * 
 * @author Igor
 *
 */
public abstract class BaseHookImpl implements Hook {

	public final Location location;

	protected final String className;
	protected final Optional<String> init;
	
	public BaseHookImpl(String className, Optional<String> init, Location location) {
		this.className = className;
		this.init = init;
		this.location = location;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public Optional<String> getInit() {
		return init;
	}

}

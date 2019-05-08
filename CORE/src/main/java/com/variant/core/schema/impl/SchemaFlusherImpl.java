package com.variant.core.schema.impl;

import java.util.Optional;

import com.variant.core.schema.Flusher;

/**
 * 
 * @author Igor
 *
 */
public class SchemaFlusherImpl implements Flusher {

	private final String className;
	private final Optional<String> init;
	
	public SchemaFlusherImpl(String className, Optional<String> init) {
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
	public Optional<String> getInit() {
		return init;
	}

}

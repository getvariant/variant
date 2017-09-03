package com.variant.core.schema.impl;

import com.variant.core.schema.Flusher;

/**
 * 
 * @author Igor
 *
 */
public class SchemaFlusherImpl implements Flusher {

	private final String className;
	private final String init;
	
	public SchemaFlusherImpl(String className, String init) {
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

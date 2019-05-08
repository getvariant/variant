package com.variant.core.schema.impl;

import java.util.Optional;

import com.variant.core.schema.Hook;
import com.variant.core.schema.parser.error.SemanticError.Location;

/**
 * 
 * @author Igor
 *
 */
public class SchemaHookImpl extends BaseHookImpl implements Hook.Schema {
	
	public SchemaHookImpl(String className, Optional<String> init, Location location) {
		super(className, init, location);
	}

}

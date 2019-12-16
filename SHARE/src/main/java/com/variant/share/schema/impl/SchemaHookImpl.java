package com.variant.share.schema.impl;

import java.util.Optional;

import com.variant.share.schema.MetaScopedHook;
import com.variant.share.schema.parser.error.SemanticError.Location;

/**
 * 
 * @author Igor
 *
 */
public class SchemaHookImpl extends BaseHookImpl implements MetaScopedHook {
	
	public SchemaHookImpl(String className, Optional<String> init, Location location) {
		super(className, init, location);
	}

}

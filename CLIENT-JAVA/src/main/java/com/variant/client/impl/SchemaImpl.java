package com.variant.client.impl;

import java.util.List;

import com.variant.core.schema.Meta;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;

/**
 * Client side schema.
 * Adds schema ID to the core functionality.
 */
public class SchemaImpl implements Schema {

	private final Schema coreSchema;
	private final String id;
	
	/**
	 */
	public SchemaImpl(String id, Schema coreSchema) {
		this.coreSchema = coreSchema;
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

	@Override
	public Meta getMeta() {
		return coreSchema.getMeta();
	}

	@Override
	public List<State> getStates() {
		return coreSchema.getStates();
	}

	@Override
	public State getState(String name) {
		return coreSchema.getState(name);
	}

	@Override
	public List<Test> getTests() {
		return coreSchema.getTests();
	}

	@Override
	public Test getTest(String name) {
		return coreSchema.getTest(name);
	}

}

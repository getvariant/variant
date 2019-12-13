package com.variant.client.impl;

import java.util.List;
import java.util.Optional;

import com.variant.share.schema.Meta;
import com.variant.share.schema.Schema;
import com.variant.share.schema.State;
import com.variant.share.schema.Variation;

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
	public Optional<State> getState(String name) {
		return coreSchema.getState(name);
	}

	@Override
	public List<Variation> getVariations() {
		return coreSchema.getVariations();
	}

	@Override
	public Optional<Variation> getVariation(String name) {
		return coreSchema.getVariation(name);
	}

}

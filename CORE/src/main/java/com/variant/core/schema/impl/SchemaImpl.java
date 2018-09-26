package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import com.variant.core.schema.Meta;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Variation;

/**
 * @author Igor
 */
public class SchemaImpl implements Schema {

	// The meta section
	private MetaImpl meta;
	
	// Views are keyed by name
	private LinkedHashSet<State> states = new LinkedHashSet<State>();
	
	// Tests are keyed by name
	private LinkedHashSet<Variation> vars = new LinkedHashSet<Variation>();
		
	/**
	 */
	public SchemaImpl() {}
					
    //---------------------------------------------------------------------------------------------//
	//                                    PUBLIC INTERFACE                                         //
	//---------------------------------------------------------------------------------------------//	
	
	/**
	 */
	@Override
	public Meta getMeta() {
		return meta;
	}

	/**
	 */
	@Override
	public List<State> getStates() {
		ArrayList<State> result = new ArrayList<State>(states.size());
		result.addAll(states);
		return Collections.unmodifiableList(result);
	}

	@Override
	public Optional<State> getState(String name) {
		for (State state: states) {
			if (state.getName().equals(name)) return Optional.of(state);
		}
		return Optional.empty();
	}

	/**
	 */
	@Override
	public List<Variation> getVariations() {
		ArrayList<Variation> result = new ArrayList<Variation>(vars.size());
		for (Variation var: vars) {
			result.add(var);
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 */
	@Override
	public Optional<Variation> getVariation(String name) {
		for (Variation var: vars) if (var.getName().equals(name)) return Optional.of(var);
		return Optional.empty();
	}
	
    //---------------------------------------------------------------------------------------------//
	//                                       PUBLIC EXT                                            //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Set schema's meta data.
	 * @param name
	 * @param comment
	 */
	public void setMeta(MetaImpl meta) {
		this.meta = meta;
	}
	
	/**
	 * Add state to this schema.
	 * @param state
	 * @return true if state didn't exist, false if did.
	 */
	public boolean addState(State state) {
		return states.add(state);
	}

	/**
	 * Add test to this schema
	 * @param test
	 * @return true if test didn't exist, false if did.
	 */
	public boolean addVariation(Variation test) {
		return vars.add(test);
	}

}

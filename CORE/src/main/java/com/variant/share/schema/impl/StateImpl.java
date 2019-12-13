package com.variant.share.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.variant.share.schema.Schema;
import com.variant.share.schema.State;
import com.variant.share.schema.StateScopedHook;
import com.variant.share.schema.Variation;

/**
 * 
 * @author Igor
 *
 */
public class StateImpl implements State {

	private final Schema schema;
	private final String name;
	private Optional<Map<String, String>> paramMapOpt = Optional.empty();    
	private Optional<List<StateScopedHook>> hooks = Optional.empty();

	/**
	 * Package scoped constructor;
	 * @param name
	 * @param path
	 */
	public StateImpl(Schema schema, String name) {
		this.schema = schema;
		this.name = name;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public Schema getSchema() {
		return schema;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Optional<Map<String,String>> getParameters() {
		return paramMapOpt;
	}

	@Override
	public List<Variation> getInstrumentedVariations() {
		
		ArrayList<Variation> result = new ArrayList<Variation>();
		
		for (Variation var: schema.getVariations()) {
			for (Variation.OnState tov: var.getOnStates()) {
				if (tov.getState().equals(this)) result.add(var);
			}
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	public boolean isInstrumentedBy(Variation test) {

		for (Variation.OnState tov: test.getOnStates()) {
			if (tov.getState().equals(this)) return true;
		}
		return false;
	}		

	/**
	 * 
	 */
	@Override
	public Optional<List<StateScopedHook>> getHooks() {
		return hooks.map(hooks -> Collections.unmodifiableList(hooks));
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//
	/**
	 * 
	 * @param parameters
	 */
	public void setParameterMap(Map<String, String> params) {
		this.paramMapOpt = Optional.of(Collections.unmodifiableMap(params));
	}
		
	/**
	 * Add hooks, if were defined.
	 */
	public void setHooks(List<StateScopedHook> hooks) {
		this.hooks = Optional.of(hooks);
	}
	
	/**
	 * States are held in a HashSet, keyed by view name.
	 */
	@Override
	public boolean equals(Object other) {
		if (! (other instanceof State)) return false;
		return ((State) other).getName().equals(this.getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}
}

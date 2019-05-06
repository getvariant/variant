package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.variant.core.schema.Hook;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Variation;

/**
 * 
 * @author Igor
 *
 */
public class StateImpl implements State {

	private final Schema schema;
	private final String name;
	private final LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
    
	// Hooks are keyed by name.
	private LinkedList<Hook> hooks = new LinkedList<Hook>();

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
	public Map<String,String> getParameters() {
		return Collections.unmodifiableMap(parameters);
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
	public List<Hook> getHooks() {
		ArrayList<Hook> result = new ArrayList<Hook>(hooks.size());
		result.addAll(hooks);
		return Collections.unmodifiableList(result);
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//
	/**
	 * 
	 * @param parameters
	 */
	public void setParameterMap(Map<String, String> params) {
		this.parameters.putAll(params);
	}
		
	/**
	 * Add life-cycle hook to this test
	 * @param hook
	 * @return true if hook didn't exist, false if did.
	 */
	public void addHook(Hook hook) {
		hooks.add(hook);
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

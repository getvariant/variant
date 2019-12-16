package com.variant.share.schema.impl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.variant.share.schema.State;
import com.variant.share.schema.Variation;

// Remove public modifier is the result of exposing the server side
// constructor.
public class VariationExperienceImpl implements Variation.Experience  {

	private String name;
	private Variation var;
	private Number weight;
	private boolean isControl;
	private Set<State> phantomStates = new HashSet<State>();
	
	/**
	 * Instantiation.
	 * @param name
	 */
	public VariationExperienceImpl(String name, Number weight, boolean isControl) {
		this.name = name;
		this.weight = weight;
		this.isControl = isControl;
	}
		
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Variation getVariation() {
		return var;
	}

	@Override
	public boolean isControl() {
		return isControl;
	}

	@Override
	public Optional<Number> getWeight() {
		return Optional.ofNullable(weight);
	}
	
	@Override
	public boolean isPhantom(State state) {
		if (state == null) throw new NullPointerException("Null state");
		return phantomStates.contains(state);
	}
	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	public void setTest(Variation var) {
		this.var = var;
	}

	public void addPhantomState(State state) {
		phantomStates.add(state);
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof VariationExperienceImpl)) return false;
		VariationExperienceImpl other = (VariationExperienceImpl) o;
		return var.equals(other.var) && name.equals(other.name);
	}
	
	@Override
	public int hashCode() {
		return var.hashCode() + name.hashCode();
	}
	
	@Override
	public String toString() {
		return var.getName() + "." + name;
	}	
}

package com.variant.core.schema.impl;

import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.core.error.CoreException;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Variation;
import com.variant.core.schema.Variation.Experience;
import com.variant.core.schema.Variation.OnState;


public class StateVariantImpl implements StateVariant {

	private final Variation.OnState onState;
	private final Variation.Experience ownExp;
	private final List<Variation.Experience> conjointExps;
	private final Map<String,String> params;
	private boolean inferred = false;
	
	/**
	 * Explicit (provided in schema)
	 */
	public StateVariantImpl(
			Variation.OnState onState, 
			Variation.Experience ownExp, 
			List<Variation.Experience> conjointExps, 
			Map<String,String> params) {
		
		this.onState = onState;
		this.ownExp = ownExp;
		this.conjointExps = conjointExps;
		this.params = params;
	}

	/**
	 * Inferred (default, not provided in schema)
	 * No state parameters.
	 */
	public StateVariantImpl(
			Variation.OnState onState, 
			Variation.Experience ownExp, 
			List<Variation.Experience> conjointExps) {
		
		this(onState, ownExp, conjointExps, new HashMap<String,String>());
		this.inferred = true;
	}

	/**
	 * 
	 * @param experience
	 *
	void addConjointExperience(VariationExperienceImpl experience) {
		conjointExperiences.add(experience);
	}
	*/
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	
	/**
	 */
	@Override
	public OnState getOnState() {
		return onState;
	}

	/**
	 */
	@Override
	public State getState() {
		return onState.getState();
	}
	
	/**
	 */
	@Override
	public Variation getVariation() {
		return onState.getVariation();
	}

	/**
	 */
	@Override
	public Map<String,String> getParameters() {
		return Collections.unmodifiableMap(params);
	}

	@Override
	public List<Experience> getConjointExperiences() {
		return Collections.unmodifiableList(conjointExps);
	}

	@Override
	public boolean isProper() {
		return conjointExps.size() == 0;
	}
	
	/**
	 * 
	 */
	@Override
	public Experience getExperience() {
		return ownExp;
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//
	
	public boolean isInferred() {
		return inferred;
	}
	
	/**
	 */
	@Override
	public String toString() {
		try {
			StringWriter result = new StringWriter(2048);
			JsonGenerator jsonGen = new JsonFactory().createGenerator(result);
			jsonGen.writeStartObject();
			jsonGen.writeStringField("state", onState.getState().getName());
			jsonGen.writeStringField("test", onState.getVariation().getName());
			jsonGen.writeStringField("properExperience", getExperience().toString());
			jsonGen.writeArrayFieldStart("conjointExperiences");
			for(Experience exp: getConjointExperiences()) {
				jsonGen.writeString(exp.toString());
			}
			jsonGen.writeEndArray();
			jsonGen.writeArrayFieldStart("params");
			for (Map.Entry<String, String> e: getParameters().entrySet()) {
				jsonGen.writeStartObject();
				jsonGen.writeStringField(e.getKey(), e.getValue());
				jsonGen.writeEndObject();
			}
			jsonGen.writeEndArray();
			jsonGen.writeEndObject();
			jsonGen.flush();
			return result.toString();
		}
		catch (Exception e) {
			throw new CoreException.Internal("Unable to serialize object [" + getClass().getSimpleName() + "]", e);
		}

	}
}

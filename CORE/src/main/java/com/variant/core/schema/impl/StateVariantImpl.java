package com.variant.core.schema.impl;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.core.impl.CoreException;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Variation;
import com.variant.core.schema.Variation.Experience;
import com.variant.core.schema.Variation.OnState;


public class StateVariantImpl implements StateVariant {

	private VariationOnStateImpl onStateImpl;
	private VariationExperienceImpl ownExperience;
	private List<VariationExperienceImpl> conjointExperiences;
	private Map<String,String> params;
	
	/**
	 * @param onView
	 * @param experiences
	 * @param path
	 */
	public StateVariantImpl(VariationOnStateImpl onViewImpl, VariationExperienceImpl ownExperience, List<VariationExperienceImpl> conjointExperiences, Map<String,String> params) {
		this.onStateImpl = onViewImpl;
		this.ownExperience = ownExperience;
		this.conjointExperiences = conjointExperiences;
		this.params = params;
	}

	/**
	 * 
	 * @param experience
	 */
	void addConjointExperience(VariationExperienceImpl experience) {
		conjointExperiences.add(experience);
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	
	/**
	 */
	@Override
	public OnState getOnState() {
		return onStateImpl;
	}

	/**
	 */
	@Override
	public State getState() {
		return onStateImpl.getState();
	}
	
	/**
	 */
	@Override
	public Variation getTest() {
		return onStateImpl.getVariation();
	}

	/**
	 */
	@Override
	public Map<String,String> getParameters() {
		return Collections.unmodifiableMap(params);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Experience> getConjointExperiences() {
		if (conjointExperiences == null) return null;
		return (List<Experience>) (List<?>) Collections.unmodifiableList(conjointExperiences);
	}

	@Override
	public boolean isProper() {
		return conjointExperiences == null;
	}
	
	/**
	 * 
	 */
	@Override
	public Experience getExperience() {
		return ownExperience;
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//
	
	@Override
	public String toString() {
		try {
			StringWriter result = new StringWriter(2048);
			JsonGenerator jsonGen = new JsonFactory().createGenerator(result);
			jsonGen.writeStartObject();
			jsonGen.writeStringField("state", onStateImpl.getState().getName());
			jsonGen.writeStringField("test", onStateImpl.getVariation().getName());
			jsonGen.writeStringField("params", params.toString());
			jsonGen.writeEndObject();
			jsonGen.flush();
			return result.toString();
		}
		catch (Exception e) {
			throw new CoreException.Internal("Unable to serialize object [" + getClass().getSimpleName() + "]", e);
		}

	}
}

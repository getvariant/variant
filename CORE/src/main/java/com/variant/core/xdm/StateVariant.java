package com.variant.core.xdm;

import java.util.List;
import java.util.Map;

import com.variant.core.xdm.Test.Experience;
import com.variant.core.xdm.Test.OnState;


/**
 * Representation of a single cell of the variant matrix.
 * Corresponds to an element of the test/onStates/variants schema list.
 *
 * @author Igor Urisman
 * @since 0.5
 */
public interface StateVariant {
	
	/**
	 * The {@link OnState} object this variant belongs to.
	 * 
	 * @return An object of type {@link OnState}.
	 * @since 0.5
	 */
	public OnState getOnState();

	/**
	 * The state for which this variant is defined. Equivalent to {@link #getOnState()}.getState().
	 * 
	 * @return A object of type {@link State}
	 * @since 0.6
	 */
	public State getState();

	/**
	 * The test for which this variant is defined. Equivalent to {@link #getOnState()}.getTest().
	 * 
	 * @return A object of type {@link Test}
	 * @since 0.5
	 */
	public Test getTest();
	
	/**
	 * This variant's own test experience, i.e. for the test within whose definition this variant
	 * is defined.
	 * 
	 * @return An object of type {@link Test.Experience}.
	 * @since 0.5
	 */
	public Experience getExperience();

	/**
	 * The list of this variant's covariantly concurrent experiences, i.e. the ones defined in the 
	 * covariant tests clause of the test within whose definition this variant is defined.
	 * 
	 * @return A list of objects of type {@link Test.Experience}.
	 * @since 0.5
	 */
	public List<Experience> getCovariantExperiences();
				
	/**
	 * This variant's resolved state parameter map. Parameter resolution works as follows:
	 * First, all parameters from the corresponsing state
	 * 
	 * @return A map of state parameters.
	 * @since 0.5
	 */
	public Map<String,String> getParameterMap();

}	

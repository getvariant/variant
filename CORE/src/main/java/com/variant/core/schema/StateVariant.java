package com.variant.core.schema;

import java.util.List;
import java.util.Map;

import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.Test.OnState;


/**
 * Representation of a single cell of a variance matrix.
 * Corresponds to an element of the test/onStates/variants schema list.
 *
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
	 * The test for which this variant is defined. Equivalent to {@link #getOnState()}.getTest().
	 * 
	 * @return
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
	 * This variant's state parameter map.
	 * @return
	 * @since 0.5
	 */
	public Map<String,String> getParameterMap();

}	

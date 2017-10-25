package com.variant.core.schema;

import java.util.List;
import java.util.Map;

import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.Test.OnState;


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
	 * @return A list of objects of type {@link Test.Experience} or null if this test does not have
	 *         the <code>covariantRestRefs</code> clause;
	 *         
	 * @since 0.5
	 */
	public List<Experience> getCovariantExperiences();
				
	/**
	 * Is this a proper variant? In a non-coaviantly concurrent test, all variants are proper. If this test
	 * is covariantly-concurrent, i.e. has the <code>covariantTestRefs</code> clause, proper variants are those which
	 * do not have the <code>covariantExperienceRefs</code> clause. Same as <code>getCovariantExperiences() == null</code>.
	 * 
	 * @return true if this state variant does not have a <code>covariantTestRefs</code>, i.e. refers to
	 *         the containing test only.
	 *         
	 * @since 0.6
	 */
	public boolean isProper();

	/**
	 * This variant's declared state parameter.
	 * 
	 * @return Immutable map containing all parameters defined by this state variant.
	 * @since 0.5
	 */
	public Map<String, String> getParameters();

}	

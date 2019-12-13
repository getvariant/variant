package com.variant.share.schema;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.variant.share.schema.Variation.Experience;
import com.variant.share.schema.Variation.OnState;


/**
 * Representation of the <code>/variations[]/onState/variations[]</code> array element.
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
	OnState getOnState();

	/**
	 * The state for which this variant is defined. Equivalent to {@link #getOnState()}.getState().
	 * 
	 * @return A object of type {@link State}
	 * @since 0.6
	 */
	State getState();

	/**
	 * The variation for which this variant is defined. Equivalent to {@link #getOnState()}.getVariation().
	 * 
	 * @return A object of type {@link Variation}
	 * @since 0.5
	 */
	Variation getVariation();
		
	/**
	 * This variant's proper experience, i.e. the one from the containing variation.
	 * 
	 * @return An object of type {@link Variation.Experience}.
	 * @since 0.5
	 */
	Experience getExperience();
	
	/**
	 * The list of this variant's conjointly concurrent experiences, i.e. the ones defined in the 
	 * {@code conjointVariationRefs} clause of the containing variation.
	 * 
	 * @return A list of objects of type {@link Variation.Experience}, which will be empty
     *         if this variation does not have the <code>conjointVariationRefs</code> clause;
	 *         
	 * @since 0.5
	 */
	List<Experience> getConjointExperiences();
				
	/**
	 * Is this a proper variant? A shorthand for <code>getConjointExperiences().isEmpty()</code>.
	 * 
	 * @return true if this state variant does not have a <code>conjointVariationRefs</code>, i.e. refers to
	 *         the containing variation only.
	 *         
	 * @since 0.6
	 */
	boolean isProper();

	/**
	 * This variant's declared state parameter.
	 * 
	 * @return Immutable map containing all parameters defined by this state variant.
	 * @since 0.5
	 */
	Optional<Map<String, String>> getParameters();

}	

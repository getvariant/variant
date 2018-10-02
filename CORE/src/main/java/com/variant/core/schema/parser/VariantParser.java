package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.error.SemanticError.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.variant.core.schema.Variation;
import com.variant.core.schema.Variation.Experience;
import com.variant.core.schema.impl.StateImpl;
import com.variant.core.schema.impl.StateVariantImpl;
import com.variant.core.schema.impl.VariationExperienceImpl;
import com.variant.core.schema.impl.VariationImpl;
import com.variant.core.schema.impl.VariationOnStateImpl;
import com.variant.core.schema.parser.error.SemanticError.Location;
import com.variant.core.util.CaseInsensitiveMap;
import com.variant.core.util.CollectionsUtils;
import com.variant.core.util.StringUtils;

/**
 * Parse the element of the tests/onViews/variants list.
 * 
 * @author Igor
 *
 */
public class VariantParser implements Keywords {

	/**
	 * Parse an element of the list property tests/onViews/variants.
	 * @param variantObject
	 * @param tov
	 * @param response
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static StateVariantImpl parseVariant(Object variantObject, Location variantLocation, VariationOnStateImpl vos, ParserResponse response) {

		VariationImpl test = (VariationImpl) vos.getVariation();
		StateImpl state = (StateImpl) vos.getState();
		
		Map<String, Object> rawVariant = null;
		
		try {
			rawVariant = (Map<String, Object>) variantObject;
		}
		catch (Exception e) {
			response.addMessage(variantLocation, ELEMENT_NOT_OBJECT, KEYWORD_VARIANTS);
			return null;
		}
		
		// Pass 1. Find proper experienceRef and isPhantom
		// Note that we loop over all entries instead of just getting what we want because
		// the syntax is case insensitive and don't know what to get().
		String experienceRef = null;
		boolean isPhantom = false;
		
		for (Map.Entry<String, Object> entry: rawVariant.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_EXPERIENCE_REF)) {
				try {
					experienceRef = (String) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(variantLocation.plusProp(KEYWORD_EXPERIENCE_REF), PROPERTY_NOT_STRING, KEYWORD_EXPERIENCE_REF);
					return null;
				}
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_IS_PHANTOM)) {
				try {
					isPhantom = (Boolean) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(variantLocation.plusProp(KEYWORD_IS_PHANTOM), PROPERTY_NOT_BOOLEAN, KEYWORD_IS_PHANTOM);
				}
			}
		}
		
		if (experienceRef == null) {
			response.addMessage(variantLocation, PROPERTY_MISSING, KEYWORD_EXPERIENCE_REF);
			return null;
		}
		
		// The experience must exist
		Optional<Experience> expOpt = test.getExperience(experienceRef);
		if (!expOpt.isPresent()) {
			response.addMessage(variantLocation.plusProp(KEYWORD_EXPERIENCE_REF), EXPERIENCEREF_UNDEFINED, experienceRef);
			return null;			
		}
		
		VariationExperienceImpl exp = (VariationExperienceImpl) expOpt.get();
		
		// Variant cannot refer to a control experience, unless phantom.
		if (exp.isControl() && !isPhantom) {
			response.addMessage(variantLocation.plusProp(KEYWORD_EXPERIENCE_REF), EXPERIENCEREF_ISCONTROL,  experienceRef);
			return null;						
		}
		
		// Pass 2. Find conjointExperienceRefs.
		ArrayList<VariationExperienceImpl> conjointExperiences = null;
		
		for (Map.Entry<String, Object> entry: rawVariant.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_CONJOINT_EXPERIENCE_REFS)) {
				
				if (isPhantom) {
					response.addMessage(
							variantLocation.plusProp(KEYWORD_EXPERIENCE_REF),
							PROPERTY_NOT_ALLOWED_PHANTOM_VARIANT, 
							KEYWORD_CONJOINT_EXPERIENCE_REFS);
					
					return null;					
				}
								
				List<?> conjointExpRefList; 
				try {
					conjointExpRefList = (List<?>) entry.getValue();
				}
				catch (Exception e) {
					response.addMessage(
							variantLocation.plusObj(KEYWORD_CONJOINT_EXPERIENCE_REFS),
							PROPERTY_NOT_LIST,  
							KEYWORD_CONJOINT_EXPERIENCE_REFS);
					
					return null;
				}
				
				conjointExperiences = new ArrayList<VariationExperienceImpl>();

				int index = 0;
				for (Object conjointExpRefObj: conjointExpRefList) {

					Location covarExpRefLocation = variantLocation.plusObj(KEYWORD_CONJOINT_EXPERIENCE_REFS).plusIx(index++);
					
					Map<String,?> conjointExpRefMap;
					try {
						conjointExpRefMap = new CaseInsensitiveMap((Map<String,?>) conjointExpRefObj);
					}
					catch (Exception e) {
						response.addMessage(
								covarExpRefLocation,
								ELEMENT_NOT_OBJECT, 
								KEYWORD_CONJOINT_EXPERIENCE_REFS);
						
						return null;
					}
					String conjointVarRef = null, conjointExpRef = null;
					try {
						conjointVarRef = (String) conjointExpRefMap.get(KEYWORD_VARIATION_REF);
					}
					catch (Exception e) {
						response.addMessage(
								covarExpRefLocation.plusProp(KEYWORD_VARIATION_REF),
								PROPERTY_NOT_STRING, 
								KEYWORD_VARIATION_REF);
					}
					try {
						conjointExpRef = (String) conjointExpRefMap.get(KEYWORD_EXPERIENCE_REF);
					}
					catch (Exception e) {
						response.addMessage(
								covarExpRefLocation.plusProp(KEYWORD_EXPERIENCE_REF),
								PROPERTY_NOT_STRING,  
								KEYWORD_EXPERIENCE_REF);
					}
					
					if (conjointVarRef == null || conjointExpRef == null) return null;
					
					// Conjoint variation must have already been defined.
					Optional<Variation> conjointVarOpt = response.getSchema().getVariation(conjointVarRef);					
					if (!conjointVarOpt.isPresent()) {
						response.addMessage(
								covarExpRefLocation.plusProp(KEYWORD_VARIATION_REF),
								CONJOINT_EXPERIENCE_TEST_REF_UNDEFINED,
								conjointVarRef);
						return null;
					}
					
					Variation conjointVar = conjointVarOpt.get();
										
					// This test must declare the other test as conjoint.
					if (test.getConjointVariations() == null || !test.getConjointVariations().contains(conjointVar)) {
						response.addMessage(
								covarExpRefLocation.plusProp(KEYWORD_EXPERIENCE_REF),
								CONJOINT_VARIANT_TEST_NOT_CONJOINT,
								conjointVarRef);
						return null;
					}

					// Conjoint experience must have already been defined.
					Optional<Experience> conjointExpOpt = conjointVar.getExperience(conjointExpRef);
					if (!conjointExpOpt.isPresent()) {
						response.addMessage(
								covarExpRefLocation.plusProp(KEYWORD_EXPERIENCE_REF),
								CONJOINT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED, 
								conjointVarRef, conjointExpRef);
						return null;
					}

					Experience conjointExp = conjointExpOpt.get();
										
					if (conjointExperiences.contains(conjointExp)) {
						response.addMessage(
								covarExpRefLocation.plusProp(KEYWORD_EXPERIENCE_REF),
								CONJOINT_EXPERIENCE_DUPE,
								conjointVarRef, conjointExpRef);
						return null;
					}

					// if multiple conjoint experience refs, they can only reference pairwise conjoint tests.
					for (VariationExperienceImpl e: conjointExperiences) {
						if (!e.getVariation().isConjointWith(conjointExp.getVariation())) {
							response.addMessage(
									covarExpRefLocation,
									CONJOINT_EXPERIENCE_REF_TESTS_NOT_CONJOINT, 
									CollectionsUtils.toString(CollectionsUtils.list(e.getVariation(), conjointExp.getVariation()), ", "));
							return null;
						}
					}

					conjointExperiences.add((VariationExperienceImpl)conjointExp);
				}
			}
		}

		
		// Pass 3. Parse the rest of experience element.
		
		Map<String,String> params = new LinkedHashMap<String, String>();
		
		for (Map.Entry<String, Object> entry: rawVariant.entrySet()) {
			
			if (StringUtils.equalsIgnoreCase(entry.getKey(), KEYWORD_EXPERIENCE_REF, KEYWORD_IS_PHANTOM, KEYWORD_CONJOINT_EXPERIENCE_REFS)) 
				continue;
		
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_PARAMETERS)) {
				
				if (isPhantom) {
					response.addMessage(
							variantLocation.plusObj(KEYWORD_PARAMETERS),
							PROPERTY_NOT_ALLOWED_PHANTOM_VARIANT, 
							KEYWORD_PARAMETERS);
					return null;					
				}

				params = ParamsParser.parse(entry.getValue(), variantLocation.plusObj(KEYWORD_PARAMETERS), response);
			}
			else {
				response.addMessage(variantLocation.plusProp(entry.getKey()), UNSUPPORTED_PROPERTY, entry.getKey());
			}
		}

		// Don't create a state variant if phantom.
		if (isPhantom) {
			exp.addUninstrumentedState(state);
			return null;
		}
		
		// Resort covarTestExperiences in ordinal order, if present.
		List<Variation.Experience> orderedCovarTestExperiences = new ArrayList<Variation.Experience>(); 
		
		if (conjointExperiences != null) {
			
			for (Variation t: response.getSchema().getVariations()) {
				for (Experience e: conjointExperiences) {
					if (t.equals(e.getVariation())) {
						orderedCovarTestExperiences.add((VariationExperienceImpl) e);
						break;
					}
				}
			}
		}
		
		return new StateVariantImpl(vos, exp, orderedCovarTestExperiences, params);
	}
}

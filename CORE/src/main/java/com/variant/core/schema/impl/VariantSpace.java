package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.variant.core.error.CoreException;
import com.variant.core.error.CoreException.Internal;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Variation;
import com.variant.core.schema.Variation.Experience;
import com.variant.core.util.CollectionsUtils;

/**
 * Variant Space is a cartesian space with the basis given by a list of tests,
 * coordinates are a list of experiences, exactly one for each basis test,
 * and value an instance of Test.OnState.Variant. 
 * 
 * @author Igor
 *
 */
public class VariantSpace {

	// The basis is a set to prevent dupes and linked to establish
	// a repeatable iterator order.
	private LinkedHashSet<Variation> basis;
	
	// Space table.
	private LinkedHashMap<Coordinates, Point> spaceMap = new LinkedHashMap<Coordinates, Point>();
	
	/**
	 * Cell coordinates in this space.
	 */
	private static class Coordinates {
	
		private ArrayList<Experience> coordinates = new ArrayList<Experience>();
		
		/**
		 * 1-dimensional coordinate vector.
		 * 
		 * @param experience
		 */
		private Coordinates(Experience experience) {
			coordinates.add(experience);
		}
		
		/**
		 * N+1 dimensional vector.
		 * @param experiences
		 */
		private Coordinates(Coordinates coordinates, Experience experience) {
			this.coordinates.addAll(coordinates.coordinates);
			this.coordinates.add(experience);
		}
		
		/**
		 * Client code access.
		 * @param experiences
		 */
		private Coordinates(List<Experience> experiences) {
			coordinates.addAll(experiences);
		}
		
		/**
		 * Can this coordinate vector be combined with a new dimension (a new variation) ?
		 * A new dimension can only be added to a vector whose all existing dimensions are
		 * conjoint with the given test.
		 * 
		 * @param test
		 * @return true, if test is pair-wise conjoint with all tests in this 
		 */
		private boolean isCombinableWith(Variation test) {
			for (Experience e: coordinates)
				if (! e.getVariation().isConjointWith(test)) return false;
			return true;
		}
		
		/**
		 */
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Coordinates)) return false;
			Coordinates other = (Coordinates) o;
			return coordinates.equals(other.coordinates);
		}
		
		/**
		 */
		@Override
		public int hashCode() {
			return coordinates.hashCode();
		}
		
		/**
		 */
		@Override
		public String toString() {
			return CollectionsUtils.toString(coordinates, ",");
		}
	}

	/**
	 * Crate the variant space for a particular Variation.OnState object,
	 * filled with inferred (default) state variants. Real ones are added
	 * as they are being parsed.
	 * 
	 * @param vosImpl
	 */
	@SuppressWarnings("unchecked")
	public VariantSpace(VariationOnStateImpl vosImpl)  {
				
		// Build the basis -- list of variations starting with the one containing this VoS,
		// followed by the conjoint variations, in ordinal order.
		basis = new LinkedHashSet<Variation>();
		basis.add(vosImpl.getVariation());
		basis.addAll(vosImpl.getVariation().getConjointVariations().orElse(Collections.EMPTY_LIST));

		// Build the space.
		for (Variation basisVar: basis) {
			
			// HashTable.keySet() returns a view, which will change if the table is modified.
			// To take a static snapshot, we crate a new collection;
			ArrayList<Coordinates> oldKeys = new ArrayList<Coordinates>(spaceMap.keySet());
			
			if (oldKeys.size() == 0) {
				
				// Empty space map => the first variation in basis is the enclosing one.
				// Add proper variant experiences.
				for (Experience exp: basisVar.getExperiences()) {
					if (exp.isControl()) continue;
					Coordinates coordinates = new Coordinates(exp);
					spaceMap.put(coordinates, new Point(vosImpl, coordinates));
				}
			}
			else {
				
				// 2nd and on variation, if present, is conjoint. Skip it, if it's not instrumented on this state.
				// Otherwise, compute the cartеsian product of what's already in the spaceMap and this current tests's 
				// variant experiences, skipping those experiences where this state is phantom.

				if (!vosImpl.getState().isInstrumentedBy(basisVar)) continue;				
				
				for (Experience exp: basisVar.getExperiences()) {
					if (exp.isControl() || exp.isPhantom(vosImpl.getState())) continue;
					for (Coordinates oldKey: oldKeys) {
						// #30: Only multiply by this dimension, if it is conjoint with all tests already in the table. 
						if (oldKey.isCombinableWith(basisVar)) {
							Coordinates coordinates = new Coordinates(oldKey, exp);
							spaceMap.put(coordinates, new Point(vosImpl, coordinates));
						}
					}
				}
			}
		}
		
	}

	/**
	 * Add a state variant to this space. This may throw a parse error.
	 * @param variant
	 */
	public void addVariant(StateVariant variant) {
		
		// Build coordinate experience list. Must be concurrent with the basis.
		List<Experience> coordinateExperiences = new ArrayList<Experience>(basis.size());
		
		// Local experience must be first.
		coordinateExperiences.add(variant.getExperience());
		
		// Conjoint experiences may not be ordered same as the basis, so we have to reorder.
		for (Variation basisTest: basis) {
		
			// Skip first test in basis because it refers to the local test and we already have that.
			if (basisTest.equals(variant.getVariation())) continue;
			
			if (!variant.isProper()) {
				for (Experience conjointExp: variant.getConjointExperiences()) {
					if (conjointExp.getVariation().equals(basisTest)) {
						coordinateExperiences.add(conjointExp);
						break;
					}
				}
			}				
		}
		
		Coordinates coordinates = new Coordinates(coordinateExperiences);
		
		// This point must already exist, unless it's undefined. 
		Point p = spaceMap.get(coordinates);
		if (p == null) {
			throw new CoreException.Internal(
					"No point for coordinates [" + CollectionsUtils.toString(coordinateExperiences, ", ") + 
					"] in variation [" + variant.getVariation().getName() + "] and state [" + variant.getOnState().getState().getName() + "]");
		}
		if (p.variant != null && !((StateVariantImpl)p.variant).isInferred())
			throw new CoreException.Internal("Variant already added");

		p.variant = variant;
		
	}
		
	/**
	 * Get all non-phantom state variants.
	 * Keep the ordinal order so tests can expect repeatable results.
	 * @return
	 */
	public Set<StateVariant> getAll() {
		LinkedHashSet<StateVariant> result = new LinkedHashSet<StateVariant>();
		spaceMap.values().forEach(p -> result.add(p.variant));
		return result;
	}
	
	/**
	 * Lookup a variant for an experience vector
	 * Caller insures that all experiences are not control.
	 */
	public StateVariant get(Set<Experience> coordinates) {
		
		// resort vector in basis order.
		ArrayList<Experience> sortedVector = new ArrayList<Experience>(coordinates.size());
		for (Variation basisTest: basis) {
			for (Experience e: coordinates) {
				if (basisTest.equals(e.getVariation())) {
					sortedVector.add(e);
					break;
				}
			}
		}

		// If there are expereinces in the input vector whose tests
		// were not found in basis, the input vector is not in this space.
		if (coordinates.size() > sortedVector.size()) return null;
		
		Point result = spaceMap.get(new Coordinates(sortedVector));
		return result == null? null : result.variant;
	}

	/**
	 * Point of this space is given by its coordinates and has Variant as value.
	 * A new point contain a default implicit state variant, which may later be
	 * replaced by an explicit one, if provided in the schema.
	 */
	private static class Point {
		
		//private Coordinates coordinates;
		private StateVariant variant;
		
		private Point(VariationOnStateImpl vosImpl, Coordinates coordinates) {
			//this.coordinates = coordinates;
			Variation.Experience ownExp = coordinates.coordinates.get(0);
			List<Variation.Experience> conjointExps = coordinates.coordinates.subList(1, coordinates.coordinates.size());
			this.variant = new StateVariantImpl(vosImpl, ownExp, conjointExps);
		}
		
		/** PROBABLY NO LONGER NEEDED
		 * The own experience
		 * @return
		 *
		public Experience getExperience() {
			return coordinates.coordinates.get(0);
		}
		
		/**
		 * Conjoint experiences, if any.
		 * @return
		 *
		public List<Experience> getConjointExperiences() {
			return coordinates.coordinates.subList(1, coordinates.coordinates.size());
		}

		/**
		 * Variant defined in this point, if any.
		 * @return
		 *
		public StateVariant getVariant() {
			return variant;
		}
		
		/**
		 * A point is defined in a variant space if none of its coordinate experiences 
		 * declared this state as phantom.In other words: if a state is phantom in some experience, 
		 * than all state variants whose coordinates contain that experience are also undefined.
		 * 
		 * @return
		 *
		public boolean isDefinedOn(State state) {
			return coordinates.coordinates.stream().allMatch(e -> !e.isPhantom(state));
		}
		*/
	}	
}

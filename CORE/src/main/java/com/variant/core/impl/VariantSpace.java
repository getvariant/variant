package com.variant.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.Test.OnState.Variant;
import com.variant.core.schema.impl.TestOnStateImpl;
import com.variant.core.util.VariantStringUtils;

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
	private LinkedHashSet<Test> basis;
	
	// Table data is a map.
	private LinkedHashMap<Coordinates, Point> table = new LinkedHashMap<Coordinates, Point>();
	
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
		 * Can this coordinate vector be combined with a new dimension, i.e. a new test?
		 * A new dimension can only be added to a vector whose all existing dimensions are
		 * covariant with the given test.
		 * 
		 * @param test
		 * @return true, if test is pair-wise covariant with all tests in this 
		 */
		private boolean isCombinableWith(Test test) {
			for (Experience e: coordinates)
				if (! e.getTest().isCovariantWith(test)) return false;
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
			return VariantStringUtils.toString(coordinates, ",");
		}
	}

	/**
	 * Crate the variant space for a particular Test.OnState instrumentation object. 
	 * Assumes that the testOnStateImpl object is fully formed, but does not
	 * assume anything else of the schema.  This is critical because this is called
	 * in the middle of the new schema construction, where the existing publicly visible
	 * schema is, potentially, obsolete, but the new schema doesn't yet exist.
	 * 
	 * @param tosImpl
	 */
	public VariantSpace(TestOnStateImpl tosImpl)  {
		
		if (tosImpl.isNonvariant()) 
			throw new VariantInternalException("Cannot crate VariantSpace for a nonvariant instrumentation");
		
		// Build basis sorted in ordinal order.
		basis = new LinkedHashSet<Test>();
		basis.add(tosImpl.getTest());
		basis.addAll(tosImpl.getTest().getCovariantTests());

		// Pass 1. Build empty space, i.e. all Points with nulls for Variant values, for now.
		for (Test t: basis) {
			
			// HashTable.keySet() returns a view, which will change if we modify the table.
			// To take a static snapshot, we crate a new collection;
			ArrayList<Coordinates> oldKeys = new ArrayList<Coordinates>(table.keySet());
			
			
			if (oldKeys.size() == 0) {
				// First test is the proper test - add all non-control experiences.
				for (Experience exp: t.getExperiences()) {
					if (exp.isControl()) continue;
					Coordinates coordinates = new Coordinates(exp);
					table.put(coordinates, new Point(coordinates));
				}
			}
			else {
				// 2nd and on test is a covariant test.  Skip it, if it's not instrumented or is nonvariant on this state.
				// Otherwise, compute the cartеsian product of what's already in the table and this current tests's experience list.
				if (!tosImpl.getState().isInstrumentedBy(t) || tosImpl.getState().isNonvariantIn(t)) continue;				
				
				for (Experience exp: t.getExperiences()) {
					if (exp.isControl()) continue;
					for (Coordinates oldKey: oldKeys) {
						// #30: Only multiply by this dimension, if it is covariant with all tests already in the table. 
						if (oldKey.isCombinableWith(t)) {
							Coordinates coordinates = new Coordinates(oldKey, exp);
							table.put(coordinates, new Point(coordinates));
						}
					}
				}
			}
		}
		
		// Pass 2. Add variants.
		for (Test.OnState.Variant variant: tosImpl.getVariants()) {

			// Build coordinate experience list. Must be concurrent with the basis.
			List<Experience> coordinateExperiences = new ArrayList<Experience>(basis.size());
			
			// Local experience must be first.
			coordinateExperiences.add(variant.getExperience());
			
			// Covariant experiences are not concurrent to basis, so we have to reorder.
			for (Test basisTest: basis) {
			
				// Skip first test in basis because it refers to the local test and we already have that.
				if (basisTest.equals(variant.getTest())) continue;
				
				for (Experience covarExp: variant.getCovariantExperiences()) {
					if (covarExp.getTest().equals(basisTest)) {
						coordinateExperiences.add(covarExp);
						break;
					}
				}
				
			}
			Coordinates coordinates = new Coordinates(coordinateExperiences);
			Point p = table.get(coordinates);
			if (p == null) {
				throw new VariantInternalException(
						"No point for coordinates [" + VariantStringUtils.toString(coordinateExperiences, ", ") + 
						"] in test [" + variant.getTest().getName() + "] and view [" + variant.getOnState().getState().getName() + "]");
			}
			if (p.variant != null)
				throw new VariantInternalException("Variant already added");
			p.variant = variant;
		}
	}
	
	/**
	 * Get all vectors in this variant space for a particula view.
	 * @return
	 */
	public Collection<Point> getAll() {
		return table.values();
	}
	
	/**
	 * Lookup a variant for an experience vector
	 * Caller insures that all experiences are not control.
	 */
	public Variant get(List<Experience> vector) {
		
		// resort vector in basis order.
		ArrayList<Experience> sortedVector = new ArrayList<Experience>(vector.size());
		for (Test basisTest: basis) {
			for (Experience e: vector) {
				if (basisTest.equals(e.getTest())) {
					sortedVector.add(e);
					break;
				}
			}
		}

		// If there are expereinces in the input vector whose tests
		// were not found in basis, the input vector is not in this space.
		if (vector.size() > sortedVector.size()) return null;
		
		Point result = table.get(new Coordinates(sortedVector));
		return result.variant;
	}

	
	/**
	 * Point of this space is gien by its coordinates and has Variant as value.
	 */
	public static class Point {
		
		private Coordinates coordinates;
		private Variant variant;
		
		private Point(Coordinates coordinates) {
			this.coordinates = coordinates;
			this.variant = null;
		}
		
		/**
		 * The local experience
		 * @return
		 */
		public Experience getExperience() {
			return coordinates.coordinates.get(0);
		}
		
		/**
		 * Covariant experiences, if any.
		 * @return
		 */
		public List<Experience> getCovariantExperiences() {
			return coordinates.coordinates.subList(1, coordinates.coordinates.size());
		}

		/**
		 * 
		 * @return
		 */
		public Variant getVariant() {
			return variant;
		}
		
	}	
}

package com.variant.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import com.variant.core.VariantInternalException;
import com.variant.core.VariantRuntimeException;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.Test.OnView.Variant;
import com.variant.core.schema.impl.TestOnViewImpl;
import com.variant.core.util.VariantStringUtils;

/**
 * Variant Space is a cartesian space with the basis given by a list of tests,
 * coordinates are a list of experiences, exactly one for each basis test,
 * and value an instance of Test.OnView.Variant. 
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
	 * Crate the variant space for a particular Test.OnView object. 
	 * Assumes that the testOnViewImpl object is fully formed, but does not
	 * assume anything else of the schema.  This is critical because this is called
	 * in the middle of the new schema construction, where the existing publically visible
	 * schema is obsolete, but the new schema doesn't yet exist.
	 * 
	 * @param basis
	 * @throws VariantRuntimeException 
	 */
	public VariantSpace(TestOnViewImpl tovImpl)  {
		
		if (tovImpl.isInvariant()) 
			throw new VariantInternalException("Cannot crate VariantSpace for an invariant Test.OnView instance");
		
		// Build basis sorted in ordinal order.
		basis = new LinkedHashSet<Test>();
		basis.add(tovImpl.getTest());
		basis.addAll(tovImpl.getTest().getCovariantTests());

		// Pass 1. Build empty space, i.e. all Points with nulls for Variant values, for now.
		for (Test t: basis) {
			
			// HashTable.keySet() returns a view, which will change if we modify the table.
			// To take a static snapshot, we crate a new collection;
			ArrayList<Coordinates> oldKeys = new ArrayList<Coordinates>(table.keySet());
			
			
			if (oldKeys.size() == 0) {
				// First test is the local test - add all non-control experiences.
				for (Experience exp: t.getExperiences()) {
					if (exp.isControl()) continue;
					Coordinates coordinates = new Coordinates(exp);
					table.put(coordinates, new Point(coordinates));
					//System.out.println("Added [" + exp + "], Test [" + tovImpl.getTest().getName() + "], View [" + tovImpl.getView().getName() + "] " + coordinates.hashCode());
				}
			}
			else {
				// 2nd and on test is a covariant test.  Skip it if it's not instrumented on this view or is invariant on this view.
				// Otherwise, compute the cartеsian product of what's already in the table and this current tests's experience list.
				if (!tovImpl.getView().isInstrumentedBy(t) || tovImpl.getView().isInvariantIn(t)) continue;
				for (Experience exp: t.getExperiences()) {
					if (exp.isControl()) continue;
					for (Coordinates oldKey: oldKeys) {
						Coordinates coordinates = new Coordinates(oldKey, exp);
						table.put(coordinates, new Point(coordinates));
						//System.out.println("Added c[" + VariantStringUtils.toString(coordinates.coordinates, ",") + "], Test [" + tovImpl.getTest().getName() + "], View [" + tovImpl.getView().getName() + "] " + coordinates.hashCode());
					}
				}
			}
		}
		
		// Pass 2. Add variants.
		for (Test.OnView.Variant variant: tovImpl.getVariants()) {

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
						"] in test [" + variant.getTest().getName() + "] and view [" + variant.getOnView().getView().getName() + "]");
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
	
	/**
	 * Cell coordinates in this space.
	 * In all constructors, caller makes sure that experience lists' order
	 * is the same as in basis and that none of the experiences are control.
	 */
	private static class Coordinates {
	
		private ArrayList<Experience> coordinates = new ArrayList<Experience>();
		
		/**
		 * 1-dimensional vector.
		 * 
		 * @param experience
		 */
		private Coordinates(Experience experience) {
			coordinates.add(experience);
		}
		
		/**
		 * N+1 dimensional vector
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
				
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Coordinates)) return false;
			Coordinates other = (Coordinates) o;
			return coordinates.equals(other.coordinates);
		}
		
		@Override
		public int hashCode() {
			return coordinates.hashCode();
		}
	}
	
}

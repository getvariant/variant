package com.variant.core.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
 * and value an instance of Test.OnView.Variant. The basis is a list of the
 * form (Tl, Tc...) where T is some test ("local") and Tc... its covariant tests 
 * in the order given in its covariantTestRefs clause. The value objects of type
 * Test.OnView.Variant come from the same Test.OnView instance which in turn
 * is part of the same local test Tl and some View V. Therefore, a VaraintSpace
 * instance has implicit relation to a particula test and a particular View,
 * i.e. some (Tl, V) pair.
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
	 * Checks that there are no duplicates, but does not assume runtime scope,
	 * i.e. does not look in current schema for anything.
	 * 
	 * @param basis
	 * @throws VariantRuntimeException 
	 */
	public VariantSpace(TestOnViewImpl tovImpl) throws VariantRuntimeException {
		
		if (tovImpl.isInvariant()) 
			throw new VariantInternalException("Cannot crate VariantSpace for an invariant Test.OnView instance");
		
		// Build basis.  The local test, i.e. the one this Test.OnView instance belongs to
		// is always first and often the only test in the basis.
		basis = new LinkedHashSet<Test>();
		basis.add(tovImpl.getTest());
		basis.addAll(tovImpl.getTest().getCovariantTests());
		
		// Pass 1. Build empty space, i.e. all Points with nulls for Variant values, for now.
		for (Test t: basis) {
			
			// HashTable.keySet() returns a view, which will change if we modify the table.
			// To take a snapshot, we crate a new collection;
			ArrayList<Coordinates> oldKeys = new ArrayList<Coordinates>(table.keySet());
			
			// If nothing yet, add all non-control experiences with nulls for variants.
			if (oldKeys.size() == 0) {
				for (Experience exp: t.getExperiences()) {
					if (exp.isControl()) continue;
					Coordinates coordinates = new Coordinates(exp);
					table.put(coordinates, new Point(coordinates));
					//System.out.println("Added [" + exp + "], Test [" + tovImpl.getTest().getName() + "], View [" + tovImpl.getView().getName() + "] " + coordinates.hashCode());
				}
			}
			// If something already in the table, compute the cartеsian product of what's already in the table 
			// and this current tests's experience list.
			else {
				if (tovImpl.getView().isInvariantIn(t)) continue;
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
	 * TODO: will prob. need this for run time lookup.
	public Point get(List<Experience> experiences) {
		
		return table.get(new Coordinates(experiences));
	}
	*/
	
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

package com.variant.core.runtime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import com.variant.core.VariantInternalException;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.Test.OnView.Variant;

/**
 * 
 * @author Igor
 *
 */
public class VariantMatrix {

	// The basis is a set to prevent dupes and linked to establish
	// a repeatable iterator order.
	private LinkedHashSet<Test> basis = new LinkedHashSet<Test>();
	
	// Table data is a map.
	private LinkedHashMap<Vector, Variant> table = new LinkedHashMap<Vector, Variant>();
	
	/**
	 * Cell coordinates in this matrix.
	 */
	private class Vector {
	
		private ArrayList<Experience> coordinates = new ArrayList<Experience>();
		
		/**
		 * Normalize a list of experiences into a coordinate
		 * @param experiences
		 */
		private Vector(List<Experience> experiences) {
			for (Test basisTest: basis) {
				boolean found = false;
				for (Experience exp: experiences) {
					if (exp.getTest().equals(basisTest)) {
						coordinates.add(exp);
						found = true;
						break;
					}
				}
				if (!found) {
					coordinates.add(basisTest.getControlExperience());
				}
			}
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Vector)) return false;
			Vector other = (Vector) o;
			return coordinates.equals(other.coordinates);
		}
		
		@Override
		public int hashCode() {
			return coordinates.hashCode();
		}
	}
	
	/**
	 * Crate the variant table with the given basis.  Tests must be unique.
	 * Checks that there are no duplicates.
	 * @param basis
	 */
	public VariantMatrix(List<Test> basis) {
		for (Test t: basis) {
			if (! this.basis.add(t)) {
				throw new VariantInternalException("Test [" + t.getName() + "'] is already in the basis."); 
			}
		}
	}
	
	/**
	 * Add a variant to the table as a cell with coordinates given by experiences.
	 * The coordinates must agree with this matrix basis, i.e. cannot contain an
	 * experience for a test that is not in the basis. But it's OK if there are fewer
	 * coordinate experiences than tests in the basis. In this case, the control
	 * experiences will be assumed for these tests.
	 * 
	 * @param experiences
	 * @param variant
	 */
	public void add(List<Experience> experiences, Variant variant) {
		
		if (table.put(new Vector(experiences), variant) != null) 
			throw new VariantInternalException("Varaint [" + variant + "] is already in the table");
		
	}
	
	public Variant lookup(List<Experience> experiences) {
		
		return table.get(new Vector(experiences));
	}
}

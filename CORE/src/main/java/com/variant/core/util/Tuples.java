package com.variant.core.util;


/**
 * Simple, reusable tuple-like containers, handy for passing around heterogeneous data.
 * Inspired by Scala tuples.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public class Tuples {

	/**
	 * Two-element tuple with elements of arbitrary types.
     * @author Igor Urisman
     * @since 0.5
	 */
	public static class Pair<T1,T2>  {
		private T1 arg1;
		private T2 arg2;
		
		/**
		 * Constructor.
		 * @param arg1 First element.
		 * @param arg2 Second element.
		 */
		public Pair(T1 arg1, T2 arg2) {
			this.arg1 = arg1;
			this.arg2 = arg2;
		}
		/**
		 * Get first element.
		 * @return
		 */
		public T1 arg1() { return arg1; }
		/**
		 * Get second element.
		 * @return
		 */
		public T2 arg2() { return arg2; }

		/**
		 * Override {@code Object.equals(Object)}
		 * @return true if the other object is also a {@link Pair} and the two pairs
		 *         are element-wise equal. 
		 */
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object other) {
			Pair<T1,T2> otherPair;
			try {
				otherPair = (Pair<T1,T2>) other;
			}
			catch (ClassCastException e){
				return false;
			}
			return arg1.equals(otherPair.arg1) && arg2.equals(otherPair.arg2);
		}
		
		/**
	     * Override {@code Object.toString()}
	     * @return Something meaningful.
		 */
		@Override
		public String toString() {
			return "(" + arg1 + ", " + arg2 + ")"; 
		}
	}
	
	/**
	 * Three-element tuple with elements of arbitrary types.
     * @author Igor Urisman
     * @since 0.5
	 */
	public static class Tripple<T1,T2,T3> {
		private T1 arg1;
		private T2 arg2;
		private T3 arg3;

		/**
		 * Constructor.
		 * @param arg1 First element.
		 * @param arg2 Second element.
		 * @param arg3 Third element.
		 */
		public Tripple(T1 arg1, T2 arg2, T3 arg3) {
			this.arg1 = arg1;
			this.arg2 = arg2;
			this.arg3 = arg3;
		}
		/**
		 * Get first element.
		 * @return
		 */
		public T1 arg1() { return arg1; }
		/**
		 * Get second element.
		 * @return
		 */
		public T2 arg2() { return arg2; }
		/**
		 * Get third element.
		 * @return
		 */
		public T3 arg3() { return arg3; }
		
		/**
		 * Override {@code Object.equals(Object)}
		 * @return true if the other object is also a {@link Pair} and the two pairs
		 *         are element-wise equal. 
		 */
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object other) {
			Tripple<T1,T2, T3> otherTripple;
			try {
				otherTripple = (Tripple<T1,T2,T3>) other;
			}
			catch (ClassCastException e){
				return false;
			}
			return arg1.equals(otherTripple.arg1) && arg2.equals(otherTripple.arg2) && arg3.equals(otherTripple.arg3);
		}
		
		/**
	     * Override {@code Object.toString()}
	     * @return Something meaningful.
		 */
		@Override
		public String toString() {
			return "(" + arg1 + ", " + arg2 + ", " + arg3 + ")"; 
		}
	}

}

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
				
		private T1 _1;
		private T2 _2;
		
		/**
		 * Constructor.
		 * @param arg1 First element.
		 * @param arg2 Second element.
		 */
		public Pair(T1 arg1, T2 arg2) {
			_1 = arg1;
			_2 = arg2;
		}
		/**
		 * Get first element.
		 * @return
		 */
		public T1 _1() { return _1; }
		/**
		 * Get second element.
		 * @return
		 */
		public T2 _2() { return _2; }

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
			return _1.equals(otherPair._1) && _2.equals(otherPair._2);
		}
		
		/**
	     * Override {@code Object.toString()}
	     * @return Something meaningful.
		 */
		@Override
		public String toString() {
			return "(" + _1 + ", " + _2 + ")"; 
		}
	}
	
	/**
	 * Three-element tuple with elements of arbitrary types.
     * @author Igor Urisman
     * @since 0.5
	 */
	public static class Tripple<T1,T2,T3> {
		private T1 _1;
		private T2 _2;
		private T3 _3;

		/**
		 * Constructor.
		 * @param arg1 First element.
		 * @param arg2 Second element.
		 * @param arg3 Third element.
		 */
		public Tripple(T1 arg1, T2 arg2, T3 arg3) {
			_1 = arg1;
			_2 = arg2;
			_3 = arg3;
		}
		/**
		 * Get first element.
		 * @return
		 */
		public T1 arg1() { return _1; }
		/**
		 * Get second element.
		 * @return
		 */
		public T2 arg2() { return _2; }
		/**
		 * Get third element.
		 * @return
		 */
		public T3 arg3() { return _3; }
		
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
			return _1.equals(otherTripple._1) && _2.equals(otherTripple._2) && _3.equals(otherTripple._3);
		}
		
		/**
	     * Override {@code Object.toString()}
	     * @return Something meaningful.
		 */
		@Override
		public String toString() {
			return "(" + _1 + ", " + _2 + ", " + _3 + ")"; 
		}
	}

}

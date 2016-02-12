package com.variant.core.util;


/**
 * Thinking Scala
 * @author Igor Urisman
 */
public class Tuples {

	/**
	 * 
	 */
	public static class Pair<T1,T2>  {
		private T1 arg1;
		private T2 arg2;
		public Pair(T1 arg1, T2 arg2) {
			this.arg1 = arg1;
			this.arg2 = arg2;
		}
		public T1 arg1() { return arg1; }
		public T2 arg2() { return arg2; }
		
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
		
		@Override
		public String toString() {
			return "(" + arg1 + ", " + arg2 + ")"; 
		}
		
	}
	
	/**
	 * 
	 */
	public static class Tripple<T1,T2,T3> {
		private T1 arg1;
		private T2 arg2;
		private T3 arg3;
		public Tripple(T1 arg1, T2 arg2, T3 arg3) {
			this.arg1 = arg1;
			this.arg2 = arg2;
			this.arg3 = arg3;
		}
		public T1 arg1() { return arg1; }
		public T2 arg2() { return arg2; }
		public T3 arg3() { return arg3; }
		
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
		
		@Override
		public String toString() {
			return "(" + arg1 + ", " + arg2 + ", " + arg3 + ")"; 
		}
	}

}

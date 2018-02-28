package com.variant.core.util;

/**
 * An anticipation of Java 8's Predicate.
 * 
 * @param <T>
 * @author Igor Urisman
 * Since 0.6
 */
public interface Predicate<T> {

    /**
     * Use the specified parameter to perform a test that returns true or false.
     *
     * @param object  the object to evaluate, should not be changed
     * @return true or false
     * @throws ClassCastException (runtime) if the input is the wrong class
     * @throws IllegalArgumentException (runtime) if the input is invalid
     * @throws FunctorException (runtime) if the predicate encounters a problem
     */
    boolean test(T object);

}

package com.variant.core.schema;

import java.util.List;
import java.util.Map;

/**
 * Representation of the <code>state</code> schema property.
 * 
 * @since 0.5
 */
public interface State {

	/**
	 * The schema object containing this state object.
	 * 
	 * @return An object of type {@link Schema}
	 * @since 0.6
	 */
	public Schema getSchema();
	
	/**
	 * The name of this state.
	 * 
	 * @return The name of this state.
	 * @since 0.5
	 */
	public String getName();	
	
	/**
	 * Immutable map of sate parameters defined by this state.
	 * 
	 * @return Immutable map of sate parameters defined by this state.
	 * @since 0.6
	 */
	public Map<String,String> getParameters();

	/**
	 * <p>List, in ordinal order, of variations which are instrumented on this state.
	 *  
	 * @return A list of {@link Variation} objects.
	 * @since 0.5
	 */
	List<Variation> getInstrumentedVariations();

	/**
	 * Returns <code>true</code> if this state is instrumented by a given variation.
	 * In other words, is a given variation contained in the return value of {@link #getInstrumentedVariants()}?
	 * 
	 * @param variation The variation of interest.
	 * @return <code>true</code> if this state is instrumented by the given variation, <code>false</code> otherwise. 
	 * @since 0.5
	 */
	public boolean isInstrumentedBy(Variation variation);

	/**
	 * Returns <code>true</code> if this state is declared as non-variant in a given variation. 
	 * It is the responsibility of the caller to ensure that this state is instrumented by the given test, 
	 * i.e. that {@link #isInstrumentedBy(Variation)} returns true.
	 * 
	 * @param variation The variation of interest.
	 * 
	 * @return <code>true</code> if this state is non-variant in the given variation, <code>false</code> if this test.
	 * @throws StateNotInstrumentedException if this state is not instrumented by the given test.
	 * @since 0.5
	 */
	public boolean isNonvariantIn(Variation variation);
	
	/**
	 * <p>List of variation-scoped life-cycle hooks defined in the scope of this state.
	 * 
	 * @return A list, in the ordinal order, of {@link Hook} objects.
	 * @since 0.8
	 */
	public List<Hook> getHooks();

}

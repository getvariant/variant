package com.variant.core.session;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.util.Tuples.Pair;
import com.variant.core.util.VariantCollectionsUtils;

/**
 * 
 * @author Igor
 *
 */
public class VariantSessionImpl implements VariantSession, Serializable {
	
	///
	private static final long serialVersionUID = 1L;

	private String id;
	private VariantStateRequest currentRequest = null;
	private HashMap<State, Integer> traversedStates = new HashMap<State, Integer>();
	private HashMap<Test, Boolean> traversedTests = new HashMap<Test, Boolean>();
	
	/**
	 * 
	 * @param id
	 */
	VariantSessionImpl(String id) {
		this.id = id;
	}
		
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 */
	@Override
	public String getId() {
		return id;
	}

	@Override
	public VariantStateRequest getStateRequest() {
		return currentRequest;
	}

	@Override
	public Collection<Pair<State, Integer>> getTraversedStates() {
		return VariantCollectionsUtils.mapToPairs(traversedStates);
	}

	@Override
	public Collection<Pair<Test, Boolean>> getTraversedTests() {
		return VariantCollectionsUtils.mapToPairs(traversedTests);
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @param req
	 */
	public void setStateRequest(VariantStateRequest req) {
		currentRequest = req;
	}

	/**
	 * Traversed and qualified?
	 * @param test
	 * @return
	 */
	public boolean isQualified(Test test) {
		Boolean result = traversedTests.get(test);
		return result != null && result;
	}
	
	/**
	 * Traversed and disqualified?
	 * @param test
	 * @return
	 */
	public boolean isDisqualified(Test test) {
		Boolean result = traversedTests.get(test);
		return result != null && !result;
	}

	/**
	 * 
	 * @param test
	 */
	public void addTraversedTest(Test test, boolean qualified) {

		if (traversedTests.get(test) != null) 
			throw new VariantInternalException(
					String.format("Test [%s] already exists in the traversed list", test.getName()));
					
		traversedTests.put(test, qualified);
	}

	/**
	 * 
	 * @param state
	 */
	public void addTraversedState(State state) {

		Integer count = traversedStates.get(state);
		if (count == null) count = 1;
		else count++;
		traversedStates.put(state, count);
	}

	@Override
	public boolean equals(Object o) {
		try {
			VariantSessionImpl other = (VariantSessionImpl) o;
			return id.equals(other.id);
		}
		catch(ClassCastException e) {
			return false;
		}
	}

}

package com.variant.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.VariantCoreSession;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.hook.TestQualificationHook;
import com.variant.core.hook.TestTargetingHook;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.Test.OnState;
import com.variant.core.schema.Test.OnState.Variant;
import com.variant.core.schema.impl.StateImpl;
import com.variant.core.schema.impl.TestOnStateImpl;
import com.variant.core.util.VariantStringUtils;

/**
 * Entry point into the runtime.
 * 
 * @author Igor.
 *
 */
public class VariantRuntime {

	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(VariantRuntime.class);

	/**
	 * 
	 */
	private static class TestQualificationHookImpl implements TestQualificationHook {
		
		private VariantCoreSession ssn;
		private Test test;
		private boolean qualified = true;
		private boolean removeFromTT = false;
		
		private TestQualificationHookImpl(VariantCoreSession ssn, Test test) {
			this.ssn = ssn;
			this.test = test;
		}
		
		@Override
		public Test getTest() {
			return test;
		}

		@Override
		public boolean isQualified() {
			return qualified;
		}
		
		@Override
		public boolean isRemoveFromTargetingTracker() {
			return removeFromTT;
		}
		
		@Override
		public VariantCoreSession getSession() {
			return ssn;
		}

		@Override
		public void setQualified(boolean qualified) {
			this.qualified = qualified;
		}

		@Override
		public void setRemoveFromTargetingTracker(boolean remove) {
			removeFromTT = remove;
		}

	};

	/**
	 * 
	 */
	private static class TestTargetingHookImpl implements TestTargetingHook {

		private VariantCoreSession session;
		private Test test;
		private Experience targetedExperience = null;
		
		private TestTargetingHookImpl(VariantCoreSession session, Test test) {
			this.session = session;
			this.test = test;
		}
		
		@Override
		public VariantCoreSession getSession() {
			return session;
		}

		@Override
		public Experience getTargetedExperience() {
			return targetedExperience;
		}

		@Override
		public Test getTest() {
			return test;
		}

		@Override
		public void setTargetedExperience(Experience experience) {
			targetedExperience = experience;
		}
		
	}		

	private VariantCore coreApi;

	/**
	 * Static singleton.
	 * Need package visibility for test facade.
	 */
	VariantRuntime(VariantCore coreApi) {
		this.coreApi = coreApi;
	}
	
	/**
	 * Target this session for all active tests.
	 * 
	 * 1. Find all the tests instrumented on this state - the state's instrumented test list (ITL),
	 *    i.e. the list of tests that may have to be targeted before we can resolve the state.
	 * 2. For each test on the ITL, if this test has already been traversed by this session and
	 *    disqualified, remove this test from the ITL as we won't really need to resolve it.
	 *    Otherwise, if this test has not yet been traversed by this session, post qualification
	 *    hooks. If client code disqualified a test, remove it from the ITL. If client code also
	 *    requested removal from targeting persister (TP), remove this test from targeting persister.
	 * 3. Some of these tests may be already targeted via the experience persistence mechanism.
	 *    Determine this already targeted subset.
     * 4. If such a subset exists, confirm that we are still able to resolve this test cell. 
     *    The reason we may not is, e.g., if two tests used to be covariant and the experience
     *    persister reports two already targeted variant experiences, but in a recent config 
     *    change these two tests are no longer covariant and hence we don't know how to resolve
     *    this test cell.
     * 5. If we're still able to resolve the pre-targeted vector, continue with the rest of the tests 
     *    on the view's list in the order they were defined, and target each test via the regular 
     *    targeting mechanism.
     * 5. If we're unable to resolve the pre-targeted vector, compute the minimal unresolvable subset
     *    and remove those experiences from the experience persistence.
     * 6. Given the new pre-targeted experience set, target the rest of the tests instrumented on
     *    this view via each test's regular targeting mechanism, in ordinal order.  Do not target
     *    tests that are OFF or disqualified tests. Instead, default them to control experiences.
     * 7. Resolve the path. For OFF tests, substitute non-control experiences with control ones.
	 * 
	 */
	private Map<String,String> targetSessionForState(VariantCoreStateRequestImpl req) {

		Schema schema = coreApi.getSchema();
		CoreSessionImpl session = (CoreSessionImpl) req.getSession();
		SessionScopedTargetingStabile targetingStabile = session.getTargetingStabile();
		State state = req.getState();
		
		// State must be in schema.
		State schemaState = schema.getState(state.getName());
		if (System.identityHashCode(schemaState) != System.identityHashCode(state)) 
			throw new VariantInternalException("State [" + state.getName() + "] is not in schema");
		
		// Re-qualify the tests we're about to traverse and that haven't yet been qualified
		// by this session, by triggering the qualification user hook.
		for (Test test: state.getInstrumentedTests()) {

			// OFF tests are neither qualified nor traversed.
			if (!test.isOn()) continue;
			
			if (!session.getTraversedTests().contains(test) && !session.getDisqualifiedTests().contains(test)) {
					
				TestQualificationHookImpl hook = new TestQualificationHookImpl(session, test);
				coreApi.getUserHooker().post(hook);
				
				if (!hook.qualified) {
					session.addDisqualifiedTest(test);
					if (hook.removeFromTT) targetingStabile.remove(test.getName());
				}
				else {
					// If this test is on, add it to the traversed list.
					session.addTraversedTest(test);
				}
			}
		}
		
		// Pre-targeted experiences from the targeting stabile. Keep original order for
		// test determinism.
		LinkedHashSet<Experience> alreadyTargetedExperiences = new LinkedHashSet<Experience>(targetingStabile.getAllAsExperiences(schema));
		
		// Remove from the pre-targeted experience list the experiences corresponding to currently
		// disqualified tests: we won't need to resolve them anyway.
		Iterator<Experience> alreadyTargetedExperiencesIterator = alreadyTargetedExperiences.iterator();
		while (alreadyTargetedExperiencesIterator.hasNext()) {			
			Experience e = alreadyTargetedExperiencesIterator.next();
			if (session.getDisqualifiedTests().contains(e.getTest())) 
				alreadyTargetedExperiencesIterator.remove();
		}
		
		// If not empty, alreadyTargetedEperience least contains experiences we need to resolve for.
		if (!alreadyTargetedExperiences.isEmpty()) {
			
			// Resolvable?  If not, find largest resolvable subset and discard the rest.
			Collection<Experience> minUnresolvableSubvector = 
					minUnresolvableSubvector(alreadyTargetedExperiences);
			
			if (minUnresolvableSubvector.isEmpty()) {
				if (LOG.isDebugEnabled()) LOG.debug("Targeting tracker resolvable for session [" + session.getId() + "]");
			}
			else {
				for (Experience e: minUnresolvableSubvector) targetingStabile.remove(e.getTest().getName());

				LOG.info(
						"Targeting tracker not resolvable for session [" + session.getId() + "]. " +
						"Discarded experiences [" + StringUtils.join(minUnresolvableSubvector.toArray()) + "].");
			}
		
		}
		
		// Actual experience vector we'll end up resolving will be different from the content of the targeting stabile
		// because OFF tests (and potentially disqualified tests) retain their entry in targeting tracker, even though
		// we'll sub that with control for actual resolution.
		ArrayList<Experience> vector = new ArrayList<Experience>();
		
		// First add all from from targeting stabile.
		for (Experience e: targetingStabile.getAllAsExperiences(schema)) {

			if (!e.getTest().isOn()) {
				Experience ce = e.getTest().getControlExperience();
				vector.add(ce);
				if (LOG.isTraceEnabled()) {
					LOG.trace(
							"Session [" + session.getId() + "] recognized persisted experience [" + e +"]" +
							" but substituted control experience [" + ce + "] because test is OFF");
				}													
			}
			else if (session.getDisqualifiedTests().contains(e.getTest())) {
				Experience ce = e.getTest().getControlExperience();
				vector.add(ce);
				if (LOG.isTraceEnabled()) {
					LOG.trace(
							"Session [" + session.getId() + "] recognized persisted experience [" + e +"]" +
							" but substituted control experience [" + ce + "] because test is disqualified");
				}													
			}
			else {
				vector.add(e);
				if (LOG.isTraceEnabled()) {
					LOG.trace(
							"Session [" + session.getId() + "] honored persisted experience [" + e + "]");
				}									
			}
		}
		
		// Then target and add the rest from the ITL.
		for (Test test: state.getInstrumentedTests()) {
						
			if (targetingStabile.get(test.getName()) == null) {
								
				if (!test.isOn()) {
					Experience e = test.getControlExperience();
					vector.add(e);
					if (LOG.isTraceEnabled()) {
						LOG.trace(
								"Session [" + session.getId() + "] temporarily targeted for OFF test [" + 
								test.getName() +"] with control experience [" + e.getName() + "]");
					}
				}
				else if (session.getDisqualifiedTests().contains(test)) {
					Experience e = test.getControlExperience();
					vector.add(e);
					if (LOG.isTraceEnabled()) {
						LOG.trace(
								"Session [" + session.getId() + "] temporarily targeted for disqualified test [" + 
								test.getName() +"] with control experience [" + e.getName() + "]");
					}										
				}
				else if (isTargetable(test, targetingStabile.getAllAsExperiences(schema))) {
					// Target this test. First post possible user hook listeners.
					TestTargetingHookImpl hook = new TestTargetingHookImpl(session, test);
					coreApi.getUserHooker().post(hook);
					Experience targetedExperience = hook.targetedExperience;
					// If no listeners or no action by client code, do the random default.
					if (targetedExperience == null) {
						targetedExperience = new TestTargeterDefault().target(test, session);
					}
										
					vector.add(targetedExperience);
					targetingStabile.add(targetedExperience, System.currentTimeMillis());
					if (LOG.isTraceEnabled()) {
						LOG.trace(
								"Session [" + session.getId() + "] targeted for test [" + 
								test.getName() +"] with experience [" + targetedExperience.getName() + "]");
					}

				}
				else {
					Experience e = test.getControlExperience();
					vector.add(e);
					targetingStabile.add(e, System.currentTimeMillis());
					if (LOG.isTraceEnabled()) {
						LOG.trace(
								"Session [" + session.getId() + "] targeted for untargetable test [" + 
								test.getName() +"] with control experience [" + e.getName() + "]");
					}
				}
			}
		}
				
		// vector at this point contains the actual experiences to be resolved, which may be different from what's in TP.
		return resolveState(state, vector);
		
	}

	/**
	 * Find the shortest sub-vector V' of the input vector V, so that V\V' is resolvable.
	 * A vector is resolvable if the current schema contains a variant for every view where it is relevant.
	 * A vector is relevant to a view if at least one of its experiences is instrumented.
	 * We do not alter semantics for OFF tests in this algorithm.
     *
	 * 1. Build a set of views relevant to the input.
	 * 2. For each relevant view, try to resolve the input vector.
	 * 3. As we go, maintain a set of views which failed to resolve, Sv
	 * 4. If the size of Sv is 0, return empty list.
	 * 5. Otherwise, create a list of tests St instrumented on Sv.
	 * 6. For each test T in St: remove it from the input vector and cal
	 *    this method recursively with the resulting vector.
	 * 7. Find the shortest result (take any if there's more than one) and
	 *    add to it the corresponding T. Return that collection.
	 * 
	 * Package scope for testing.
	 * 
	 * TODO: GETME.
	 * 
	 * @param coordinates
	 * @return the list of experiences from the input vector.
	 */
	Collection<Experience> minUnresolvableSubvector(Collection<Experience> vector) {
		
		Collection<Experience> result = new ArrayList<Experience>();
			
		// 1. Build a set of all instrumented states.
		LinkedHashSet<State> instrumentedStates = new LinkedHashSet<State>();
		for (Experience e: vector) {
			for (OnState tov: e.getTest().getOnStates()) {
				if (!tov.getState().isNonvariantIn(e.getTest())) {
					instrumentedStates.add(tov.getState());
				}
			}
		}
			
		// 2,3. Try to resolve them all.
		ArrayList<State> unresolvedStates = new ArrayList<State>();
		for (State state: instrumentedStates) {
			if (resolveState(state, vector) == null) {
				unresolvedStates.add(state);
			}
		}
				
		// 4.
		if (unresolvedStates.isEmpty()) return result;
		
		// 5. Build the set of tests instrumented on any of the unresolved states.
		HashSet<Test> testsInstumentedOnUnresolvedStates = new HashSet<Test>();  
		for (State uv: unresolvedStates) {
			for (Test t: uv.getInstrumentedTests()) {
				if (!uv.isNonvariantIn(t)) testsInstumentedOnUnresolvedStates.add(t);
			}
		}
		
		// 6. 
		int shortestLength = vector.size();
		Experience shortestExperience = null;
		Collection<Experience> shortestSubvector = null;
		
		for (Experience inputExperience: vector) {
		
			if (!testsInstumentedOnUnresolvedStates.contains(inputExperience.getTest())) continue;
			
			Collection<Experience> newInputVector = new ArrayList<Experience>(vector);
			newInputVector.remove(inputExperience);
			Collection<Experience> newMinUnresolvableSubvector = minUnresolvableSubvector(newInputVector);
			if (shortestLength > newMinUnresolvableSubvector.size()) {
				shortestLength = newMinUnresolvableSubvector.size();
				shortestExperience = inputExperience;
				shortestSubvector = newMinUnresolvableSubvector;
			}
		}
	
		result.add(shortestExperience);
		result.addAll(shortestSubvector);
		return result;
	}

	/**
	 * Can a test be targeted to a variant experience, given a set of already targeted experiences.
	 * 
	 * 1. Confirm that the already targeted set is resolvable.
	 * 2. Confirm that the input test isn't yet targeted.
	 * 3. Create a vector containing the already targeted experiences and add to it an arbitrary
	 *    variant experience from the input test.
	 * 4. return true if new vector is resolvable.
	 * 
	 * @param test set of tests.  We require set to guarantee no duplicates.
	 * @return
	 */
	boolean isTargetable(Test test, Collection<Experience> alreadyTargetedExperiences) {
		
		for (Experience e: alreadyTargetedExperiences) {
			if (test.equals(e.getTest())) 
				throw new VariantInternalException("Input test [" + test + "] is already targeted");
		}
		
		if (!minUnresolvableSubvector(alreadyTargetedExperiences).isEmpty()) {
			throw new VariantInternalException(
					"Input set [" +
				    StringUtils.join(alreadyTargetedExperiences, ",") +
				    "] is already unresolvable");
		}
		
		ArrayList<Experience> vector = new ArrayList<Experience>(alreadyTargetedExperiences);
		
		for (Experience e: test.getExperiences()) {
			if (!e.isControl()) {
				vector.add(e);
				break;
			}
		}
		
		return minUnresolvableSubvector(vector).isEmpty();
	}

	/**
	 * Find a view variant for a given set of experiences. It is caller's responsibility
	 * to ensure that all experiences are independent, i.e. the input vector does not contain
	 * a pair e1,e2 such that <code>e1.getTest().equals(e2.getTest())</code>
	 * 
	 * 0. Verify that vector has at least 1 experience. 
	 * 1. Resort the experience vector in ordinal order. 
	 * 2. As we go, remove experiences that are either control in their test, or their test
	 *    is not instrumented on the given view.
	 * 3. Keep track if any of the experiences in vector were discarded in step 2.
	 * 4. In the sorted list, the last experience corresponds to the highest order test Th.
	 * 5. Find the Test.OnView object corresponding to Th and the given view.  If does not
	 *    exist, return null.
	 * 6. Find the Test.OnView.Variant object there.  If does not exist, return null;
	 * 
	 * TODO: GETME.
	 * Package scope for testing
	 * 
	 * @param state
	 * @param vector
	 * @return params map
	 */
	Map<String,String> resolveState(State state, Collection<Experience> vector) {

		if (vector.size() == 0) 
			throw new VariantInternalException("No experiences in input vector");
		
		ArrayList<Experience> sortedList = new ArrayList<Experience>(vector.size());
		for (Test t: coreApi.getSchema().getTests()) {
			boolean found = false;
			for (Experience e: vector) {
				if (e.getTest().equals(t)) {
					if (found) {
						throw new VariantInternalException("Duplicate test [" + t + "] in input");
					}
					else {
						found = true;
						if (!e.isControl() && state.isInstrumentedBy(e.getTest()) && !state.isNonvariantIn(e.getTest())) 
							sortedList.add(e);
					}
				}
			}
		}

		// All experiences were control or uninstumented?
		if (sortedList.size() == 0) return state.getParameterMap();
		
		Test highOrderTest = sortedList.get(sortedList.size() - 1).getTest();
		TestOnStateImpl tov = (TestOnStateImpl) highOrderTest.getOnView(state);
		
		if (tov == null) return null;
		
		Test.OnState.Variant variant = tov.variantSpace().get(sortedList);
		return variant == null ? null : variant.getParameterMap();	
	
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Implementation of {@link Variant#targetForState(VariantCoreSession, State, Object...)}
	 * @param ssn
	 * @param view
	 * @return
	 */
	public VariantCoreStateRequestImpl targetSessionForState(CoreSessionImpl ssn, StateImpl state) {

		// Resolve the path and get all tests instrumented on the state.
		VariantCoreStateRequestImpl result = new VariantCoreStateRequestImpl(ssn, state);
		
		Map<String,String> resolvedParams = targetSessionForState(result);		
		result.setResolvedParameters(resolvedParams);
		
		// Targeting stabile contains targeted experiences.
		SessionScopedTargetingStabile targetingStabile = ssn.getTargetingStabile();
		
		if (LOG.isTraceEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Session [").append(ssn.getId()).append("] resolved state [").append(state.getName()).append("] as [");
			sb.append(VariantStringUtils.toString(resolvedParams,","));
			sb.append("] for experience vector [").append(StringUtils.join(targetingStabile.getAll().toArray(), ",")).append("]");
			LOG.trace(sb.toString());
		}   
			
		// Create the state serve event if there are any tests instrumented on this state.
		if (state.getInstrumentedTests().isEmpty()) {
			
			if (LOG.isTraceEnabled()) {
				LOG.trace(
						"Session [" + ssn.getId() + "] requested state [" + 
						state.getName() +"] that does not have any instrumented tests.");
			}   
		}
		else if (result.getTargetedExperiences().isEmpty()) {
			if (LOG.isTraceEnabled()) {
				LOG.trace(
						"Session [" + ssn.getId() + "] requested state [" + 
						state.getName() +"] that does not have live tests.");
			}   			
		}
		else {
			// Real state hit.
			ssn.addTraversedState(state);
			result.createStateVisitedEvent();
		}
	
		return result;

	}
	
}

package com.variant.core.impl;

import java.util.ArrayList;
import java.util.Collection;
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
import com.variant.core.session.SessionScopedTargetingStabile;
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
	 * 1. Build the active test list (ATL). Start by putting on it all the active tests instrumented 
	 *    on this state, i.e. the tests which may require targeting as part of state resolution. This 
	 *    list comprises all declared instrumentations, as defined by State.getInstrumentedTests(),
	 *    minus the OFF tests. Note that we include in this non-variant (NV) instrumentation, i.e. if the
	 *    first state hit is a NV test, we will target.
	 *    
	 * 2. For each test on the ATL, if it has already been disqualified by this session, remove it
	 *    from the ATL, because it's not active and we won't need to resolve it. Otherwise, if this test 
	 *    has not yet been traversed by this session, post qualification hooks. If client code disqualifies 
	 *    the test, remove it from the ATL. If client code also requested removal from targeting tracker (TT), 
	 *    remove this test from TT, if present.
	 *    
	 * 3. Remaining tests are active and must be targeted, if not yet targeted. Each active test is one of:
	 *      *) Already targeted in this session, if it's in the TT and on the traversed tests list (TTL).
	 *      *) Pre-targeted, if it's in TT only, but not in TTL. Need to confirm targeting,
	 *         see step 4 below.
	 *      *) Not yet targeted, or free, if it's in neither in TTL nor in TT.  Will be targeted after
	 *         the pre-targets are confirmed.
     * 
     * 4. If we still have pre-targeted tests, confirm that their targeting is compatible with this
     *    session's current targeting. In other words, confirm that the test cell given by the combination
     *    of the targeted and the pre-targeted experiences is resolvable in the current schema.
     *    The reason we may not is, e.g., if two tests used to be covariant and the TT contains a variant
     *    experience for one, and the currently targeted list contains a variant experience for the other.
     *    If, in the current schema, they are no longer covariant, this combination is no longer resolvable.
     *    
     * 5. If pre-targeted list is not compatible with the currently targeted list, compute the maximal 
     *    compatible subset, i.e. one that of possible compatible subsets is the longest. Remove from the
     *    TT those experiences that did not make the compatible subset. These tests will be treated as 
     *    untargeted, i.e. as if they had never been targeted in the past.
     *    
     * 6. For each test that is on ATL but not in TTL: if test is targetable, post the targeting hook 
     *    and, if the hook returns an experience, add it to TT. If the test is not targetable, target
     *    for control.
     *    
     * 7. Resolve the state params for the resulting list of active experiences.
	 * 
	 * 
	 * TODO: IP.
	 */
	private Map<String,String> targetSessionForState(VariantCoreStateRequestImpl req) {

		Schema schema = coreApi.getSchema();
		CoreSessionImpl session = (CoreSessionImpl) req.getSession();
		SessionScopedTargetingStabile targetingStabile = session.getTargetingStabile();
		State state = req.getState();
		
		// State must be in current schema.
		State schemaState = schema.getState(state.getName());
		if (System.identityHashCode(schemaState) != System.identityHashCode(state)) 
			throw new VariantInternalException("State [" + state.getName() + "] is not in schema");
		
		// 1. Build the active test list.
		ArrayList<Test> activeTestList = new ArrayList<Test>();
		for (Test test: state.getInstrumentedTests()) {
			if (test.isOn()) activeTestList.add(test);
		}

		// 2. Remove from ATL those already disqualified, qualify others and remove if disqualified.
		for (Iterator<Test> iter = activeTestList.iterator(); iter.hasNext();) {
			Test test = iter.next();
			if (session.getDisqualifiedTests().contains(test)) {
				iter.remove();
			}
			else if (!session.getTraversedTests().contains(test) && !qualifyTest(test, session)) {
				iter.remove();
			}
		}
		
		// 3. ATL contains only active, qualified tests at this point. Build the already targeted 
		//    (in TT and in TTL), pre-targeted (in TT but not in TTL) and free (neither) experience lists.
		LinkedHashSet<Experience> alreadyTargeted = new LinkedHashSet<Experience>();
		LinkedHashSet<Experience> preTargeted = new LinkedHashSet<Experience>();
		LinkedHashSet<Test> free = new LinkedHashSet<Test>();
		
		for (Test test: activeTestList) {
			if (session.getTraversedTests().contains(test)) {
				// Already traversed, hence must be already targeted.
				Experience exp = targetingStabile.getAsExperience(test.getName(), schema);
				if (exp == null)
					throw new VariantInternalException(
							"Active traversed test [" + test.getName() + "] not in targeting stabile");
				alreadyTargeted.add(exp);
			}
			else  {
				// Not yet traversed. Add to the pre-targeted experience list, if in TT. If not in TT,
				// it's a free test that will be targeted after the pre-targets.
				Experience exp = targetingStabile.getAsExperience(test.getName(), schema);
				if (exp == null) free.add(test); 
				else preTargeted.add(exp);
				
				session.addTraversedTest(test);
			}
		}
				
		// 4. If not empty, preTargeted list contains experiences we need to confirm are
		//    compatible with those already targeted, i.e. in TT and in TTL
		if (!preTargeted.isEmpty()) {
			
			// Resolvable?  If not, find max resolvable subset and discard the rest.
			Collection<Experience> minUnresolvableSubvector = minUnresolvableSubvector(alreadyTargeted, preTargeted);
			
			if (minUnresolvableSubvector.isEmpty()) {
				if (LOG.isDebugEnabled()) 
					LOG.debug("Targeting tracker resolvable for session [" + session.getId() + "]");
			}
			else {
				// 5. Remove unresolvable experiences from TT. We don't need to keep track of them because they
				//    will not become targetable in this session.
				for (Experience e: minUnresolvableSubvector) targetingStabile.remove(e.getTest().getName());
				LOG.info(
						"Targeting tracker not resolvable for session [" + session.getId() + "]. " +
						"Discarded experiences [" + StringUtils.join(minUnresolvableSubvector.toArray()) + "].");
			}
		}

		// The actual experience vector we will be resolving is: alreadyTargeted + preTargeted + possibly
		// free, if they are targetable, and happen to fall on a non-control experience.
		// 
		ArrayList<Experience> vector = new ArrayList<Experience>();
		vector.addAll(alreadyTargeted);
		vector.addAll(preTargeted);
		
		// Target free tests.  They are already on the ATL.
		for (Test ft: free) {

			if (isTargetable(ft, vector)) {
				// Target this test. First post targeting hooks.
				TestTargetingHookImpl hook = new TestTargetingHookImpl(session, ft);
				coreApi.getUserHooker().post(hook);
				Experience targetedExperience = hook.targetedExperience;
				// If no listeners or no action by client code, do the random default.
				if (targetedExperience == null) {
					targetedExperience = new TestTargeterDefault().target(ft, session);
				}
										
				if (!targetedExperience.isControl()) vector.add(targetedExperience);
				targetingStabile.add(targetedExperience);
				
				if (LOG.isTraceEnabled()) {
					LOG.trace(
							"Session [" + session.getId() + "] targeted for test [" + 
							ft.getName() +"] with experience [" + targetedExperience.getName() + "]");
				}

			}
			else {
				Experience e = ft.getControlExperience();
				vector.add(e);
				targetingStabile.add(e);
				if (LOG.isTraceEnabled()) {
					LOG.trace(
							"Session [" + session.getId() + "] targeted for untargetable test [" + 
							ft.getName() +"] with control experience [" + e.getName() + "]");
				}
			}
		}
				
		// vector at this point contains active, non-control experiences to be resolved.
		return resolveState(state, vector);
		
	}

	/**
	 * Qualify a test by posting the qualification hook.
	 * If not qualified, add to session's disqualified tests list and,
	 * if requested by the hook listener, remove from the targeting tracker.
	 * @param session
	 * @param test
	 */
	private boolean qualifyTest(Test test, CoreSessionImpl session) {

		/**
		 * 
		 */
		class TestQualificationHookImpl implements TestQualificationHook {
			
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

		TestQualificationHookImpl hook = new TestQualificationHookImpl(session, test);
		coreApi.getUserHooker().post(hook);

		if (!hook.qualified) {
			session.addDisqualifiedTest(test);
			if (hook.removeFromTT) session.getTargetingStabile().remove(test.getName());
		}				
		
		return hook.qualified;
	}
	
	/**
	 * Is a vector resolvable? I.e., does the current schema contains a variant def for every state 
	 * where it is relevant. A vector is relevant to a state if at least one of its tests is 
	 * instrumented in a variantful fashion.
	 * @param vector
	 * @return
	 */
	boolean isResolvable(Collection<Experience> vector) {

		// Build the set of unique relevant states. 
		// Many of them may be dupes, so no need to attempt to resolve all of them.
		LinkedHashSet<State> relevantStates = new LinkedHashSet<State>();
		for (Experience e: vector) {
			for (OnState tos: e.getTest().getOnStates()) {
				if (!tos.getState().isNonvariantIn(e.getTest())) {
					relevantStates.add(tos.getState());
				}
			}
		}

		for (State state: relevantStates) {
			// Only try the experiences of tests that are insturmented on state.
			// Otherwise resolveState() with throw an exception.
			Collection<Experience> instumentedVector = new ArrayList<Experience>();
			for (Experience e: vector) {
				if (state.isInstrumentedBy(e.getTest())) 
					instumentedVector.add(e);
			}
			if (resolveState(state, instumentedVector) == null) return false;
		}
		
		return true;
	}
	
	/**
	 * Given a resolvable experience vector V and arbitrary experience vector W, find longest sub-vector w of W,
	 * so that V+w is resolvable. 
     *
	 * Package scope for testing.
	 * 
	 * @param V
	 * @param W becomes w, as a side effect of this call. V+w is the resolvable.
	 * @return W minus w, i.e remainder of W that makes V+W unresolvable.
	 */
	Collection<Experience> minUnresolvableSubvector(Collection<Experience> v, Collection<Experience> w) {
					
		// V must be resolvable.
		if (!isResolvable(v))
			throw new VariantInternalException(
					String.format("Input vector [%s] must be resolvable, but is not", VariantStringUtils.toString(v, ",")));

		// W must not contain experiences that contradict those in V
		for (Experience ew: w) {
			for (Experience ev: v) {
				if (ew.getTest().equals(ev.getTest()))
					throw new VariantInternalException(
							String.format("Experience [%s] in second argument contradicts experience [%s] in first argument", ew, ev));
			}
		}
		
		Collection<Experience> currentlyResolvable = new LinkedHashSet<Experience>(v);
		Collection<Experience> remainder = new LinkedHashSet<Experience>();

		for (Iterator<Experience> iter = w.iterator(); iter.hasNext();) {
			Experience e = iter.next();
			Collection<Experience> toTry = new LinkedHashSet<Experience>(currentlyResolvable);
			toTry.add(e);
			if (isResolvable(toTry)) {
				currentlyResolvable.add(e);
				iter.remove();
			}
			else {
				remainder.add(e);
			}
		}
		return remainder;
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

		// alreadyTargetedExperiences should not contain an experience for the input test.
		for (Experience e: alreadyTargetedExperiences) {
			if (test.equals(e.getTest())) 
				throw new VariantInternalException("Input test [" + test + "] is already targeted");
		}

		if (!isResolvable(alreadyTargetedExperiences))
			throw new VariantInternalException(
					"Input vector [" + StringUtils.join(alreadyTargetedExperiences, ",") + "] is already unresolvable");
		
		// Find some non-control experience
		ArrayList<Experience> vector = new ArrayList<Experience>();		
		for (Experience e: test.getExperiences()) {
			if (!e.isControl()) {
				vector.add(e);
				break;
			}
		}
		
		return minUnresolvableSubvector(alreadyTargetedExperiences, vector).isEmpty();
	}

	/**
	 * Find a view variant for a given set of experiences. It is caller's responsibility
	 * to ensure that 
	 *   o) all experiences are independent, i.e. the input vector does not contain
	 *      a pair e1,e2 such that <code>e1.getTest().equals(e2.getTest())</code>
	 *   o) there are no control experiences
	 *   o) each experience's test is instrumented.
	 * 
	 * 0. Verify that vector has at least 1 experience. 
	 * 1. Resort the experience vector in ordinal order. 
	 * 2. As we go, remove experiences that are either control in their test, or their test
	 *    is not instrumented on the given state.
	 * 3. Keep track if any of the experiences in vector were discarded in step 2.
	 * 4. In the sorted list, the last experience corresponds to the highest order test Th.
	 * 5. Find the Test.OnState object corresponding to Th and the given state.  If does not
	 *    exist, return null.
	 * 6. Find the Test.OnState.Variant object there.  If does not exist, return null;
	 * 
	 * Package scope for testing
	 * 
	 * @param state
	 * @param vector
	 * @return params map
	 */
	Map<String,String> resolveState(State state, Collection<Experience> vector) {

		
		ArrayList<Experience> sortedList = new ArrayList<Experience>(vector.size());
			
		for (Test t: coreApi.getSchema().getTests()) {
			boolean found = false;
			for (Experience e: vector) {
				if (e.getTest().equals(t)) {
					if (found) {
						throw new VariantInternalException("Duplicate test [" + t + "] in input vector");
					}
					else {
						
						if (e.isControl())
							throw new VariantInternalException("Control experience [" + e + "] in input vector");
						
						if (!state.isInstrumentedBy(e.getTest()))
							throw new VariantInternalException("Uninstrumented test [" + e + "] in input vector");
						
						found = true;

						// Non-variant instrumentation are resolved for, as control.
						if (!state.isNonvariantIn(e.getTest())) { 
							sortedList.add(e);
							// Continue down the vector, to ensure that there's no other experience for this test.
						}
					}
				}
			}
		}
		
		// If no variant experiences (all input experiences were control or uninstrumented or off.
		// on the input state), return the state params.
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

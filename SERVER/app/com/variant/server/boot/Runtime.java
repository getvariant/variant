package com.variant.server.boot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.impl.ServerError;
import com.variant.core.util.CollectionsUtils;
import com.variant.core.util.StringUtils;
import com.variant.core.session.SessionScopedTargetingStabile;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Variation;
import com.variant.core.schema.Variation.Experience;
import com.variant.core.schema.Variation.OnState;
import com.variant.core.schema.impl.StateImpl;
import com.variant.core.schema.impl.VariationImpl;
import com.variant.core.schema.impl.VariationOnStateImpl;
import com.variant.server.api.Session;
import com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent;
import com.variant.server.impl.SessionImpl;
import com.variant.server.impl.VariationQualificationLifecycleEventImpl;
import com.variant.server.impl.VariationQualificationLifecycleEventPostResultImpl;
import com.variant.server.impl.VariationTargetingLifecycleEventImpl;
import com.variant.server.impl.VariationTargetingLifecycleEventPostResultImpl;
import com.variant.server.schema.SchemaGen;

/**
 * Stateless runtime methods.
 * 
 * @author Igor.
 *
 */
public class Runtime {

	private static final Logger LOG = LoggerFactory.getLogger(Runtime.class);
	
	final private SchemaGen schema;
	
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
	 * 3. Remaining tests are active and qualified and may have to be targeted. Each active test is one of:
	 *      *) Already targeted in this session, if it's in the TT and on the traversed tests list (TTL).
	 *      *) Pre-targeted, if it's in TT only, but not in TTL. Need to confirm targeting,
	 *         see step 4 below.
	 *      *) Not yet targeted, or free, if it's in neither in TTL nor in TT.  Will be targeted after
	 *         the pre-targets are confirmed.
     * 
     * 4. If we still have pre-targeted tests, confirm that their targeting is compatible with this
     *    session's current targeting. In other words, confirm that the test cell given by the combination
     *    of the targeted and the pre-targeted experiences is resolvable in the current schema.
     *    The reason we may not is, e.g., if two tests used to be conjoint and the TT contains a variant
     *    experience for one, and the currently targeted list contains a variant experience for the other.
     *    If, in the current schema, they are no longer conjoint, this combination is no longer resolvable.
     *    
     * 5. If pre-targeted list is not compatible with the currently targeted list, compute the maximal 
     *    compatible subset, i.e. one that of possible compatible subsets is the longest. Remove from the
     *    TT those experiences that did not make the compatible subset. These tests will be treated as 
     *    untargeted, i.e. as if they had never been targeted in the past.
     *    
     * 6. For each test that is on ATL but not in TTL: if test is targetable, post the targeting hook Ò
     *    and, if the hook returns an experience, add it to TT. If the test is not targetable, target
     *    for control.
     *    
     * 7. Resolve the state params for the resulting list of active experiences.
	 * 
	 * TODO: IP.
	 * 
	 */
	private void target(SessionImpl session, StateImpl state) {
		
		// State must be in current schema.
		State schemaState = schema.getState(state.getName()).get();
		
		if (System.identityHashCode(schemaState) != System.identityHashCode(state)) 
			throw new ServerExceptionInternal("State [" + state.getName() + "] is not in schema");
				
		// 1. Build the active test list.
		ArrayList<Variation> activeTestList = new ArrayList<Variation>();
		for (Variation test: state.getInstrumentedVariations()) {
			if (test.isOn()) activeTestList.add(test);
		}

		// 2. Remove from ATL those already disqualified, qualify others and remove if disqualified.
		for (Iterator<Variation> iter = activeTestList.iterator(); iter.hasNext();) {
			VariationImpl test = (VariationImpl) iter.next();
			if (session.getDisqualifiedVariations().contains(test)) {
				iter.remove();
			}
			else if (!session.getTraversedVariations().contains(test) && !qualifyTest(test, session)) {
				iter.remove();
			}
		}
		
		// 3. ATL contains only active, qualified tests at this point. Build the already targeted 
		//    (in TT and in TTL), pre-targeted (in TT but not in TTL) and free (neither) experience lists.
		LinkedHashSet<Experience> alreadyTargeted = new LinkedHashSet<Experience>();
		LinkedHashSet<Experience> preTargeted = new LinkedHashSet<Experience>();
		LinkedHashSet<Variation> free = new LinkedHashSet<Variation>();
		
		for (Variation test: activeTestList) {
			if (session.getTraversedVariations().contains(test)) {
				
				// Already traversed, hence must be already targeted.
				Experience exp = session.getTargetingStabile().getAsExperience(test.getName(), schema);
				if (exp == null)
					throw new ServerExceptionInternal(
							"Active traversed test [" + test.getName() + "] not in targeting stabile");

				// It's a user error to hit an undefined state in an active experience.
				if (exp.isPhantom(state))  {
					throw new ServerExceptionLocal(ServerError.STATE_UNDEFINED_IN_EXPERIENCE, exp.toString(), state.getName());
				}

				alreadyTargeted.add(exp);
			}
			else  {
				// Not yet traversed. Add to the pre-targeted experience list, if in TT. If not in TT,
				// it's a free test that will be targeted after the pre-targets.
				Experience exp = session.getTargetingStabile().getAsExperience(test.getName(), schema);
				
				if (exp == null) {
					free.add(test); 
				}
				else {
					// We have a pre-targeted experience for a test that we have just hit.
					// If this is an undefined state, we need to discard the pre-targeted experience
					// and treat this as free.
					if (!exp.isPhantom(state)) {
						preTargeted.add(exp);						
					}
					else {
						session.getTargetingStabile().remove(test.getName());
						free.add(test);
					}
				}
				
				session.coreSession().addTraversedVariation(test);
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
				// 5. Replace the unresolvable experiences from TT with control.
				for (Experience e: minUnresolvableSubvector) {
					session.getTargetingStabile().add(e.getVariation().getControlExperience());
				}
				LOG.info(
						"Targeting tracker not resolvable for session [" + session.getId() + "]. " +
						"Discarded experiences [" + StringUtils.join(minUnresolvableSubvector.toArray(), ",") + "].");
			}
		}

		// The actual experience vector we will be resolving is: alreadyTargeted + preTargeted + possibly
		// free, if they are targetable, and happen to fall on a non-control experience.
		// 
		ArrayList<Experience> vector = new ArrayList<Experience>();
		vector.addAll(alreadyTargeted);
		vector.addAll(preTargeted);
		
		// Target free tests.  They are already on the ATL.
		for (Variation ft: free) {

			if (isTargetable(ft, state, vector)) {
				// Target this test. First post targeting hooks.
				VariationTargetingLifecycleEventImpl event = new VariationTargetingLifecycleEventImpl(session, ft, state);
				VariationTargetingLifecycleEventPostResultImpl hookResult = (VariationTargetingLifecycleEventPostResultImpl) schema.hooksService().post(event);
				Experience targetedExperience = hookResult.getTargetedExperience();

				if (LOG.isTraceEnabled()) {
					LOG.trace(
							"Session [" + session.getId() + "] targeted for test [" + 
							ft.getName() +"] with experience [" + targetedExperience.getName() + "]");
				}

				vector.add(targetedExperience);
				session.getTargetingStabile().add(targetedExperience);				

			}
			else {
				Experience e = ft.getControlExperience();
				session.getTargetingStabile().add(e);
				if (LOG.isTraceEnabled()) {
					LOG.trace(
							"Session [" + session.getId() + "] targeted for untargetable test [" + 
							ft.getName() +"] with control experience [" + e.getName() + "]");
				}
			}
		}

		// If all went well, we must be resolvable.
		Optional<StateVariant> resolvedVariantOpt = resolveState(state, vector);
		if (resolvedVariantOpt == null) throw new ServerExceptionInternal(
				"Vector [" + CollectionsUtils.toString(vector, ",") + "] is unresolvable");
		
		// AOK. Create the state request object.
		session.setStateRequest(state, resolvedVariantOpt);
	}

	/**
	 * Qualify a test by posting the qualification hook.
	 * If not qualified, add to session's disqualified tests list and,
	 * if requested by the hook listener, remove from the targeting tracker.
	 * @param session
	 * @param test
	 */
	private boolean qualifyTest(VariationImpl var, SessionImpl session) {
		

		VariationQualificationLifecycleEvent event = new VariationQualificationLifecycleEventImpl(session, var);
		VariationQualificationLifecycleEventPostResultImpl hookResult = (VariationQualificationLifecycleEventPostResultImpl) schema.hooksService().post(event);

		if (!hookResult.isQualified()) {
			session.addDisqualifiedTest(var);
			if (hookResult.isRemoveFromTT()) session.getTargetingStabile().remove(var.getName());
		}				
		
		return hookResult.isQualified();
	}
	
	/**
	 * Is a vector resolvable? I.e., does the current schema contain a variant def for every state 
	 * where it is relevant. A vector is relevant to a state if at least one of its tests is 
	 * instrumented in a variantful fashion.
 	 * NB: See bug #102.
	 * @param vector
	 * @return
	 */
	boolean isResolvable(Collection<Experience> vector) {

		// Build the set of unique relevant states. Set will take care of getting rid of duplicates.
		LinkedHashSet<State> relevantStates = new LinkedHashSet<State>();
		for (Experience e: vector) {
			if (e.isControl()) continue;
			for (OnState tos: e.getVariation().getOnStates()) {
				if (!e.isPhantom(tos.getState())) {
					relevantStates.add(tos.getState());
				}
			}
		}

		for (State state: relevantStates) {
			// Only try the experiences of tests that are insturmented and defined on state.
			// Otherwise resolveState() with throw an exception.
			Collection<Experience> instumentedVector = new ArrayList<Experience>();
			for (Experience e: vector) {
				if (!e.isControl() && state.isInstrumentedBy(e.getVariation()) && !e.isPhantom(state)) 
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
			throw new ServerExceptionInternal(
					String.format("Input vector [%s] must be resolvable, but is not", CollectionsUtils.toString(v, ",")));

		// W must not contain experiences that contradict those in V
		for (Experience ew: w) {
			for (Experience ev: v) {
				if (ew.getVariation().equals(ev.getVariation()))
					throw new ServerExceptionInternal(
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
			}
			else {
				iter.remove();    // Remove from W
				remainder.add(e); // Add to result
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
	boolean isTargetable(Variation var, State state, Collection<Experience> alreadyTargetedExperiences) {

		// alreadyTargetedExperiences should not contain an experience for the input test.
		for (Experience e: alreadyTargetedExperiences) {
			if (var.equals(e.getVariation())) 
				throw new ServerExceptionInternal("Input test [" + var + "] is already targeted");
		}

		if (!isResolvable(alreadyTargetedExperiences))
			throw new ServerExceptionInternal(
					"Input vector [" + StringUtils.join(alreadyTargetedExperiences, ",") + "] is already unresolvable");
		
		// Find some non-control, defined experience. Untargetable if none.
		ArrayList<Experience> vector = new ArrayList<Experience>();
		Experience  definedVariantExperience = null;
		for (Experience e: var.getExperiences()) {
			if (!e.isControl() && !e.isPhantom(state)) {
				definedVariantExperience = e;
				break;
			}
		}
		
		if (definedVariantExperience == null) return false;
		
		vector.add(definedVariantExperience);		
		return minUnresolvableSubvector(alreadyTargetedExperiences, vector).isEmpty();
	}

	/**
	 * Find a state variant for a given set of experiences, or coordinates. 
	 * It is caller's responsibility to ensure that 
	 *   o) all coordinate experiences are independent, i.e. the given cord collection does not contain
	 *      a pair e1,e2 such that <code>e1.getTest().equals(e2.getTest())</code>
	 *   o) there are no control experiences
	 *   o) each experience's test is instrumented.
	 * 
	 * 0. Verify that the coordinates vector has at least 1 experience. 
	 * 1. Resort the coordinate vector in ordinal order. 
	 * 2. As we go, remove experiences that are either control in their test, or their test
	 *    is not instrumented on the given state.
	 * 3. Keep track if any of the experiences in vector were discarded in step 2.
	 * 4. In the sorted list, the last experience corresponds to the highest order test Th.
	 * 5. Find the Test.OnState object corresponding to Th and the given state.  If does not
	 *    exist, this vector is unresolvable - return null.
	 * 6. Find the Test.OnState.Variant object there.  If does not exist, return null;
	 * 
	 * Package scope for testing
	 * 
	 * @param state
	 * @param coord
	 * @return An Optional, containing the resolved state variant or empty, in case of trivial resolution, which
	 *         is resolvable, but no state variant, — or null, if not resolvable.
	 */
	Optional<StateVariant> resolveState(State state, Collection<Experience> coord) {

		ArrayList<Experience> sortedList = new ArrayList<Experience>(coord.size());
			
		for (Variation var: schema.getVariations()) {
			boolean found = false;
			for (Experience e: coord) {
				if (e.getVariation().equals(var)) {
					if (found) {
						throw new ServerExceptionInternal("Duplicate variation [" + var + "] in coordinate vector");
					}
					else {
						
						if (!state.isInstrumentedBy(e.getVariation()))
							throw new ServerExceptionInternal("Uninstrumented variation [" + e + "] in coordinate vector");

						if (e.isPhantom(state))
							throw new ServerExceptionInternal("Undefined experience [" + e + "] in coordinate vector");

						found = true;

						if (!e.isControl()) { 
							sortedList.add(e);
							// Continue down the vector, to ensure that there's no other experience for this test.
						}
					}
				}
			}
		}
		
		boolean resolvable = false;
		Optional<StateVariant> resolvedVariantOpt = Optional.empty();
		
		if (sortedList.size() == 0) {
			// Trivial resolution.
			resolvable = true;
		}
		else {
			Variation highOrderVar = sortedList.get(sortedList.size() - 1).getVariation();
			Optional<Variation.OnState> vosOpt = highOrderVar.getOnState(state);
			if (vosOpt.isPresent()) {
				VariationOnStateImpl vos = (VariationOnStateImpl) vosOpt.get();
				resolvedVariantOpt = vos.getVariant(new HashSet<Experience>(sortedList));
				resolvable = resolvedVariantOpt.isPresent();
			}
		}
		return resolvable ? resolvedVariantOpt : null;
	}
	
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	public Runtime(SchemaGen schema) {
		this.schema = schema;
	}
	
	/**
	 * Implementation of {@link Variant#targetForState(VariantCoreSession, State, Object...)}
	 * @param ssn
	 * @param state
	 * @return
	 */
	public void targetForState(Session session, State state) {
		
		SessionImpl ssnImpl = (SessionImpl) session;
		StateImpl stateImpl = (StateImpl) state;
		
		// All the complexity happens here.
		target(ssnImpl, stateImpl);		
		
		// Targeting stabile contains targeted experiences.
		SessionScopedTargetingStabile targetingStabile = ssnImpl.getTargetingStabile();
		
		if (LOG.isTraceEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Session [").append(session.getId()).append("] resolved state [").append(state.getName()).append("] as [");
			boolean first = true;
			for (Map.Entry<String,String> e: session.getStateRequest().get().getResolvedParameters().entrySet()) {
				if (first) first = false;
				else sb.append(",");
				sb.append(e.getValue());
			}
			sb.append("] for experience vector [").append(StringUtils.join(targetingStabile.getAll().toArray(), ",")).append("]");
			LOG.trace(sb.toString());
		}   
			
		// Create the state visited event. Note that we will trigger one even for states
		// that don't have any live experiences in the state, but it will be an "orphan"
		// event without any experiences.
		ssnImpl.addTraversedState(stateImpl);
	}
	
}

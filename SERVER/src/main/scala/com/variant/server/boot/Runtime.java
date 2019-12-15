package com.variant.server.boot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.share.error.ServerError;
import com.variant.share.util.CollectionsUtils;
import com.variant.share.util.StringUtils;
import com.variant.share.session.SessionScopedTargetingStabile;
import com.variant.share.schema.State;
import com.variant.share.schema.StateVariant;
import com.variant.share.schema.Variation;
import com.variant.share.schema.Variation.Experience;
import com.variant.share.schema.Variation.OnState;
import com.variant.share.schema.impl.StateImpl;
import com.variant.share.schema.impl.VariationImpl;
import com.variant.share.schema.impl.VariationOnStateImpl;
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
	 * 1. Build the active variation list (AVL), which contains all instrumented variations,
	 *    as given by State.getInstrumentedVariations()) but excluding the offline variations.
	 *    
	 * 2. For each variation on the AVL:
	 *    • if it has already been disqualified by this session, remove it from the AVL, 
	 *      because we won't need to resolve for it. 
	 *    • Otherwise, if this variation has not yet been traversed by this session and must be targeted
	 *      for. Post qualification hooks. If client code disqualifies the variation, remove it from the AVL. 
	 *      (If client code also requested removal from targeting tracker (TT), remove this test from TT, if present.)
	 *    
	 * 3. Remaining variations are active and qualified and may have to be targeted. Each active test is one of:
	 *    • Already targeted in this session, if it's on the traversed variations list (TVL).
	 *      (Consequently, it's also in the TT).
	 *    • Pre-targeted, if it's in the TT, but not in TVL. Need to confirm targeting, see step 4 below.
	 *    • Not yet targeted, or free, if it's in neither in TVL nor in TT.  Must be targeted as part of this
	 *      state request.
     * 
     * 4. If we still have pre-targeted tests, confirm that their targeting is compatible with this
     *    session's current list of live experiences. In other words, confirm that the test cell given
     *    by the combination of the live and the pre-targeted experiences is resolvable in the current schema.
     *    (It may not be, e.g., when two tests used to be conjoint and the TT contains a variant
     *    experience for one, and the currently targeted list contains a variant experience for the other.
     *    If, in the current schema, they are no longer conjoint, this combination is no longer resolvable.)
     *    
     * 5. If pre-targeted experiences are not compatible with the live expriences, compute the maximal 
     *    compatible subset, i.e. one that of possible compatible subsets is the longest. Remove from the
     *    TT those experiences that did not make the compatible subset. These tests will be treated as 
     *    untargeted, i.e. as if they had never been targeted in the past.
     *    
     * 6. For each test that is on AVL but not in TVL: if test is targetable, post the targeting hooks
     *    and, if the hook returns an experience, add it to TT. If the test is not targetable, default
     *    to control.
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
				
		// 1. The already targeted live experiences -- those in the targeting tracker,
		// that are also in the live variation list. These experiences must be honored.
		LinkedHashSet<Experience> alreadyTargetedExperiences = new LinkedHashSet<Experience>();
		for (Experience exp: session.getTargetingStabile().getAllAsExperiences(schema)) {
			if (session.getTraversedVariations().contains(exp.getVariation())) {
				// in targeting tracker and live. 
				// User error if requested state is phantom in this experience.
				if (exp.isPhantom(state))  {
					throw ServerExceptionRemote$.MODULE$.apply(ServerError.STATE_PHANTOM_IN_EXPERIENCE, state.getName(), exp.toString());
				}
				alreadyTargetedExperiences.add(exp);
			}
		}
		// Map already targeted experiences to variations
		Set<Variation> alreadyTargetedVariations =
				alreadyTargetedExperiences.stream().map(exp -> exp.getVariation()).collect(Collectors.toSet());

		// 2. The active variation list (AVL): all instrumented variations, as given by State.getInstrumentedVariations()),
		// but excluding the offline variations and those we've already been disqualified for.
		ArrayList<Variation> activeVariationsList = new ArrayList<Variation>();
		for (Variation var: state.getInstrumentedVariations()) {
			if (var.isOn()) activeVariationsList.add(var);
		}

		// Remove from AVL those already disqualified, qualify those which haven't yet been
		// qualified by this session, and remove those disqualified.
		for (Iterator<Variation> iter = activeVariationsList.iterator(); iter.hasNext();) {
			VariationImpl test = (VariationImpl) iter.next();
			if (session.getDisqualifiedVariations().contains(test)) {
				iter.remove();
			}
			else if (!session.getTraversedVariations().contains(test) && !qualifyTest(test, session)) {
				iter.remove();
			}
		}
		
		// 3. At this point, AVL contains only online variations, instrumented on this state, and
		// qualified for by this session. Each variation on AVL is either accounted for by the already
		// targeted list, or otherwise falls into one of two categories: pre-targeted (if on the
		// targeting tracker) or free (otherwise).   ("Free" is not a good name because they may 
		// not actually be free due to concurrency.  They are not yet targeted.)
		LinkedHashSet<Experience> preTargetedExperiences = new LinkedHashSet<Experience>();
		LinkedHashSet<Variation> freeVariations = new LinkedHashSet<Variation>();

		for (Variation var: activeVariationsList) {
			if (!alreadyTargetedVariations.contains(var)) {
				
				// Not yet traversed by this session. Add to the pre-targeted experience list, if in TT. If not in TT,
				// it's a free test that will be targeted after the pre-targets.
				Experience exp = session.getTargetingStabile().getAsExperience(var.getName(), schema);
				
				if (exp == null) {
					freeVariations.add(var); 
				}
				else {
					// We have a pre-targeted experience for a test that we have just hit.
					// If this is an undefined state, we need to discard the pre-targeted experience
					// and treat this as free.
					if (!exp.isPhantom(state)) {
						preTargetedExperiences.add(exp);						
					}
					else {
						session.getTargetingStabile().remove(var.getName());
						freeVariations.add(var);
					}
				}
				
				session.coreSession().addTraversedVariation(var);
			}
		}
		//System.out.println("Already Targeted: " + CollectionsUtils.toString(alreadyTargetedExperiences, ", "));
		//System.out.println("Pre Targeted: " + CollectionsUtils.toString(preTargetedExperiences, ", "));
		//System.out.println("Free: " + CollectionsUtils.toString(freeVariations, ", "));
		
		// 4. If not empty, preTargeted list contains experiences we need to confirm are
		//    compatible with those already targeted, i.e. in TT and in TTL
		if (!preTargetedExperiences.isEmpty()) {
			
			// Resolvable?  If not, find max resolvable subset and discard the rest.
			Collection<Experience> minUnresolvableSubvector = minUnresolvableSubvector(alreadyTargetedExperiences, preTargetedExperiences);
			
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
		vector.addAll(alreadyTargetedExperiences);
		vector.addAll(preTargetedExperiences);
		
		// Target free tests.
		for (Variation ft: freeVariations) {

			if (isTargetable(ft, state, vector)) {
				// Target this test. First post targeting hooks.
				VariationTargetingLifecycleEventImpl event = new VariationTargetingLifecycleEventImpl(session, ft, state);
				VariationTargetingLifecycleEventPostResultImpl postResult = (VariationTargetingLifecycleEventPostResultImpl) schema.hooksService().post(event);
				Experience targetedExperience = postResult.getTargetedExperience();

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
		// For the purposes of state resolution, we need to drop the experiences which are not
		// instrumented on this state.
		Iterator<Experience> iter = vector.iterator();
		while (iter.hasNext()) {
			if (!state.isInstrumentedBy(iter.next().getVariation())) iter.remove();
		}
		//System.out.println("Vector: " + CollectionsUtils.toString(vector, ", "));
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
		VariationQualificationLifecycleEventPostResultImpl postResult = (VariationQualificationLifecycleEventPostResultImpl) schema.hooksService().post(event);

		if (!postResult.isQualified()) {
			session.addDisqualifiedTest(var);
			if (postResult.isRemoveFromTT()) session.getTargetingStabile().remove(var.getName());
		}				
		
		return postResult.isQualified();
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
	 *   • all coordinate experiences are pairwise independent, i.e. that there are no such
	 *     pair e1,e2 that <code>e1.getVariation().equals(e2.getVariation())</code>
	 *   • each experience's variation is instrumented on the given state
	 * 
	 * 0. Verify that the coordinates vector has at least 1 experience. 
	 * 1. Resort the coordinate vector in ordinal order. 
	 * 2. As we go, remove experiences that are either control in their variation, 
	 *    or their variation is not instrumented on the given state.
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

		// Build a list of input coordinates, resorted in ordinal order and with
		// all control and uninstrumented experiences removed.
		ArrayList<Experience> sortedCoord = new ArrayList<Experience>(coord.size());
			
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
							throw new ServerExceptionInternal("Phantom experience [" + e + "] in coordinate vector");

						found = true;

						if (!e.isControl()) { 
							sortedCoord.add(e);
							// Continue down the vector, to ensure that there's no other experience for this test.
						}
					}
				}
			}
		}
		
		boolean resolvable = false;
		Optional<StateVariant> resolvedVariantOpt = Optional.empty();
		
		if (sortedCoord.size() == 0) {
			// Trivial resolution.
			resolvable = true;
		}
		else {
			Variation highOrderVar = sortedCoord.get(sortedCoord.size() - 1).getVariation();
			Optional<Variation.OnState> vosOpt = highOrderVar.getOnState(state);
			if (vosOpt.isPresent()) {
				VariationOnStateImpl vos = (VariationOnStateImpl) vosOpt.get();
				resolvedVariantOpt = vos.getVariant(new HashSet<Experience>(sortedCoord));
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

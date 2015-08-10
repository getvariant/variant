package com.variant.core.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

import com.variant.core.Variant;
import com.variant.core.VariantInternalException;
import com.variant.core.VariantSession;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.Test.OnView;
import com.variant.core.schema.View;
import com.variant.core.schema.impl.TestImpl;
import com.variant.core.schema.impl.TestOnViewImpl;
import com.variant.core.schema.impl.ViewImpl;
import com.variant.core.session.TargetingPersister;
import com.variant.core.session.VariantSessionImpl;

/**
 * Entry point into the runtime.
 * 
 * @author Igor.
 *
 */
public class VariantRuntime {

	/**
	 * Static singleton.
	 * Need package visibility for test facade.
	 */
	private VariantRuntime() {}
	
	
	/**
	 * Target this session for all active tests.
	 * 
	 * 1. Find all the tests instrumented on this view - the view's test list,
	 *    i.e. the list of tests that must be targeted before we can resolve the view.
	 * 2. Some of these tests may be already targeted via the experience persistence mechanism.
	 *    Determine this already targeted subset.
     * 3. If such a subset exists, confirm that we are still able to resolve this test cell. 
     *    The reason we may not is, e.g., if two tests used to be covariant and the experience
     *    persister reports two already targeted variant experiences, but in a recent config 
     *    change these two tests are no longer covariant and hence we don't know how to resolve
     *    this test cell.
     * 4. If we're still able to resolve the pre-targeted vector, continue with the rest of the tests 
     *    on the view's list in the order they were defined, and target each test via the regular 
     *    targeting mechanism.
     * 5. If we're unable to resolve the pre-targeted vector, compute the minimal unresolvable subset
     *    and remove those experiences from the experience persistence.
     * 6. Given the new pre-targeted experience set, target the rest of the tests instrumented on
     *    this view via each test's regular targeting mechanism, in ordinal order.  Do not target
     *    tests that are OFF — default them to control experiences.
     * 7. Resolve the path. For OFF tests, substitute non-control experiences with control ones.
	 * 
	 * @param config
	 * @param viewPath
	 */
	public static VariantViewRequestImpl targetSessionForView(VariantSession ssn, View view) {

		Schema schema = Variant.getSchema();
		TargetingPersister tp = ssn.getTargetingPersister();
		
		// It is illegal to call this with a view that is not in schema, e.g. before runtime.
		View schemaView = schema.getView(view.getName());
		if (System.identityHashCode(schemaView) != System.identityHashCode(view)) 
			throw new VariantInternalException("View [" + view.getName() + "] is not in schema");

		// Pre-targeted experiences from the targeting persister.
		Collection<Experience> alreadyTargetedExperiences = tp.getAll();
		
		if (!alreadyTargetedExperiences.isEmpty()) {
			
			// Resolvable?  If not, find smallest subset that will make it resolvable.
			Collection<Experience> minUnresolvableSubvector = 
					minUnresolvableSubvector(alreadyTargetedExperiences);
			
			if (!minUnresolvableSubvector.isEmpty()) {

				for (Experience e: minUnresolvableSubvector) tp.remove(e.getTest());

				Variant.getLogger().info(
						"Targeting persistor not resolvable for session [" + ssn.getId() + "]. " +
						"Removed experiences [" + StringUtils.join(minUnresolvableSubvector.toArray()) + "].");
			}
		
		}

		// Targeting persister now has all currently targeted experiences.
		for (Experience e: tp.getAll()) {
			if (e.getTest().isOn()) {
				if (Variant.getLogger().isTraceEnabled()) {
					Variant.getLogger().trace(
							"Session [" + ssn.getId() + "] recognized persisted experience [" + e +"]");
				}									
			}
			else {
				if (Variant.getLogger().isTraceEnabled()) {
					Variant.getLogger().trace(
							"Session [" + ssn.getId() + "] recognized persisted experience [" + e +"]" +
							" but substituted control experience [" + e.getTest().getControlExperience() + "]" +
							" because test is OFF");
				}													
			}
		}
		// Tests that are instrumented on this page need to be targeted, unless already targeted.
		for (Test test: view.getInstrumentedTests()) {
			
			if (view.isInstrumentedBy(test) && tp.get(test) == null) {
				
				if (!test.isOn()) {
					Experience e = test.getControlExperience();
					tp.add(e, System.currentTimeMillis());
					if (Variant.getLogger().isTraceEnabled()) {
						Variant.getLogger().trace(
								"Session [" + ssn.getId() + "] temporarily targeted for OFF test [" + 
								test.getName() +"] with control experience [" + e.getName() + "]");
					}					
				}
				else if (isTargetable(test, tp.getAll())) {
					Experience e = ((TestImpl) test).target(ssn);
					tp.add(e, System.currentTimeMillis());
					if (Variant.getLogger().isTraceEnabled()) {
						Variant.getLogger().trace(
								"Session [" + ssn.getId() + "] targeted for test [" + 
								test.getName() +"] with experience [" + e.getName() + "]");
					}

				}
				else {
					Experience e = test.getControlExperience();
					tp.add(e, System.currentTimeMillis());
					if (Variant.getLogger().isTraceEnabled()) {
						Variant.getLogger().trace(
								"Session [" + ssn.getId() + "] targeted for test [" + 
								test.getName() +"] with control experience [" + e.getName() + "]");
					}
				}
			}
		}
		
		VariantViewRequestImpl result = new VariantViewRequestImpl((VariantSessionImpl)ssn, (ViewImpl) view);
		
		// TP may still contain non-control experiences for OFF tests, if they were in the persister at the top
		// of this method and did not get reduced out due to non-resolvability.  Before resolving the view path,
		// we substitute them with control.
		ArrayList<Experience> vector = new ArrayList<Experience>();
		for (Experience e: tp.getAll()) {
			vector.add(e.getTest().isOn() ? e : e.getTest().getControlExperience());
		}
		
		String resolvedPath = resolveViewPath(view, vector);
		
		if (Variant.getLogger().isTraceEnabled()) {
			Variant.getLogger().trace(
					"Session [" + ssn.getId() + "] resolved view [" + 
					view.getName() +"] as [" + resolvedPath + "] for experience vector [" +
					StringUtils.join(tp.getAll().toArray(), ",") + "]");
		}   

		result.setResolvedPath(resolvedPath);

		return result;
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
	static Collection<Experience> minUnresolvableSubvector(Collection<Experience> vector) {
		
		Collection<Experience> result = new ArrayList<Experience>();
			
		// 1. Build a set of all instrumented views.
		HashSet<View> instrumentedViews = new HashSet<View>();
		for (Experience e: vector) {
			for (OnView tov: e.getTest().getOnViews()) {
				if (!tov.getView().isInvariantIn(e.getTest())) {
					instrumentedViews.add(tov.getView());
				}
			}
		}
			
		// 2,3. Try to resolve them all.
		ArrayList<View> unresolvedViews = new ArrayList<View>();
		for (View view: instrumentedViews) {
			if (resolveViewPath(view, vector) == null) {
				unresolvedViews.add(view);
			}
		}
				
		// 4.
		if (unresolvedViews.isEmpty()) return result;
		
		// 5. Build the set of tests instrumented on any of the unresolved views.
		HashSet<Test> testsInstumentedOnUnresolvedViews = new HashSet<Test>();  
		for (View uv: unresolvedViews) {
			for (Test t: uv.getInstrumentedTests()) {
				if (!uv.isInvariantIn(t)) testsInstumentedOnUnresolvedViews.add(t);
			}
		}
		
		// 6. 
		int shortestLength = vector.size();
		Experience shortestExperience = null;
		Collection<Experience> shortestSubvector = null;
		
		for (Experience inputExperience: vector) {
		
			if (!testsInstumentedOnUnresolvedViews.contains(inputExperience.getTest())) continue;
			
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
	static boolean isTargetable(Test test, Collection<Experience> alreadyTargetedExperiences) {
		
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
	 * @param view
	 * @param vector
	 * @return path
	 */
	static String resolveViewPath(View view, Collection<Experience> vector) {
		
		if (vector.size() == 0) 
			throw new VariantInternalException("No experiences in vector");
		
		ArrayList<Experience> sortedList = new ArrayList<Experience>(vector.size());
		for (Test t: Variant.getSchema().getTests()) {
			boolean found = false;
			for (Experience e: vector) {
				if (e.getTest().equals(t)) {
					if (found) {
						throw new VariantInternalException("Duplicate test [" + t + "] in input");
					}
					else {
						found = true;
						if (!e.isControl() && view.isInstrumentedBy(e.getTest()) && !view.isInvariantIn(e.getTest())) 
							sortedList.add(e);
					}
				}
			}
		}

		// All experiences were control or uninstumented?
		if (sortedList.size() == 0) return view.getPath();
		
		Test highOrderTest = sortedList.get(sortedList.size() - 1).getTest();
		TestOnViewImpl tov = (TestOnViewImpl) highOrderTest.getOnView(view);
		
		if (tov == null) return null;
		
		Test.OnView.Variant variant = tov.variantSpace().get(sortedList);
		return variant == null ? null : variant.getPath();
	}

	/**
	 * A list of tests co-varaint with a given test.  This is different from <code>Test.getCovariantTests()</code>
	 * because each test returned by this method is either mentioned in the given test's covariantTestRefs
	 * (and hence will also be returned by <code>Test.getCovariantTests()</code>), or mentions this test in its 
	 * covarainttestRefs. In other words, relationship of covariance is commutative: if A is covariant with B, 
	 * then B is also covariant with A, and this method computes and returns closure over this relationship.
     *
	 * *** DOESN'T LOOK LIKE THIS IS NEEDED. Remove TestImpl.get/putRuntimeAttribute() as well ***
	@SuppressWarnings("unchecked")
	public static List<TestImpl> getCovariantTests(Test test) {

		Schema schema = Variant.getSchema();
				
		// This is called on each view hit, so we cache it inside the test object.
		Object cachedResult = ((TestImpl)test).getRuntimeAttribute("fullCovarList");
		if (cachedResult == null) { 
			List<Test> newResult = new ArrayList<Test>();
			newResult.addAll(test.getCovariantTests());
			// 2. Any other test whose covariance list contains this test.
			for (Test other: schema.getTests()) {
				if (other.equals(test)) continue;
				for (Test otherCovar: other.getCovariantTests()) {
					if (otherCovar.equals(test)) {
						newResult.add(other);
						break;
					}
				}
			}
			((TestImpl)test).putRuntimeAttribute("fullCovarList", newResult);
			cachedResult = newResult;
		}
		
		return (List<TestImpl>) Collections.unmodifiableList((List<TestImpl>)cachedResult);
	}
	*/
	
}

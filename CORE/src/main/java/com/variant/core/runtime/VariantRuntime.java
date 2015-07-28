package com.variant.core.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.variant.core.Variant;
import com.variant.core.VariantInternalException;
import com.variant.core.VariantSession;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.View;
import com.variant.core.util.VariantCollectionsUtils;

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
	 * 2. Some of these tests may be already targeted via the experience stability mechanism.
	 *    Determine this already targeted subset.
     * 3. If such a subset exists, confirm that we are still able to resolve this test cell. 
     *    The reason we may not is, e.g., if two tests used to be covariant and the experience
     *    persister reports two already targeted variant experiences, but in a recent config 
     *    change these two tests are no longer covariant and hence we don't know how to resolve
     *    this test cell.
     * 4. If we're still able to resolve the pre-targeted cell, continue with the rest of the tests 
     *    on the view's list in the order they were defined, and target each test via the regular 
     *    targeting mechanism.
     * 5. If we're unable to resolve the pre-targeted cell, remove all its experience from the
     *    tergeting persister, i.e. make it equivalent to there being no pre-targed tests at all
     *    and target all tests, in the order they were defined, regularly.
	 * 
	 * @param config
	 * @param viewPath
	 */
	public static void targetSession(VariantSession ssn, View view) {

		Schema schema = Variant.getSchema();

		// It is illegal to call this with a view that is not in schema, e.g. before runtime.
		View schemaView = schema.getView(view.getName());
		if (System.identityHashCode(schemaView) != System.identityHashCode(view)) 
			throw new VariantInternalException("View [" + view.getName() + "] is not in schema");

		// Tests that are instrumented on this page.
		List<Test> testList = view.getInstrumentedTests();
		
		// Of those, experiences that are already targeted.
		List<Experience> alreadyTargetedTestList = new ArrayList<Experience>(testList.size());

		for (Test test: testList) {
			Experience e = ssn.getTargetingPersister().read(test);
			if (e != null) alreadyTargetedTestList.add(e);
		}

		// 
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

	/**
	 * Find the maximal resolvable subvector.
	 * A given vector is resolvable is we are able to resolve it for every view where it is relevant.
	 * A max resolvable subvector is one that is as close as possible to the original vector and is resolvable,
	 * while the original vector is not.
	 * 1. Sort vector in ordinal order.
	 * 2. 
	 * 
	 * 
	 * TODO: GETME.
	 * 
	 * @param coordinates
	 * @return
	 */
	private static Collection<Experience> maxResolvableSubvector(Collection<Experience> vector) {
		return null;
	}
	
	/**
	 * Is a set of tests resolvable in the current schema?
	 * In other words, does current schema has a variant for every experience permutation on every view
	 * where the constituent tests are instrumented?
	 * 
	 * 1. Find the test Tk with the highest ordinal number from all in the input set. 
	 * 2. The given set {T} is resolvable if {T}\Tk is a subset of Tk's covariance set. 
	 *  
	 * @param test set of tests.  We require set to guarantee no duplicates.
	 * @return
	 */
	static boolean isResolvable(Set<Test> tests) {
		
		Schema schema = Variant.getSchema();
		
		ArrayList<Test> sortedTests = new ArrayList<Test>(tests.size());
		Test rightMostTest = null;
		for (Test t: schema.getTests()) {
			if (tests.contains(t)) {
				rightMostTest = t;
			}
		}
		
		sortedTests.remove(rightMostTest);
		
		return VariantCollectionsUtils.contains(rightMostTest.getCovariantTests(), sortedTests);
		
	}
	

}

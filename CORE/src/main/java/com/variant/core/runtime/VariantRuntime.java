package com.variant.core.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.View;
import com.variant.core.schema.impl.TestImpl;

/**
 * Entry point into the runtime.
 * 
 * @author Igor.
 *
 */
public class VariantRuntime {

	/**
	 * static singleton.
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

		// It is illegal to call this with a view that is not in schema, i.e. before runtime.
		View schemaView = schema.getView(view.getName());
		if (System.identityHashCode(schemaView) != System.identityHashCode(view)) 
			throw new IllegalArgumentException("View [" + view.getName() + "] is not in schema");

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
	 */
	@SuppressWarnings("unchecked")
	public static List<TestImpl> getCovariantTests(Test test) {

		Schema schema = Variant.getSchema();
		
		// It is illegal to call this with a test that is not in schema, i.e. before runtime.
		Test schemaTest = schema.getTest(test.getName());
		if (System.identityHashCode(schemaTest) != System.identityHashCode(test)) 
			throw new IllegalArgumentException("Test [" + test.getName() + "] is not in schema");
		
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

	/**
	 * 
	 * @param view
	 * @param coordinates
	 * @return
	 */
	private static String resolvePath(View view, Collection<Test.Experience> coordinates) {
		return null;
	}
}

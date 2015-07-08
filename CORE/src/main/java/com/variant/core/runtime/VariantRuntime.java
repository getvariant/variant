package com.variant.core.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.variant.core.VariantSession;
import com.variant.core.schema.Test;
import com.variant.core.schema.View;
import com.variant.core.schema.Test.Experience;

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
	 * 
	 * @param view
	 * @param coordinates
	 * @return
	 */
	private static String resolvePath(View view, Collection<Test.Experience> coordinates) {
		return null;
	}
}

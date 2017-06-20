package com.variant.server.test.hooks;

import java.util.List;
import java.util.Random;

import com.typesafe.config.Config;
import com.variant.core.UserHook;
import com.variant.core.schema.Hook;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.server.api.ServerException;
import com.variant.server.api.Session;
import com.variant.server.api.hook.PostResultFactory;
import com.variant.server.api.hook.TestTargetingLifecycleEvent;
import com.variant.server.api.hook.TestTargetingLifecycleEventPostResult;

/**
 * Target according to weights passed in the init property weights.
 */
public class TestTargetingHook implements UserHook<TestTargetingLifecycleEvent> {

	public static String ATTR_KEY = TestTargetingHook.class.getName();

	private static Random rand = new Random(System.currentTimeMillis());

	private Double[] weights;
	private String experienceToReturn;
	
	@Override
	public void init(Config config, Hook hook) {

		if (config.hasPath("init.weights")) {
			List<Double> weightsConfig = config.getDoubleList("init.weights");
			weights = weightsConfig.toArray(new Double[weightsConfig.size()]);
		}
		else if (config.hasPath("init.experience")) {
			experienceToReturn = config.getString("init.experience");
		}
		else {
			throw new RuntimeException(String.format("Either 'weights' or 'experience' must be provided"));
		}
	}

	@Override
    public Class<TestTargetingLifecycleEvent> getLifecycleEventClass() {
		return TestTargetingLifecycleEvent.class;
    }
   
	@Override
	public PostResult post(TestTargetingLifecycleEvent event) {

		Session ssn = event.getStateRequest().getSession();
		TestTargetingLifecycleEventPostResult result = PostResultFactory.mkPostResult(event);
		
		if (experienceToReturn != null) {
			String[] tokens = experienceToReturn.split("\\.");
			Experience exp = event.getStateRequest().getSession().getSchema().getTest(tokens[0]).getExperience(tokens[1]);			
			result.setTargetedExperience(exp);
			return result;
		} 
		
		Test test = event.getTest();
		State state = event.getState();
		
		String curVal = ssn.getAttribute(ATTR_KEY);
		if (curVal == null) curVal = ""; else curVal += " ";
		ssn.setAttribute(ATTR_KEY,  curVal + test.getName());
		
		double weightSum = 0;
		if (weights.length != test.getExperiences().size()) 
			throw new RuntimeException(
					String.format("Number of weights passed in init (%s) does not equal the number of test experiences (%s)", 
							weights.length, test.getExperiences().size()));
					
		for (int i = 0; i < weights.length; i++) {
			Experience e = test.getExperiences().get(i);
			if (!e.isDefinedOn(state)) continue;
			weightSum += weights[i];
		}

		if (weightSum == 0) {
			throw new ServerException.Internal(
					String.format("No defined states in test [%s] on state [%s]", test.getName(), state.getName()));
		}
		
		double randVal = rand.nextDouble() * weightSum;
		weightSum = 0;
		//Experience lastExperience = null;
		for (int i = 0; i < weights.length; i++) {
			Experience e = test.getExperiences().get(i);
			if (!e.isDefinedOn(state)) continue;
			//lastExperience = e;
			weightSum += weights[i];
			if (randVal < weightSum) {
				result.setTargetedExperience(e);
				return result;
			}
		}
		// Should never happen.
		throw new RuntimeException(String.format("Should never happen: %s %s", randVal, weightSum));
	}
}

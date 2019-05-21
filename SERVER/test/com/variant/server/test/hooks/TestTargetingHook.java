package com.variant.server.test.hooks;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.typesafe.config.Config;
import com.variant.core.schema.State;
import com.variant.core.schema.Variation;
import com.variant.core.schema.Variation.Experience;
import com.variant.server.api.Session;
import com.variant.server.api.lifecycle.LifecycleEvent;
import com.variant.server.api.lifecycle.LifecycleHook;
import com.variant.server.api.lifecycle.VariationTargetingLifecycleEvent;
import com.variant.server.boot.ServerExceptionInternal;

/**
 * Test targeting hook.  Used by the targeting test.
 * if weights is given as array if doubles, they are interpreted as random weights.
 * otherwise, if experience is given, attempt to find that experience.
 * 
 */
public class TestTargetingHook implements LifecycleHook<VariationTargetingLifecycleEvent> {

	public static String ATTR_KEY = TestTargetingHook.class.getName();

	private static Random rand = new Random(System.currentTimeMillis());

	private Double[] weights;
	private String experienceToReturn;
	
	/**
	 * Non nullary constructor
	 * @param config
	 */
	public TestTargetingHook(Config config) {

		if (config.hasPath("weights")) {
			List<Double> weightsConfig = config.getDoubleList("weights");
			weights = weightsConfig.toArray(new Double[weightsConfig.size()]);
		}
		else if (config.hasPath("experience")) {
			experienceToReturn = config.getString("experience");
		}
		else {
			throw new RuntimeException(String.format("Either 'weights' or 'experience' must be provided"));
		}
	}

	@Override
    public Class<VariationTargetingLifecycleEvent> getLifecycleEventClass() {
		return VariationTargetingLifecycleEvent.class;
    }
   
	@Override
	public Optional<LifecycleEvent.PostResult> post(VariationTargetingLifecycleEvent event) {

		Session ssn = event.getSession();
		VariationTargetingLifecycleEvent.PostResult result = event.mkPostResult();
		
		if (experienceToReturn != null) {
			String[] tokens = experienceToReturn.split("\\.");
			Experience exp = event.getSession().getSchema().getVariation(tokens[0]).get().getExperience(tokens[1]).get();			
			result.setTargetedExperience(exp);
			return Optional.of(result);
		} 
		
		Variation test = event.getVariation();
		State state = event.getState();
		
		String curVal = ssn.getAttributes().get(ATTR_KEY);
		if (curVal == null) curVal = ""; else curVal += " ";
		ssn.getAttributes().put(ATTR_KEY,  curVal + test.getName());
		
		double weightSum = 0;
		if (weights.length != test.getExperiences().size()) 
			throw new RuntimeException(
					String.format("Number of weights passed in init (%s) does not equal the number of test experiences (%s)", 
							weights.length, test.getExperiences().size()));
					
		for (int i = 0; i < weights.length; i++) {
			Experience e = test.getExperiences().get(i);
			if (e.isPhantom(state)) continue;
			weightSum += weights[i];
		}

		if (weightSum == 0) {
			throw new ServerExceptionInternal(
					String.format("No defined states in test [%s] on state [%s]", test.getName(), state.getName()));
		}
		
		double randVal = rand.nextDouble() * weightSum;
		weightSum = 0;
		//Experience lastExperience = null;
		for (int i = 0; i < weights.length; i++) {
			Experience e = test.getExperiences().get(i);
			if (e.isPhantom(state)) continue;
			//lastExperience = e;
			weightSum += weights[i];
			if (randVal < weightSum) {
				result.setTargetedExperience(e);
				return Optional.of(result);
			}
		}
		// Should never happen.
		throw new RuntimeException(String.format("Should never happen: %s %s", randVal, weightSum));
	}
}

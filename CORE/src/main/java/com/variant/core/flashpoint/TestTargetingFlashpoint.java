package com.variant.core.flashpoint;

import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

public interface TestTargetingFlashpoint extends RuntimeFlashpoint {

	/**
	 * What test is being targeted.  This is how Variant will
	 * know when to call the <code>isQualified()</code> method.
	 * @return
	 */
	public Test getTest();
	
	/**
	 * The current value, most recently set by <code>setTargetedExperience()</code>.
	 * The seed value is null.
	 * @return
	 */
	public Experience getTargetedExperience();
	
	/**
	 * Listener calls this to pass the result back to the container.
	 * If no suitable listener was found or the listener didn't call
	 * this method, the container will run the default random targeter
	 * based on the weights
	 * .
	 * @param experience
	 */
	public void setTargetedExperience(Experience experience);

}

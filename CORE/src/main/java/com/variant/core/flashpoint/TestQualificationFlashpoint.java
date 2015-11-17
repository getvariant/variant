package com.variant.core.flashpoint;

import com.variant.core.schema.Test;

public interface TestQualificationFlashpoint extends RuntimeFlashpoint {

	/**
	 * What test this qualifier is for.  This is how Variant will
	 * know when to call the <code>isQualified()</code> method.
	 * @return
	 */
	public Test getTest();
	
	/**
	 * The current value, most recently set by <code>setQualified()</code>.
	 * The seed value is true;
	 * @return
	 */
	public boolean isQualified();
	
	/**
	 * The current value, most recently set by <code>setREmoveFromTargetingTracker()</code>.
	 * The seed value is true;
	 */
	public boolean isRemoveFromTargetingTracker();
	
	/**
	 * Listener calls this to pass the result back to the engine.
	 * If no suitable listener was found or the listener didn't call
	 * this method, the engine will assume true.
	 * @param qualified
	 */
	public void setQualified(boolean qualified);

	/**
	 * Listener calls this to tell the engine if the entry from this test should
	 * be removed from the targeting persister iff it is disqualified. The engine
	 * will not care what value was passed via this call if the <code>setQualifed</code>
	 * passed true;
	 * 
	 * @param remove
	 */
	public void setRemoveFromTargetingTracker(boolean remove);

}

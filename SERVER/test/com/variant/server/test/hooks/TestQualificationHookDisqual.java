package com.variant.server.test.hooks;

import com.typesafe.config.ConfigValue;
import com.variant.core.schema.Hook;
import com.variant.server.api.TestQualificationLifecycleEvent;
import com.variant.server.api.UserHook;

public class TestQualificationHookDisqual implements UserHook<TestQualificationLifecycleEvent>{

	public static String ATTR_KEY = "current-list";
	
	@Override
	public void init(ConfigValue init) {}

	@Override
    public Class<TestQualificationLifecycleEvent> getLifecycleEventClass() {
		return TestQualificationLifecycleEvent.class;
    }
   
	@Override
	public void post(TestQualificationLifecycleEvent event, Hook hook) {
		String curVal = event.getSession().getAttribute(ATTR_KEY);
		if (curVal == null) curVal = ""; else curVal += " ";
		event.getSession().setAttribute(ATTR_KEY,  curVal + event.getTest().getName());
	}
	
}
/**
 * Disqualify passed tests and optionally remove their entries from targeting stabile
 *
class TestQualificationHookDisqual(removeFromStabile: Boolean, testsToDisqualify:Test*) 
extends UserHook[TestQualificationLifecycleEvent] {

	val testList = ListBuffer[Test]()

	override def getLifecycleEventClass() = classOf[TestQualificationLifecycleEvent]
	
	override def post(event: TestQualificationLifecycleEvent) {
		assert(event.getSession() != null, "No session passed")
		assert(event.getTest() != null, "No test passed")
		val test = testsToDisqualify.find { t => t.equals(event.getTest()) }			
		if (test.isDefined) {
			testList.add(event.getTest());
			event.setQualified(false);
			event.setRemoveFromTargetingTracker(removeFromStabile);
		}
	}		
}
*/
package com.variant.server.test;

import scala.util.Random
import com.variant.core.hook.HookListener
import com.variant.core.schema.StateParsedHook
import com.variant.core.schema.State
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.variant.core.exception.Error.Severity
import com.variant.server.schema.SchemaDeployer
import com.variant.core.schema.TestParsedHook
import com.variant.core.schema.Test
import com.variant.core.hook.TestQualificationHook
import com.variant.server.session.ServerSession

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class UserHookTest extends ServerBaseSpec {

   val MESSAGE_TEXT_STATE = "Info-Message-State"
	val MESSAGE_TEXT_TEST = "Info-Message-Test"
	
	val rand = new Random()
	var schemaId = None
   
	"StateParsedHook listener" should {
	   "be posted when state is parsed" in {
	      
   		val listener = new StateParsedHookListener
   		server.hooker.addListener(listener)
   		val response = server.installSchemaDeployer(SchemaDeployer.fromClasspath("/ParserCovariantOkayBigTest.json"))
   		response.getMessages(Severity.ERROR) mustBe empty
   		server.schema.isDefined mustBe true
   		val schema = server.schema.get
   		listener.stateList.toSeq mustEqual schema.getStates.toSeq
   		schema.getStates().size() mustEqual response.getMessages().size()
   		for (msg <- response.getMessages) {
   			msg.getSeverity mustBe Severity.INFO
   			msg.getText mustBe MESSAGE_TEXT_STATE
   			
   		}  
	   }
   }
	   
	"TestParsedHook listener" should {
	   "be posted when test is parsed" in {
	      
   	   server.hooker.clear()
   	   val listener = new TestParsedHookListener
   	   server.hooker.addListener(listener)
         val response = server.installSchemaDeployer(SchemaDeployer.fromClasspath("/ParserCovariantOkayBigTest.json"))
      	response.getMessages(Severity.ERROR) mustBe empty
         server.schema.isDefined mustBe true
      	val schema = server.schema.get
      	listener.testList.toSeq mustEqual schema.getTests.toSeq
      	schema.getTests().size() mustEqual response.getMessages().size()
   		for (msg <- response.getMessages) {
   			msg.getSeverity mustBe Severity.INFO
      		msg.getText mustBe MESSAGE_TEXT_TEST		
   		}
	   }
	}

	"StateParsedHook and TestParsedHook listenes" should {
	   "both be posted, states first, then tests" in {
	      
   	   server.hooker.clear()
   	   val stateListener = new StateParsedHookListener
   	   val testListener = new TestParsedHookListener
   	   server.hooker.addListener(testListener, stateListener)
         val response = server.installSchemaDeployer(SchemaDeployer.fromClasspath("/ParserCovariantOkayBigTest.json"))
      	response.getMessages(Severity.ERROR) mustBe empty
         server.schema.isDefined mustBe true
      	val schema = server.schema.get
      	testListener.testList.toSeq mustEqual schema.getTests.toSeq
      	stateListener.stateList.toSeq mustEqual schema.getStates.toSeq
      	response.getMessages.size mustEqual schema.getTests.size + schema.getStates.size 
   		for (i <- 0 until response.getMessages.size) {
   		   val msg = response.getMessages.get(i)
   			msg.getSeverity mustBe Severity.INFO
      		msg.getText mustBe (if (i < schema.getStates.size) MESSAGE_TEXT_STATE else MESSAGE_TEXT_TEST)
   		}
	   }
	}

	"TestQualificationHook" should {
	   
   	val ssn = new ServerSession(rand.nextString(5))
   	val nullListener = new TestQualificationHookListenerNil
	   
      "be posted for state1" in {

         server.hooker.clear()
         
    		server.hooker.addListener(nullListener);
         val response = server.installSchemaDeployer(SchemaDeployer.fromClasspath("/ParserCovariantOkayBigTest.json"))
   	   response.hasMessages() mustBe false		
   		nullListener.testList mustBe empty
   		server.schema.isDefined mustBe true
   	   val schema = server.schema.get
   		val state1 = schema.getState("state1")
   	   val test1 = schema.getTest("test1")
   	   val test2 = schema.getTest("test2")
   	   val test3 = schema.getTest("test3")
   	   val test4 = schema.getTest("test4")
   	   val test5 = schema.getTest("test5")
   	   val test6 = schema.getTest("test6")
   	   
   		val req = ssn.targetForState(state1);
		   ssn.getTraversedStates().size() mustBe 1
		   ssn.getTraversedStates().get(state1) mustEqual 1
		   ssn.getTraversedTests().toSet mustEqual Set(test2, test3, test4, test5, test6)
		   ssn.getDisqualifiedTests().size() mustEqual 0
		   val stabile = ssn.targetingStabile
		   stabile.getAll().size() mustEqual 5
		   stabile.get("test1") mustBe (null)
		   stabile.get("test2") mustNot be (null)
		   stabile.get("test3") mustNot be (null)
		   stabile.get("test4") mustNot be (null)
		   stabile.get("test5") mustNot be (null)
		   stabile.get("test6") mustNot be (null)
		   nullListener.testList.toSeq mustEqual Seq(test2, test3, test4, test5, test6)
	   }
	   
	   "be posted for state2" in {

   	   val schema = server.schema.get
   		val state1 = schema.getState("state1")
   		val state2 = schema.getState("state2")
   	   val test1 = schema.getTest("test1")
   	   val test2 = schema.getTest("test2")
   	   val test3 = schema.getTest("test3")
   	   val test4 = schema.getTest("test4")
   	   val test5 = schema.getTest("test5")
   	   val test6 = schema.getTest("test6")
   	   nullListener.testList.clear()
   	   
	      val req = ssn.targetForState(state2);
		   ssn.getTraversedStates().size() mustEqual 2
		   ssn.getTraversedStates().get(state1) mustEqual 1
		   ssn.getTraversedStates().get(state2) mustEqual 1
		   ssn.getTraversedTests().toSet mustEqual Set(test1, test2, test3, test4, test5, test6)
		   ssn.getDisqualifiedTests().size() mustEqual 0
		   val stabile = ssn.targetingStabile
		   stabile.getAll().size() mustEqual 6
		   stabile.get("test1") mustNot be (null)
		   stabile.get("test2") mustNot be (null)
		   stabile.get("test3") mustNot be (null)
		   stabile.get("test4") mustNot be (null)
		   stabile.get("test5") mustNot be (null)
		   stabile.get("test6") mustNot be (null)
		   nullListener.testList.toSeq mustEqual Seq(test1)

	   }
	}

	/**
	 * 
	 */
	class StateParsedHookListener extends HookListener[StateParsedHook] {
	   val stateList = ListBuffer[State]()
		override def getHookClass() = classOf[StateParsedHook]
      override def post(hook: StateParsedHook) {
	      stateList += hook.getState()
			hook.addMessage(Severity.INFO, MESSAGE_TEXT_STATE)
      }
   }

	/**
	 * 
	 */
	class TestParsedHookListener extends HookListener[TestParsedHook] {
	   val testList = ListBuffer[Test]()
		override def getHookClass() = classOf[TestParsedHook]
      override def post(hook: TestParsedHook) {
	      testList += hook.getTest()
			hook.addMessage(Severity.INFO, MESSAGE_TEXT_TEST)
      }
   }

	/**
	 * Do nothing. Tests should be qualified by default.
	 */
	class TestQualificationHookListenerNil extends HookListener[TestQualificationHook] {
		val testList = ListBuffer[Test]()
		override def getHookClass() = classOf[TestQualificationHook]
		override def post(hook: TestQualificationHook) {
			testList += hook.getTest()
		}		
	}

}


/*

	/**
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void testQualificationTest() throws Exception {
		
		
		
		nullListener.testList.clear();
		
		// Repeat the same thing.  Test should have been put on the qualified list for the session
		// so the hooks won't be posted.
		assertTrue(nullListener.testList.isEmpty());
		request = ssn.targetForState(state1);
		assertEquals(1, ssn.getTraversedStates().size());
		assertEquals(state1, ssn.getTraversedStates().iterator().next().arg1());
		assertEquals(2, ssn.getTraversedStates().iterator().next().arg2().intValue());
		assertEqualAsSets(ssn.getTraversedTests(), schema.getTest("test1"), schema.getTest("Test1"));

		assertEquals(2, stabile.getAll().size());
		assertNotNull(stabile.get("test1"));
		assertNotNull(stabile.get("Test1"));
		assertEquals(0, nullListener.testList.size());
		request.commit();

		// New session. Disqualify, but keep in TT.
		TestQualificationHookListenerDisqualifyImpl disqualListener = new TestQualificationHookListenerDisqualifyImpl(false, schema.getTest("test1"));
		core.addHookListener(disqualListener);
		response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertTrue(nullListener.testList.isEmpty());
		assertTrue(disqualListener.testList.isEmpty());
		schema = core.getSchema();
		state1 = schema.getState("state1");
		sessionId = VariantStringUtils.random64BitString(rand);
		VariantCoreSession ssn2 = core.getSession(sessionId, true).getBody();
		setTargetingStabile(ssn2, "test2.D", "Test1.A");
		request = ssn2.targetForState(state1);
		assertEquals(1, ssn2.getTraversedStates().size());
		assertEquals(state1, ssn2.getTraversedStates().iterator().next().arg1());
		assertEquals(1, ssn2.getTraversedStates().iterator().next().arg2().intValue());
		assertEqualAsSets(ssn2.getTraversedTests(), schema.getTest("Test1"));
		assertEqualAsSets(ssn2.getDisqualifiedTests(), schema.getTest("test1"));

		assertEquals(2, stabile.getAll().size());
		assertNotNull(stabile.get("test1"));
		assertNotNull(stabile.get("Test1"));
		assertEquals(VariantCollectionsUtils.list(schema.getTest("test1")), disqualListener.testList);
		assertEquals("/path/to/state1/Test1.A", request.getResolvedParameter("path"));
		request.commit();

		// New session. Disqualify and drop from TT
		core.clearHookListeners();
		disqualListener = new TestQualificationHookListenerDisqualifyImpl(true, schema.getTest("Test1"));
		core.addHookListener(disqualListener);
		
		response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertTrue(disqualListener.testList.isEmpty());
		schema = core.getSchema();
		state1 = schema.getState("state1");
		sessionId = VariantStringUtils.random64BitString(rand);
		CoreSession ssn3 = (CoreSession) core.getSession(sessionId, true).getBody();
		assertTrue(ssn3.getTraversedStates().isEmpty());
		assertTrue(ssn3.getTraversedTests().isEmpty());
		setTargetingStabile(ssn3, "test1.B","test2.D","Test1.A");
		request = ssn3.targetForState(state1);
		assertEqualAsSets(ssn3.getTraversedStates(), new Pair<State, Integer>(state1, 1));
		assertEqualAsSets(ssn3.getTraversedTests(), schema.getTest("test1"));
		assertEqualAsSets(ssn3.getDisqualifiedTests(), schema.getTest("Test1"));

		VariantCoreSession ssn3uncommitted = core.getSession(sessionId, true).getBody();
		assertTrue(ssn3uncommitted.getTraversedStates().isEmpty());
		assertTrue(ssn3uncommitted.getTraversedTests().isEmpty());

		stabile = ssn3.getTargetingStabile();
		assertEquals(2, stabile.getAll().size());
		assertNull(stabile.get("Test1"));
		assertNotNull(stabile.get("test1"));
		assertNotNull(stabile.get("test2"));
		assertEquals(VariantCollectionsUtils.list(schema.getTest("Test1")), disqualListener.testList);
		assertEquals("/path/to/state1/test1.B", request.getResolvedParameter("path"));
		request.commit();

		// Same session, but dispatch to state2 - it's only instrumented by the off test2. 
		// The extra disqualifier should not matter because test1 has already been qualified for this session. The
		disqualListener = new TestQualificationHookListenerDisqualifyImpl(true, schema.getTest("Test1"), schema.getTest("test1"));
		core.addHookListener(disqualListener);
		
		assertTrue(disqualListener.testList.isEmpty());
		schema = core.getSchema();
		state1 = schema.getState("state1");
		state2 = schema.getState("state2");
		setTargetingStabile(ssn3, "test1.B","test2.D","Test1.A");
		request = ssn3.targetForState(state2);
		assertEqualAsSets(ssn3.getTraversedStates(), new Pair<State, Integer>(state1, 1));

		assertEqualAsSets(ssn3.getTraversedTests(), schema.getTest("test1"));

		stabile = ssn3.getTargetingStabile();
		assertEquals(3, stabile.getAll().size());
		assertEquals("A", stabile.get("Test1").getExperienceName());
		assertEquals("B", stabile.get("test1").getExperienceName());
		assertEquals("D", stabile.get("test2").getExperienceName());
		assertTrue(disqualListener.testList.isEmpty());
		assertEquals("/path/to/state2", request.getResolvedParameter("path"));
		request.commit();
		assertEqualAsSets(ssn3.getTraversedStates(), new Pair<State, Integer>(state1, 1));

		assertEqualAsSets(ssn3.getTraversedTests(), schema.getTest("test1"));
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void testTargetingTest() throws Exception {
		
		final VariantCore core = rebootApi();
		ParserResponse response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		Schema schema = core.getSchema();
		final Test t1 = schema.getTest("test1");
		final Test t2 = schema.getTest("test2");
		final Test t3 = schema.getTest("Test1");
		final State s1 = schema.getState("state1");
		final State s2 = schema.getState("state2");
		final State s3 = schema.getState("state3");

		core.clearHookListeners();
		
		// Listen to targeting posts from the off test "test2". Should never be posted.
		TargetingHookListener test2Listener = new TargetingHookListener(t2, t2.getControlExperience());
		core.addHookListener(test2Listener);
				
		String sessionId = VariantStringUtils.random64BitString(rand);
		VariantCoreSession ssn = core.getSession(sessionId, true).getBody();
		ssn.targetForState(s1).commit();
		assertEquals("Off tests should not be targeted", 0, test2Listener.count);
		ssn.targetForState(s2).commit();
		assertEquals("Off tests should not be targeted", 0, test2Listener.count);
		ssn.targetForState(s3).commit();
		assertEquals("Off tests should not be targeted", 0, test2Listener.count);
		
		// Listen to targeting posts from the on test "test1". Should be posted.
		TargetingHookListener test1Listener = new TargetingHookListener(t1, t1.getControlExperience());
		core.addHookListener(test1Listener);

		sessionId = VariantStringUtils.random64BitString(rand);
		ssn = core.getSession(sessionId, true).getBody();
		ssn.targetForState(s1).commit();
		assertEquals(1, test1Listener.count);
		ssn.targetForState(s2).commit();
		assertEquals(1, test1Listener.count);
		ssn.targetForState(s3).commit();
		assertEquals(1, test1Listener.count);

		// Return the wrong experience. Should throw runtime user exception.
		final TargetingHookListener test1BadListener = new TargetingHookListener(t1, t3.getControlExperience());
		core.addHookListener(test1BadListener);
		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() {		
				String sessionId = VariantStringUtils.random64BitString(rand);
				VariantCoreSession ssn = core.getSession(sessionId, true).getBody();
				ssn.targetForState(s2).commit();  // Not instrumented on s2
				assertEquals("Off tests should not be targeted", 0, test1BadListener.count);
				ssn.targetForState(s1);
			}
		}.assertThrown(Error.RUN_HOOK_TARGETING_BAD_EXPERIENCE, test1BadListener.getClass().getName(), t1.getName(), t3.getControlExperience().toString());

	}


	/**
	 * 
	 */
	private static class TestQualificationHookListenerDisqualifyImpl implements HookListener<TestQualificationHook> {

		private ArrayList<com.variant.core.schema.Test> testList = new ArrayList<com.variant.core.schema.Test>();
		private com.variant.core.schema.Test[] testsToDisqualify;
		private boolean removeFromTt;
		
		private TestQualificationHookListenerDisqualifyImpl(boolean removeFromTt, Test...testsToDisqualify) {
			this.testsToDisqualify = testsToDisqualify;
			this.removeFromTt = removeFromTt;
		}

		@Override
		public Class<TestQualificationHook> getHookClass() {
			return TestQualificationHook.class;
		}

		@Override
		public void post(TestQualificationHook hook) {
			assertNotNull(hook.getSession());
			assertNotNull(hook.getTest());
			boolean found = false;
			for (com.variant.core.schema.Test test: testsToDisqualify) {
				if (test.equals(hook.getTest())) {
					found = true;
					break;
				}
			}
			
			if (found) {
				testList.add(hook.getTest());
				hook.setQualified(false);
				hook.setRemoveFromTargetingTracker(removeFromTt);
			}
		}		
	}

	/**
	 * 
	 */
	private static class TargetingHookListener implements HookListener<TestTargetingHook> {

		private int count = 0;
		private Test forTest;
		private Experience targetExperience;
		
		/**
		 * Target for experience exp if posted for test test.
		 * @param test
		 * @param exp 
		 */
		TargetingHookListener(Test forTest, Experience targetExperience) {
			this.forTest = forTest;
			this.targetExperience = targetExperience;
		}
		
		@Override
		public Class<TestTargetingHook> getHookClass() {
			return TestTargetingHook.class;
		}

		@Override
		public void post(TestTargetingHook hook) {
			assertNotNull(hook.getSession());
			assertNotNull(hook.getTest());
			assertNotNull(hook.getState());
			if (hook.getTest().equals(forTest)) {
				count++;
				hook.setTargetedExperience(targetExperience);
			}
		}
		
	};

}
*/
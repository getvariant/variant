package com.variant.server.test;

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
import org.scalatest.Assertions._

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class UserHookTest extends ServerBaseSpec {

   val MESSAGE_TEXT_STATE = "Info-Message-State"
	val MESSAGE_TEXT_TEST = "Info-Message-Test"
	
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
	   
   	val nullListener = new TestQualificationHookListenerNil
	   var ssn = new ServerSession(newSid())

      "be posted for tests instrumented on state1" in {

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
		   ssn.getTraversedStates().size() mustEqual 1
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
	   
	   "not be posted for tests already qualified" in {

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
	   
	   "disqual test2, test6; not disqual test1, and keep all in targeting stabile" in {

	      // New session. Disqualify, but keep in TT.
   	   val schema = server.schema.get
   		val state1 = schema.getState("state1")
   	   val test1 = schema.getTest("test1")
   	   val test2 = schema.getTest("test2")
   	   val test3 = schema.getTest("test3")
   	   val test4 = schema.getTest("test4")
   	   val test5 = schema.getTest("test5")
   	   val test6 = schema.getTest("test6")

   	   server.hooker.clear()
   	   val dl1 = new TestQualificationHookListenerDisqual(false, test1)
   	   val dl2 = new TestQualificationHookListenerDisqual(false, test2)
   	   val dl6 = new TestQualificationHookListenerDisqual(false, test6)
		   server.hooker.addListener(dl1)
		   server.hooker.addListener(dl2)
		   server.hooker.addListener(dl6)

		   ssn = new ServerSession(newSid())
		   setTargetingStabile(ssn, "test6.B", "test2.C", "test1.A")
		   val req = ssn.targetForState(state1);
		   ssn.getTraversedStates().toSet mustEqual Set((state1, 1))
		   ssn.getTraversedTests().toSet mustEqual Set(test3, test4, test5)
		   ssn.getDisqualifiedTests().toSet mustEqual Set(test2, test6)

		   val stabile = ssn.targetingStabile
		   stabile.getAll().size() mustEqual 6  // test1 was not removed
		   stabile.get("test1").toString() must startWith ("test1.A")
		   stabile.get("test2").toString() must startWith ("test2.C")
		   stabile.get("test3").toString() must startWith ("test3")
		   stabile.get("test4").toString() must startWith ("test4")
		   stabile.get("test5").toString() must startWith ("test5")
		   stabile.get("test6").toString() must startWith ("test6.B")
		   req.getResolvedParameter("path") must startWith ("/path/to/state1")

	   }

	   "disqual test1, and drop tfrom targeting stabile" in {

	      // New session. Disqualify, but keep in TT.
   	   val schema = server.schema.get
   		val state1 = schema.getState("state1")
   		val state2 = schema.getState("state2")
   	   val test1 = schema.getTest("test1")
   	   val test2 = schema.getTest("test2")
   	   val test3 = schema.getTest("test3")
   	   val test4 = schema.getTest("test4")
   	   val test5 = schema.getTest("test5")
   	   val test6 = schema.getTest("test6")

   	   // second test1 listener, in addition to one added in previous test.
   	   val dl1 = new TestQualificationHookListenerDisqual(true, test1)
		   server.hooker.addListener(dl1)

		   val req = ssn.targetForState(state2);
		   ssn.getTraversedStates().toSet mustEqual Set((state1,1), (state2,1))
		   ssn.getTraversedTests().toSet mustEqual Set(test3, test4, test5)
		   ssn.getDisqualifiedTests().toSet mustEqual Set(test1, test2, test6)

		   val stabile = ssn.targetingStabile
		   stabile.getAll().size() mustEqual 5  // test1 was removed
		   stabile.get("test1") must be (null)
		   stabile.get("test2").toString() must startWith ("test2.C")
		   stabile.get("test3").toString() must startWith ("test3")
		   stabile.get("test4").toString() must startWith ("test4")
		   stabile.get("test5").toString() must startWith ("test5")
		   stabile.get("test6").toString() must startWith ("test6.B")
		   req.getResolvedParameter("path") must startWith ("/path/to/state2")

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

	/**
	 * Disqualify passed tests and optionally remove their entries from targeting stabile
	 */
	class TestQualificationHookListenerDisqual(removeFromStabile: Boolean, testsToDisqualify:Test*) 
	extends HookListener[TestQualificationHook] {

		val testList = ListBuffer[Test]()
		override def getHookClass() = classOf[TestQualificationHook]
		
		override def post(hook: TestQualificationHook) {
			assert(hook.getSession() != null, "No session passed")
			assert(hook.getTest() != null, "No test passed")
			val test = testsToDisqualify.find { t => t.equals(hook.getTest()) }			
			if (test.isDefined) {
				testList.add(hook.getTest());
				hook.setQualified(false);
				hook.setRemoveFromTargetingTracker(removeFromStabile);
			}
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
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
import com.variant.core.hook.TestTargetingHook
import com.variant.server.ServerErrorException
import com.variant.server.ServerError
import com.variant.core.exception.RuntimeErrorException

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class UserHookTest extends BaseSpecWithSchema {

   val MESSAGE_TEXT_STATE = "Info-Message-State"
	val MESSAGE_TEXT_TEST = "Info-Message-Test"
	
	var schemaId = None
   
	"StateParsedHook listener" should {
	   "be posted when state is parsed" in {
	      
   		val listener = new StateParsedHookListener
   		server.hooker.addListener(listener)
   		val response = server.installSchemaDeployer(SchemaDeployer.fromClasspath("/ParserCovariantOkayBigTest.json")).get
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
         val response = server.installSchemaDeployer(SchemaDeployer.fromClasspath("/ParserCovariantOkayBigTest.json")).get
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
         val response = server.installSchemaDeployer(SchemaDeployer.fromClasspath("/ParserCovariantOkayBigTest.json")).get
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
         val response = server.installSchemaDeployer(SchemaDeployer.fromClasspath("/ParserCovariantOkayBigTest.json")).get
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
		   ssn.getTraversedTests().toSet mustEqual Set(test3, test4, test5, test6)
		   ssn.getDisqualifiedTests().size() mustEqual 0
		   val stabile = ssn.targetingStabile
		   stabile.getAll().size() mustEqual 4
		   stabile.get("test1") must be (null)
		   stabile.get("test2") must be (null)
		   stabile.get("test3") mustNot be (null)
		   stabile.get("test4") mustNot be (null)
		   stabile.get("test5") mustNot be (null)
		   stabile.get("test6") mustNot be (null)
		   nullListener.testList.toSeq mustEqual Seq(test3, test4, test5, test6)

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
		   ssn.getTraversedTests().toSet mustEqual Set(test1, test3, test4, test5, test6)
		   ssn.getDisqualifiedTests().size() mustEqual 0
		   val stabile = ssn.targetingStabile
		   stabile.getAll().size() mustEqual 5
		   stabile.get("test1") mustNot be (null)
		   stabile.get("test2") must be (null)
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
		   ssn.getDisqualifiedTests().toSet mustEqual Set(test6)

		   val stabile = ssn.targetingStabile
		   stabile.getAll().size() mustEqual 6  
		   stabile.get("test1").toString() must startWith ("test1.A") // disqualified but not removed
		   stabile.get("test2").toString() must startWith ("test2.C") // OFF => not removed.
		   stabile.get("test3").toString() must startWith ("test3")
		   stabile.get("test4").toString() must startWith ("test4")
		   stabile.get("test5").toString() must startWith ("test5")
		   stabile.get("test6").toString() must startWith ("test6.B")
		   req.getResolvedParameter("path") must startWith ("/path/to/state1")

	   }

	   "disqual test1, and drop tfrom targeting stabile" in {

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
		   ssn.getDisqualifiedTests().toSet mustEqual Set(test1, test6)

		   val stabile = ssn.targetingStabile
		   stabile.getAll().size() mustEqual 5  
		   stabile.get("test1") must be (null)                        // disqualified and removed
		   stabile.get("test2").toString() must startWith ("test2.C") // OFF => not removed
		   stabile.get("test3").toString() must startWith ("test3")
		   stabile.get("test4").toString() must startWith ("test4")
		   stabile.get("test5").toString() must startWith ("test5")
		   stabile.get("test6").toString() must startWith ("test6.B")
		   req.getResolvedParameter("path") must startWith ("/path/to/state2")

	   }

	   "honor session-current targeting settings when targeting for state3" in {

   	   val schema = server.schema.get
   		val state1 = schema.getState("state1")
   		val state2 = schema.getState("state2")
   		val state3 = schema.getState("state3")
   	   val test1 = schema.getTest("test1")
   	   val test2 = schema.getTest("test2")
   	   val test3 = schema.getTest("test3")
   	   val test4 = schema.getTest("test4")
   	   val test5 = schema.getTest("test5")
   	   val test6 = schema.getTest("test6")

		   val req = ssn.targetForState(state3);
		   ssn.getTraversedStates().toSet mustEqual Set((state1,1), (state2,1), (state3,1))
		   ssn.getTraversedTests().toSet mustEqual Set(test3, test4, test5)
		   ssn.getDisqualifiedTests().toSet mustEqual Set(test1, test6)

		   val stabile = ssn.targetingStabile
		   stabile.getAll().size() mustEqual 5
		   stabile.get("test1") must be (null)                        // disqualified and removed
		   stabile.get("test2").toString() must startWith ("test2.C") // OFF => not removed
		   stabile.get("test3").toString() must startWith ("test3")
		   stabile.get("test4").toString() must startWith ("test4")
		   stabile.get("test5").toString() must startWith ("test5")
		   stabile.get("test6").toString() must startWith ("test6.B")
		   req.getResolvedParameter("path") must startWith ("/path/to/state3")

	   }

	   "not be posted for an OFF test" in {

   	   val schema = server.schema.get
   		val state1 = schema.getState("state1")
   		val state2 = schema.getState("state2")
   		val state3 = schema.getState("state3")
   		val state4 = schema.getState("state4")
   	   val test1 = schema.getTest("test1")
   	   val test2 = schema.getTest("test2")
   	   val test3 = schema.getTest("test3")
   	   val test4 = schema.getTest("test4")
   	   val test5 = schema.getTest("test5")
   	   val test6 = schema.getTest("test6")
		
	   	// ON - should be posted.
		   val l1 = new TargetingHookListener(test1, test1.getExperience("B"));

	   	// OFF - should not be posted.
	   	val l2 = new TargetingHookListener(test2, test2.getExperience("C"))


   	   server.hooker.clear()
   		server.hooker.addListener(l1, l2)
   		
   		// New session.
         ssn = new ServerSession(newSid())
		   ssn.targetForState(state1)
   		l1.count mustBe 0  // Not instrumented
   		l2.count mustBe 0  // Off

   		ssn.targetForState(state2)
   		l1.count mustBe 0 // Instrumented, but unresolvable => didn't post.
   		l2.count mustBe 0 // Off

   		// New session
   		ssn = new ServerSession(newSid())
		   ssn.targetForState(state4)
   		l1.count mustBe 1 
   		l2.count mustBe 0  // Off

   		ssn.targetForState(state4)
   		l1.count mustBe 1
   		l2.count mustBe 0 // Off

	   }
	}
	
	"Runtime" should {

	   "Throw exception if targeting listener returns wrong experience" in {

   	   val schema = server.schema.get
   		val state1 = schema.getState("state1")
   		val state2 = schema.getState("state2")
   		val state3 = schema.getState("state3")
   		val state4 = schema.getState("state4")
   	   val test1 = schema.getTest("test1")
   	   val test2 = schema.getTest("test2")
   	   val test3 = schema.getTest("test3")
   	   val test4 = schema.getTest("test4")
   	   val test5 = schema.getTest("test5")
   	   val test6 = schema.getTest("test6")
		   

		   val l = new TargetingHookListener(test1, test3.getControlExperience())
		   server.hooker.addListener(l)
	
		   var ssn = new ServerSession(newSid())
         ssn.targetForState(state1)   // Ok - state1 is not instrumented by test1
         l.count mustEqual 0
         ssn.targetForState(state2)   // Ok - listener is not posted because test1 is not free at this point.
         l.count mustEqual 0

         ssn = new ServerSession(newSid())
   	   
   	   val caughtEx = intercept[RuntimeErrorException] {
             ssn.targetForState(state2)   // Kaboom
         }
         assert(
               caughtEx.getMessage.equals(
                     new RuntimeErrorException(
                           ServerError.HOOK_TARGETING_BAD_EXPERIENCE, l.getClass.getName, "test1", "test3.A"
                     ).getMessage)
         )         
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

	/**
	 * 
	 */
	class TargetingHookListener(forTest: Test, targetExperience: Test.Experience) 
	extends HookListener[TestTargetingHook] {

		var count = 0
		
		override def getHookClass() = classOf[TestTargetingHook]

		@Override
		override def post(hook: TestTargetingHook) {
			assert(hook.getSession() != null, "No session passed")
			assert(hook.getTest() != null, "No test passed")
			assert(hook.getState() != null, "No state passed")
			if (hook.getTest().equals(forTest)) {
				count += 1
				hook.setTargetedExperience(targetExperience);
			}
		}
	}
	
}

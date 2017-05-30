package com.variant.server.test;

import com.variant.core.LifecycleEvent
import com.variant.core.schema.StateParsedLifecycleEvent
import com.variant.core.schema.State
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.variant.core.UserError.Severity._
import com.variant.server.schema.SchemaDeployer
import com.variant.core.schema.TestParsedLifecycleEvent
import com.variant.core.schema.Test
import com.variant.server.session.ServerSession
import org.scalatest.Assertions._
import com.variant.server.boot.ServerErrorLocal._
import com.variant.core.CommonError._
import com.variant.server.api.ServerException
import com.variant.core.schema.parser.ParserMessageImpl
import com.variant.server.api.TestQualificationLifecycleEvent
import com.variant.server.api.TestTargetingLifecycleEvent
import com.variant.server.api.UserHook

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class UserHookTest extends BaseSpecWithServer {

   val MESSAGE_TEXT_STATE = "Info-Message-State"
	val MESSAGE_TEXT_TEST = "Info-Message-Test"
	
	var schemaId = None
/*   
	"StateParsedHook listener" should {
	   "be posted when state is parsed" in {
	      
   		val listener = new StateParsedHook
   		server.hooker.addHook(listener)
   		val response = server.installSchemaDeployer(SchemaDeployer.fromClasspath("/ParserCovariantOkayBigTest.json")).get
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR) mustNot be (empty)
   		server.schema.isDefined mustBe false
   		response.getMessages.size mustBe 5
   		for (msg <- response.getMessages) {
   			msg.getSeverity mustBe ERROR
   			msg.getText mustBe (new ParserMessageImpl(HOOK_LISTENER_ERROR, MESSAGE_TEXT_STATE).getText)
   			
   		}  
	   }
   }
	   
	"TestParsedHook listener" should {
	   "be posted when test is parsed" in {
	      
   	   server.hooker.clear()
   	   val listener = new TestParsedHook
   	   server.hooker.addHook(listener)
         val response = server.installSchemaDeployer(SchemaDeployer.fromClasspath("/ParserCovariantOkayBigTest.json")).get
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR) mustNot be (empty)
   		server.schema.isDefined mustBe false
   		response.getMessages.size mustBe 6
   		for (msg <- response.getMessages) {
   			msg.getSeverity mustBe ERROR
   			msg.getText mustBe (new ParserMessageImpl(HOOK_LISTENER_ERROR, MESSAGE_TEXT_TEST).getText)
   			
   		}  
	   }
	}

	"StateParsedHook and TestParsedHook listenes" should {
	   "both be posted, states first, then tests" in {
	      
   	   server.hooker.clear()
   	   val stateListener = new StateParsedHook
   	   val testListener = new TestParsedHook
   	   server.hooker.addHook(testListener, stateListener)
         val response = server.installSchemaDeployer(SchemaDeployer.fromClasspath("/ParserCovariantOkayBigTest.json")).get
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR) mustNot be (empty)
   		server.schema.isDefined mustBe false
   		response.getMessages.size mustBe 11
   		for (i <- 0 until response.getMessages.size) {
   		   val msg = response.getMessages.get(i)
   			msg.getSeverity mustBe ERROR
      		msg.getText mustBe (new ParserMessageImpl(HOOK_LISTENER_ERROR, (if (i < 5) MESSAGE_TEXT_STATE else MESSAGE_TEXT_TEST)).getText)
   		}
	   }
	}

	"TestQualificationHook" should {
	   
   	val nullListener = new TestQualificationHookNil
	   var ssn = ServerSession.empty(newSid())

      "be posted for tests instrumented on state1" in {

         server.hooker.clear()
         
    		server.hooker.addHook(nullListener);
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
		   ssn.getTraversedStates.size() mustEqual 1
		   ssn.getTraversedStates.get(state1) mustEqual 1
		   ssn.getTraversedTests.toSet mustEqual Set(test3, test4, test5, test6)
		   ssn.getDisqualifiedTests.size() mustEqual 0
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
		   ssn.getTraversedStates.size() mustEqual 2
		   ssn.getTraversedStates.get(state1) mustEqual 1
		   ssn.getTraversedStates.get(state2) mustEqual 1
		   ssn.getTraversedTests.toSet mustEqual Set(test1, test3, test4, test5, test6)
		   ssn.getDisqualifiedTests.size() mustEqual 0
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
   	   val dl1 = new TestQualificationHookDisqual(false, test1)
   	   val dl2 = new TestQualificationHookDisqual(false, test2)
   	   val dl6 = new TestQualificationHookDisqual(false, test6)
		   server.hooker.addHook(dl1)
		   server.hooker.addHook(dl2)
		   server.hooker.addHook(dl6)

		   ssn = ServerSession.empty(newSid())
		   setTargetingStabile(ssn, "test6.B", "test2.C", "test1.A")
		   val req = ssn.targetForState(state1);
		   ssn.getTraversedStates.toSet mustEqual Set((state1, 1))
		   ssn.getTraversedTests.toSet mustEqual Set(test3, test4, test5)
		   ssn.getDisqualifiedTests.toSet mustEqual Set(test6)

		   val stabile = ssn.targetingStabile
		   stabile.getAll().size() mustEqual 6  
		   stabile.get("test1").toString() must startWith ("test1.A") // disqualified but not removed
		   stabile.get("test2").toString() must startWith ("test2.C") // OFF => not removed.
		   stabile.get("test3").toString() must startWith ("test3")
		   stabile.get("test4").toString() must startWith ("test4")
		   stabile.get("test5").toString() must startWith ("test5")
		   stabile.get("test6").toString() must startWith ("test6.B")
		   req.getResolvedParameters().get("path") must startWith ("/path/to/state1")

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
   	   val dl1 = new TestQualificationHookDisqual(true, test1)
		   server.hooker.addHook(dl1)

		   val req = ssn.targetForState(state2);
		   ssn.getTraversedStates.toSet mustEqual Set((state1,1), (state2,1))
		   ssn.getTraversedTests.toSet mustEqual Set(test3, test4, test5)
		   ssn.getDisqualifiedTests.toSet mustEqual Set(test1, test6)

		   val stabile = ssn.targetingStabile
		   stabile.getAll().size() mustEqual 5  
		   stabile.get("test1") must be (null)                        // disqualified and removed
		   stabile.get("test2").toString() must startWith ("test2.C") // OFF => not removed
		   stabile.get("test3").toString() must startWith ("test3")
		   stabile.get("test4").toString() must startWith ("test4")
		   stabile.get("test5").toString() must startWith ("test5")
		   stabile.get("test6").toString() must startWith ("test6.B")
		   req.getResolvedParameters().get("path") must startWith ("/path/to/state2")

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
		   ssn.getTraversedStates.toSet mustEqual Set((state1,1), (state2,1), (state3,1))
		   ssn.getTraversedTests.toSet mustEqual Set(test3, test4, test5)
		   ssn.getDisqualifiedTests.toSet mustEqual Set(test1, test6)

		   val stabile = ssn.targetingStabile
		   stabile.getAll().size() mustEqual 5
		   stabile.get("test1") must be (null)                        // disqualified and removed
		   stabile.get("test2").toString() must startWith ("test2.C") // OFF => not removed
		   stabile.get("test3").toString() must startWith ("test3")
		   stabile.get("test4").toString() must startWith ("test4")
		   stabile.get("test5").toString() must startWith ("test5")
		   stabile.get("test6").toString() must startWith ("test6.B")
		   req.getResolvedParameters().get("path") must startWith ("/path/to/state3")

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
		   val l1 = new TargetingHook(test1, test1.getExperience("B"));

	   	// OFF - should not be posted.
	   	val l2 = new TargetingHook(test2, test2.getExperience("C"))


   	   server.hooker.clear()
   		server.hooker.addHook(l1, l2)
   		
   		// New session.
         ssn = ServerSession.empty(newSid())
		   ssn.targetForState(state1)
   		l1.count mustBe 0  // Not instrumented
   		l2.count mustBe 0  // Off

   		ssn.targetForState(state2)
   		l1.count mustBe 0 // Instrumented, but unresolvable => didn't post.
   		l2.count mustBe 0 // Off

   		// New session
   		ssn = ServerSession.empty(newSid())
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
		   

		   val l = new TargetingHook(test1, test3.getControlExperience())
		   server.hooker.addHook(l)
	
		   var ssn = ServerSession.empty(newSid())
         ssn.targetForState(state1)   // Ok - state1 is not instrumented by test1
         l.count mustEqual 0
         ssn.targetForState(state2)   // Ok - listener is not posted because test1 is not free at this point.
         l.count mustEqual 0

         ssn = ServerSession.empty(newSid())
   	   
   	   val caughtEx = intercept[ServerException.User] {
             ssn.targetForState(state2)   // Kaboom
         }
         assert(
               caughtEx.getMessage.equals(
                     new ServerException.User(
                           HOOK_TARGETING_BAD_EXPERIENCE, l.getClass.getName, "test1", "test3.A"
                     ).getMessage)
         )         
	   }
	}

	/**
	 * 
	 */
	class StateParsedHook extends UserHook[StateParsedLifecycleEvent] {
	   val stateList = ListBuffer[State]()
		override def getLifecycleEventClass() = classOf[StateParsedLifecycleEvent]
      override def post(event: StateParsedLifecycleEvent) {
	      stateList += event.getState()
			event.addMessage(MESSAGE_TEXT_STATE)
      }
   }

	/**
	 * 
	 */
	class TestParsedHook extends UserHook[TestParsedLifecycleEvent] {
	   val testList = ListBuffer[Test]()
		override def getLifecycleEventClass() = classOf[TestParsedLifecycleEvent]
      override def post(event: TestParsedLifecycleEvent) {
	      testList += event.getTest()
			event.addMessage(MESSAGE_TEXT_TEST)
      }
   }

	/**
	 * Do nothing. Tests should be qualified by default.
	 */
	class TestQualificationHookNil extends UserHook[TestQualificationLifecycleEvent] {
		val testList = ListBuffer[Test]()
		override def getLifecycleEventClass() = classOf[TestQualificationLifecycleEvent]
		override def post(event: TestQualificationLifecycleEvent) {
			testList += event.getTest()
		}		
	}

	/**
	 * Disqualify passed tests and optionally remove their entries from targeting stabile
	 */
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

	/**
	 * 
	 */
	class TargetingHook(forTest: Test, targetExperience: Test.Experience) 
	extends UserHook[TestTargetingLifecycleEvent] {

		var count = 0
		
		override def getLifecycleEventClass() = classOf[TestTargetingLifecycleEvent]

		@Override
		override def post(event: TestTargetingLifecycleEvent) {
			assert(event.getSession() != null, "No session passed")
			assert(event.getTest() != null, "No test passed")
			assert(event.getState() != null, "No state passed")
			if (event.getTest().equals(forTest)) {
				count += 1
				event.setTargetedExperience(targetExperience);
			}
		}
	}
	*/
}

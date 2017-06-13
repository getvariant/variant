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
import com.variant.server.api.Session
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

package com.variant.server.test

import org.apache.commons.io.IOUtils

import scala.collection.JavaConversions._
import com.variant.server.impl.SessionImpl
import com.variant.server.schema.SchemaDeployer
import com.variant.server.test.util.ParameterizedString

class TestQualificationHookTest extends BaseSpecWithServer {
   
   private[this] def generateSchema(hooks: (String,String)*):String = {
      
      /**
       * Inject hooks into the schema.
       */
      val hookList = hooks.foldLeft("") ((s, h) => 
         s + ",{'name':'%s','class':'%s'}".format(h._1,h._2)
      ).substring(1) // lose leading comma
      
      println(hookList)
      val stream = getClass.getResourceAsStream("/ParserCovariantOkayBigTest.json")
      ParameterizedString(IOUtils.toString(stream)).expand("hooks"->hookList)
   }
   
  	"TestQualificationHook" should {
	   
//   	val nullListener = new TestQualificationHookNil
	   var ssn = SessionImpl.empty(newSid())

      "be posted for tests instrumented on state1" in {

         val schemaSrc = generateSchema("nullTargetingHook" -> "com.variant.server.test.hooks.NullTargetingHook")
         //println(schemaSrc)
//    		server.hooker.addHook(nullListener);
         val response = server.installSchemaDeployer(SchemaDeployer.fromString(schemaSrc)).get
   	   response.hasMessages() mustBe false
//   		NullTargetingHook.testList mustBe empty  // using class field is silly. Instead, we should use session params to return state from the hook.
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
//		   nullListener.testList.toSeq mustEqual Seq(test3, test4, test5, test6)

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
//   	   nullListener.testList.clear()
   	   
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
//		   nullListener.testList.toSeq mustEqual Seq(test1)

	   }
/*	   
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

//   	   server.hooker.clear()
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
	   * 
	   */
	}
	
}
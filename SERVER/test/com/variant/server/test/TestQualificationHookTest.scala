package com.variant.server.test

import org.apache.commons.io.IOUtils
import scala.collection.JavaConversions._
import com.variant.server.impl.SessionImpl
import com.variant.server.schema.SchemaDeployer
import com.variant.server.test.util.ParameterizedString
import com.variant.server.api.TestQualificationLifecycleEvent
import com.variant.server.test.hooks.TestQualificationHookNil
import com.variant.core.schema.Hook

class TestQualificationHookTest extends BaseSpecWithServer {
   
   /**
    * Inject all the hooks.
    */
   private[this] def generateSchema(hooks: Map[String, String]):String = {
      
      /**
       * Inject hooks, passed as parameters, into the schema.
       */
      val schemaHooksList = hooks.getOrElse("schema-hooks","")      
      val test1HooksList = hooks.getOrElse("test1-hooks","")
      val test2HooksList = hooks.getOrElse("test2-hooks","")
      val test3HooksList = hooks.getOrElse("test3-hooks","")
      val test4HooksList = hooks.getOrElse("test4-hooks","")
      val test5HooksList = hooks.getOrElse("test5-hooks","")
      val test6HooksList = hooks.getOrElse("test6-hooks","")
      
      val stream = getClass.getResourceAsStream("/ParserCovariantOkayBigTest.json")
      ParameterizedString(IOUtils.toString(stream)).expand(
            "schema-hooks"-> schemaHooksList,
            "test1-hooks"->test1HooksList,
            "test2-hooks"->test2HooksList,
            "test3-hooks"->test3HooksList,
            "test4-hooks"->test4HooksList,
            "test5-hooks"->test5HooksList,
            "test6-hooks"->test6HooksList)
   }
   
  	"TestQualificationHook" should {
	   
	   var ssn = SessionImpl.empty(newSid())

      "be posted for tests instrumented on state1" in {

         val schemaSrc = generateSchema(
            Map(
               "test1-hooks" -> 
               """ {
                     'name' :'nullQualificationHook',
                     'class':'com.variant.server.test.hooks.TestQualificationHookNil'
                   }
               """,
               "test2-hooks" -> 
               """ {
                     'name' :'nullQualificationHook',
                     'class':'com.variant.server.test.hooks.TestQualificationHookNil'
                   }
               """,
               "test3-hooks" -> 
               """ {
                     'name' :'nullQualificationHook',
                     'class':'com.variant.server.test.hooks.TestQualificationHookNil'
                   }
               """
            )
         )
         
         val response = server.installSchemaDeployer(SchemaDeployer.fromString(schemaSrc)).get

   	   response.hasMessages() mustBe false
   		server.schema.isDefined mustBe true
   	   val schema = server.schema.get
   		val state1 = schema.getState("state1")
   	   val test1 = schema.getTest("test1")
   	   val test2 = schema.getTest("test2")
   	   val test3 = schema.getTest("test3")
   	   val test4 = schema.getTest("test4")
   	   val test5 = schema.getTest("test5")
   	   val test6 = schema.getTest("test6")

   	   schema.getHooks() mustBe empty
   	   test1.getHooks.size mustBe 1
   	   val h1 = test1.getHooks.get(0)
   	   h1.getName mustBe "nullQualificationHook"
   	   h1.getClassName mustBe "com.variant.server.test.hooks.TestQualificationHookNil"
   	   h1.getInit mustBe null
   	   test2.getHooks.size mustBe 1
   	   val h2 = test1.getHooks.get(0)
   	   h2.getName mustBe "nullQualificationHook"
   	   h2.getClassName mustBe "com.variant.server.test.hooks.TestQualificationHookNil"
   	   h2.getInit mustBe null
   	   test3.getHooks.size mustBe 1
   	   val h3 = test1.getHooks.get(0)
   	   h3.getName mustBe "nullQualificationHook"
   	   h3.getClassName mustBe "com.variant.server.test.hooks.TestQualificationHookNil"
   	   h3.getInit mustBe null
   	   test4.getHooks.size mustBe 0
   	   test5.getHooks.size mustBe 0
   	   test6.getHooks.size mustBe 0
   	   
   	   // qualification hooks will not be called before targeting.
   	   ssn.getAttribute(TestQualificationHookNil.ATTR_KEY) mustBe null 
   	   
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
		   ssn.getAttribute(TestQualificationHookNil.ATTR_KEY) mustBe "test3"

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
   	   ssn.clearAttribute(TestQualificationHookNil.ATTR_KEY)
   	   ssn.getAttribute(TestQualificationHookNil.ATTR_KEY) mustBe null
   	   
	      val req = ssn.targetForState(state2)
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
		   ssn.getAttribute(TestQualificationHookNil.ATTR_KEY) mustBe "test1"

	   }

	   "disqual test2, test6; not disqual test1, and keep all in targeting stabile" in {

	      // New session. Disqualify, but keep in TT.
         val schemaSrc = generateSchema(
            Map(
                  "test1-hooks" ->
"""               {
                     'name' :'disqualHook',
                     'class':'com.variant.server.test.hooks.TestQualificationHookDisqual',
                     'init':{'removeFromTargetingTracker':false}
                  }
""",
                  "test2-hooks" ->
"""               {
                     'name' :'disqualHook',
                     'class':'com.variant.server.test.hooks.TestQualificationHookDisqual',
                     'init':{'removeFromTargetingTracker':false}
                  }
""",
                  "test6-hooks" ->
"""               {
                     'name' :'disqualHook',
                     'class':'com.variant.server.test.hooks.TestQualificationHookDisqual',
                     'init':{'removeFromTargetingTracker':false}
                  }
"""
            )
         )
         
         val response = server.installSchemaDeployer(SchemaDeployer.fromString(schemaSrc)).get

   	   response.hasMessages() mustBe false
   		server.schema.isDefined mustBe true
   	   val schema = server.schema.get
   		val state1 = schema.getState("state1")
   		val state2 = schema.getState("state2")
   	   val test1 = schema.getTest("test1")
   	   val test2 = schema.getTest("test2")
   	   val test3 = schema.getTest("test3")
   	   val test4 = schema.getTest("test4")
   	   val test5 = schema.getTest("test5")
   	   val test6 = schema.getTest("test6")

   	   schema.getHooks() mustBe empty
   	   test1.getHooks.size mustBe 1
   	   val h1 = test1.getHooks.get(0)
   	   h1.getName mustBe "disqualHook"
   	   h1.getClassName mustBe "com.variant.server.test.hooks.TestQualificationHookDisqual"
   	   h1.getInit mustBe "{\"removeFromTargetingTracker\":false}"
   	   test2.getHooks.size mustBe 1
   	   val h2 = test2.getHooks.get(0)
   	   h2.getName mustBe "disqualHook"
   	   h2.getClassName mustBe "com.variant.server.test.hooks.TestQualificationHookDisqual"
   	   h2.getInit mustBe "{\"removeFromTargetingTracker\":false}"
   	   test3.getHooks.size mustBe 0
   	   test4.getHooks.size mustBe 0
   	   test5.getHooks.size mustBe 0
   	   test6.getHooks.size mustBe 1
   	   val h6 = test6.getHooks.get(0)
   	   h6.getName mustBe "disqualHook"
   	   h6.getClassName mustBe "com.variant.server.test.hooks.TestQualificationHookDisqual"
   	   h6.getInit mustBe "{\"removeFromTargetingTracker\":false}"

   	   // New Session.
		   ssn = SessionImpl.empty(newSid())
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

	   "disqual test1, and drop it from targeting stabile" in {

	      // second test1 listener, in addition to one added in previous test.
   	   //val dl1 = new TestQualificationHookDisqual(true, test1)
		   //server.hooker.addHook(dl1)

	      // New session. Disqualify, but keep in TT.
	      
	      // Same schema as before, but remove from TT on test1.
         val schemaSrc = generateSchema(
            Map(
                  "test1-hooks" ->
"""               {
                     'name' :'disqualHook',
                     'class':'com.variant.server.test.hooks.TestQualificationHookDisqual',
                     'init':{'removeFromTargetingTracker':true}
                  }
"""
            )
         )

         val response = server.installSchemaDeployer(SchemaDeployer.fromString(schemaSrc)).get
   	   response.hasMessages() mustBe false
   		server.schema.isDefined mustBe true

   	   val schema = server.schema.get
   		val state1 = schema.getState("state1")
   		val state2 = schema.getState("state2")
   	   val test1 = schema.getTest("test1")
   	   val test2 = schema.getTest("test2")
   	   val test3 = schema.getTest("test3")
   	   val test4 = schema.getTest("test4")
   	   val test5 = schema.getTest("test5")
   	   val test6 = schema.getTest("test6")

   	   // Note that reusing session after schema reaload is an error. Still works, until
   	   // reloadable schema is implement, and schema reload will invalidate current sessions.
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

	      // Same schema as before, but remove from TT on test1.
         val schemaSrc = generateSchema(
            Map(
               "test2-hooks" -> 
               """ {
                     'name' :'nullQualificationHook',
                     'class':'com.variant.server.test.hooks.TestQualificationHookNil'
                   }
               """
            )
         )

         val response = server.installSchemaDeployer(SchemaDeployer.fromString(schemaSrc)).get
   	   response.hasMessages() mustBe false
   		server.schema.isDefined mustBe true

   	   val schema = server.schema.get
   		val state1 = schema.getState("state1")
		
   		// New session.
         ssn = SessionImpl.empty(newSid())
		   ssn.targetForState(state1)
		   ssn.getAttribute(TestQualificationHookNil.ATTR_KEY) mustBe null
	   }
	}
	
}
package com.variant.server.test.api

import scala.io.Source
import scala.collection.JavaConverters._
import com.variant.server.api.StateRequest.Status._
import com.variant.server.impl.SessionImpl
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.hooks.TestQualificationHookSimple
import com.variant.server.schema.SchemaDeployer
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.impl.StateRequestImpl
import com.variant.core.schema.impl.VariationScopedHookImpl
import java.util.Optional
import java.util.Collections
import com.variant.server.test.spec.TempSchemataDir
import java.io.PrintWriter
import com.variant.server.api.Session

class TestQualificationHookTest extends EmbeddedServerSpec with TempSchemataDir {

   // No schemata to start with
   override lazy val schemata = Set.empty

   /**
    * Inject all the hooks into the schema in /ParserConjointOkayBigTest.json
    */
   private[this] def generateSchema(name: String, hooks: Map[String, String]): String = {

      /**
       * Inject hooks, passed as parameters, into the schema.
       */
      val schemaHooksList = hooks.getOrElse("schema-hooks", "")
      val test1HooksList = hooks.getOrElse("test1-hooks", "")
      val test2HooksList = hooks.getOrElse("test2-hooks", "")
      val test3HooksList = hooks.getOrElse("test3-hooks", "")
      val test4HooksList = hooks.getOrElse("test4-hooks", "")
      val test5HooksList = hooks.getOrElse("test5-hooks", "")
      val test6HooksList = hooks.getOrElse("test6-hooks", "")

      val stream = getClass.getResourceAsStream("/ParserConjointOkayBigTest.json")
      ParameterizedString(Source.fromInputStream(stream).mkString).expand(
         "schema-name" -> name,
         "schema-hooks" -> schemaHooksList,
         "test1-hooks" -> test1HooksList,
         "test2-hooks" -> test2HooksList,
         "test3-hooks" -> test3HooksList,
         "test4-hooks" -> test4HooksList,
         "test5-hooks" -> test5HooksList,
         "test6-hooks" -> test6HooksList)
   }

   "TestQualificationHookSimple" should {

      var ssn: SessionImpl = null
      val schemaName = "TestQualificationHookTest1"

      "be posted for tests instrumented on state1" in {

         val schemaSrc = generateSchema(
            schemaName,
            Map(
               "test1-hooks" ->
                  """ {
               		'init':{'value':'h1'},
                     'class':'com.variant.server.test.hooks.TestQualificationHookSimple'
                   }
               """,
               "test2-hooks" ->
                  """ {
               		'init':{'value':'h2'},
                     'class':'com.variant.server.test.hooks.TestQualificationHookSimple'
                   }
               """,
               "test3-hooks" ->
                  """ {
               		'init':{'value':'h3'},
                     'class':'com.variant.server.test.hooks.TestQualificationHookSimple'
                   }
               """))

         // Write this string
         new PrintWriter(s"${schemataDir}/${schemaName}.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.schemata.get(schemaName).isDefined mustBe true

         server.schemata.get(schemaName).isDefined mustBe true
         val schema = server.schemata.get(schemaName).get.liveGen.get
         val state1 = schema.getState("state1").get
         val test1 = schema.getVariation("test1").get
         val test2 = schema.getVariation("test2").get
         val test3 = schema.getVariation("test3").get
         val test4 = schema.getVariation("test4").get
         val test5 = schema.getVariation("test5").get
         val test6 = schema.getVariation("test6").get
         ssn = SessionImpl.empty(newSid(), schema)

         schema.getMeta.getHooks mustBe Optional.of(Collections.EMPTY_LIST)

         test1.getHooks.get.size mustBe 1
         val h1 = test1.getHooks.get.get(0).asInstanceOf[VariationScopedHookImpl]
         h1.location.getPath mustBe "/variations[0]/hooks[0]/"
         h1.getClassName mustBe "com.variant.server.test.hooks.TestQualificationHookSimple"
         h1.getInit mustBe Optional.of("""{"value":"h1"}""")
         test2.getHooks.get.size mustBe 1
         val h2 = test2.getHooks.get.get(0).asInstanceOf[VariationScopedHookImpl]
         h2.location.getPath mustBe "/variations[1]/hooks[0]/"
         h2.getClassName mustBe "com.variant.server.test.hooks.TestQualificationHookSimple"
         h2.getInit mustBe Optional.of("""{"value":"h2"}""")
         test3.getHooks.get.size mustBe 1
         val h3 = test3.getHooks.get.get(0).asInstanceOf[VariationScopedHookImpl]
         h3.location.getPath mustBe "/variations[2]/hooks[0]/"
         h3.getClassName mustBe "com.variant.server.test.hooks.TestQualificationHookSimple"
         h3.getInit mustBe Optional.of("""{"value":"h3"}""")
         test4.getHooks mustBe Optional.of(Collections.EMPTY_LIST)
         test5.getHooks mustBe Optional.of(Collections.EMPTY_LIST)
         test6.getHooks mustBe Optional.of(Collections.EMPTY_LIST)

         // qualification hooks will not be called before targeting.
         ssn.getAttributes.get(TestQualificationHookSimple.ATTR_NAME) mustBe null

         val req = ssn.targetForState(state1);
         ssn.getTraversedStates.size() mustEqual 1
         ssn.getTraversedStates.get(state1) mustEqual 1
         ssn.getTraversedVariations.asScala.toSet mustEqual Set(test3, test4, test5, test6)
         ssn.getDisqualifiedVariations.size() mustEqual 0
         val stabile = ssn.targetingStabile
         stabile.getAll().size() mustEqual 4
         stabile.get("test1") must be(null)
         stabile.get("test2") must be(null)
         stabile.get("test3") mustNot be(null)
         stabile.get("test4") mustNot be(null)
         stabile.get("test5") mustNot be(null)
         stabile.get("test6") mustNot be(null)
         ssn.getAttributes.get(TestQualificationHookSimple.ATTR_NAME) mustBe "h3.test3"
         req.asInstanceOf[StateRequestImpl].setStatus(Committed)

      }

      "not be posted for tests already qualified" in {

         val schema = server.schemata.get(schemaName).get.liveGen.get
         val state1 = schema.getState("state1").get
         val state2 = schema.getState("state2").get
         val test1 = schema.getVariation("test1").get
         val test2 = schema.getVariation("test2").get
         val test3 = schema.getVariation("test3").get
         val test4 = schema.getVariation("test4").get
         val test5 = schema.getVariation("test5").get
         val test6 = schema.getVariation("test6").get
         ssn.getAttributes.remove(TestQualificationHookSimple.ATTR_NAME)
         ssn.getAttributes.get(TestQualificationHookSimple.ATTR_NAME) mustBe null

         val req = ssn.targetForState(state2)
         ssn.getTraversedStates.size() mustEqual 2
         ssn.getTraversedStates.get(state1) mustEqual 1
         ssn.getTraversedStates.get(state2) mustEqual 1
         ssn.getTraversedVariations.asScala.toSet mustEqual Set(test1, test3, test4, test5, test6)
         ssn.getDisqualifiedVariations.size() mustEqual 0
         val stabile = ssn.targetingStabile
         stabile.getAll().size() mustEqual 5
         stabile.get("test1") mustNot be(null)
         stabile.get("test2") must be(null)
         stabile.get("test3") mustNot be(null)
         stabile.get("test4") mustNot be(null)
         stabile.get("test5") mustNot be(null)
         stabile.get("test6") mustNot be(null)
         ssn.getAttributes.get(TestQualificationHookSimple.ATTR_NAME) mustBe "h1.test1"
      }
   }

   "TestQualificationHookDisqual" should {

      var schemaName = "TestQualificationHookTest2"

      "disqual test2, test6; not disqual test1, and keep all in targeting stabile" in {

         // New session. Disqualify, but keep in TT.
         val schemaSrc = generateSchema(
            schemaName,
            Map(
               "test1-hooks" ->
                  """               {
                     'class':'com.variant.server.test.hooks.TestQualificationHookDisqual'
                  }
""",
               "test2-hooks" ->
                  """               {
                     'class':'com.variant.server.test.hooks.TestQualificationHookDisqual'
                  }
""",
               "test6-hooks" ->
                  """               {
                     'class':'com.variant.server.test.hooks.TestQualificationHookDisqual'
                  }
"""))

         // Write this string
         new PrintWriter(s"${schemataDir}/${schemaName}.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.schemata.get(schemaName).isDefined mustBe true

         val schema = server.schemata.get(schemaName).get.liveGen.get
         val state1 = schema.getState("state1").get
         val state2 = schema.getState("state2").get
         val test1 = schema.getVariation("test1").get
         val test2 = schema.getVariation("test2").get
         val test3 = schema.getVariation("test3").get
         val test4 = schema.getVariation("test4").get
         val test5 = schema.getVariation("test5").get
         val test6 = schema.getVariation("test6").get

         schema.getMeta.getHooks mustBe Optional.of(Collections.EMPTY_LIST)

         test1.getHooks.get.size mustBe 1
         val h1 = test1.getHooks.get.get(0).asInstanceOf[VariationScopedHookImpl]
         h1.location.getPath mustBe "/variations[0]/hooks[0]/"
         h1.getClassName mustBe "com.variant.server.test.hooks.TestQualificationHookDisqual"
         h1.getInit mustBe Optional.empty
         test2.getHooks.get.size mustBe 1
         val h2 = test2.getHooks.get.get(0).asInstanceOf[VariationScopedHookImpl]
         h2.location.getPath mustBe "/variations[1]/hooks[0]/"
         h2.getClassName mustBe "com.variant.server.test.hooks.TestQualificationHookDisqual"
         h2.getInit mustBe Optional.empty
         test3.getHooks mustBe Optional.of(Collections.EMPTY_LIST)
         test4.getHooks mustBe Optional.of(Collections.EMPTY_LIST)
         test5.getHooks mustBe Optional.of(Collections.EMPTY_LIST)
         test6.getHooks.get.size mustBe 1
         val h6 = test6.getHooks.get.get(0).asInstanceOf[VariationScopedHookImpl]
         h6.location.getPath mustBe "/variations[5]/hooks[0]/"
         h6.getClassName mustBe "com.variant.server.test.hooks.TestQualificationHookDisqual"
         h6.getInit mustBe Optional.empty

         // New Session.
         val ssn = SessionImpl.empty(newSid(), schema)
         setTargetingStabile(ssn, "test6.B", "test2.C", "test1.A")
         val req = ssn.targetForState(state1);
         ssn.getTraversedStates.asScala.toSet mustEqual Set((state1, 1))
         ssn.getTraversedVariations.asScala.toSet mustEqual Set(test3, test4, test5)
         ssn.getDisqualifiedVariations.asScala.toSet mustEqual Set(test6)

         val stabile = ssn.targetingStabile
         stabile.getAll().size() mustEqual 6
         stabile.get("test1").toString() must startWith("test1.A") // disqualified but not removed
         stabile.get("test2").toString() must startWith("test2.C") // OFF => not removed.
         stabile.get("test3").toString() must startWith("test3")
         stabile.get("test4").toString() must startWith("test4")
         stabile.get("test5").toString() must startWith("test5")
         stabile.get("test6").toString() must startWith("test6.B")
         req.getResolvedParameters().get("path") must startWith("/path/to/state1")

      }

      var ssn: SessionImpl = null

      "disqual test1, and drop it from targeting stabile" in {

         schemaName = "TestQualificationHookTest3"
         // Same schema as before, but remove from TT on test1.
         val schemaSrc = generateSchema(
            schemaName,
            Map(
               "test1-hooks" ->
                  """               {
                     'class':'com.variant.server.test.hooks.TestQualificationHookDisqual'
                  }
""",
               "test6-hooks" ->
                  """               {
                     'class':'com.variant.server.test.hooks.TestQualificationHookDisqual'
                  }
"""))

         // Write this string
         new PrintWriter(s"${schemataDir}/${schemaName}.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.schemata.get(schemaName).isDefined mustBe true

         val schema = server.schemata.get(schemaName).get.liveGen.get
         val state1 = schema.getState("state1").get
         val state2 = schema.getState("state2").get
         val test1 = schema.getVariation("test1").get
         val test2 = schema.getVariation("test2").get
         val test3 = schema.getVariation("test3").get
         val test4 = schema.getVariation("test4").get
         val test5 = schema.getVariation("test5").get
         val test6 = schema.getVariation("test6").get
         ssn = SessionImpl.empty(newSid(), schema)
         setTargetingStabile(ssn, "test6.B", "test2.C", "test1.A")

         val req = ssn.targetForState(state2);
         ssn.getTraversedStates.asScala.toSet mustEqual Set((state2, 1))
         ssn.getTraversedVariations.asScala.toSet mustEqual Set(test3, test4, test5)
         ssn.getDisqualifiedVariations.asScala.toSet mustEqual Set(test1, test6)

         val stabile = ssn.targetingStabile
         stabile.getAll().size() mustEqual 6
         stabile.get("test1").toString() must startWith("test1")
         stabile.get("test2").toString() must startWith("test2.C") // OFF => not removed
         stabile.get("test3").toString() must startWith("test3")
         stabile.get("test4").toString() must startWith("test4")
         stabile.get("test5").toString() must startWith("test5")
         stabile.get("test6").toString() must startWith("test6.B")
         req.getResolvedParameters().get("path") must startWith("/path/to/state2")
         req.asInstanceOf[StateRequestImpl].setStatus(Committed);
      }

      "honor session-current targeting settings when targeting for state3" in {

         val schema = server.schemata.get(schemaName).get.liveGen.get
         val state1 = schema.getState("state1").get
         val state2 = schema.getState("state2").get
         val state3 = schema.getState("state3").get
         val test1 = schema.getVariation("test1").get
         val test2 = schema.getVariation("test2").get
         val test3 = schema.getVariation("test3").get
         val test4 = schema.getVariation("test4").get
         val test5 = schema.getVariation("test5").get
         val test6 = schema.getVariation("test6").get

         val req = ssn.targetForState(state3);
         ssn.getTraversedStates.asScala.toSet mustEqual Set((state2, 1), (state3, 1))
         ssn.getTraversedVariations.asScala.toSet mustEqual Set(test3, test4, test5)
         ssn.getDisqualifiedVariations.asScala.toSet mustEqual Set(test1, test6)

         val stabile = ssn.targetingStabile
         stabile.getAll().size() mustEqual 6
         stabile.get("test1").toString() must startWith("test1")
         stabile.get("test2").toString() must startWith("test2.C") // OFF => not removed
         stabile.get("test3").toString() must startWith("test3")
         stabile.get("test4").toString() must startWith("test4")
         stabile.get("test5").toString() must startWith("test5")
         stabile.get("test6").toString() must startWith("test6.B")
         req.getResolvedParameters().get("path") must startWith("/path/to/state3")

      }

      "not be posted for an OFF test" in {

         val schemaName = "TestQualificationHookTest4"
         // Same schema as before, but remove from TT on test1.
         val schemaSrc = generateSchema(
            schemaName,
            Map(
               "test2-hooks" ->
                  """ {
                     'class':'com.variant.server.test.hooks.TestQualificationHookSimple',
                     'init':{'value':'foo'}
                   }
               """))

         // Write this string
         new PrintWriter(s"${schemataDir}/${schemaName}.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.schemata.get(schemaName).isDefined mustBe true

         val schema = server.schemata.get(schemaName).get.liveGen.get
         val state1 = schema.getState("state1").get

         // New session.
         ssn = SessionImpl.empty(newSid(), schema)
         ssn.targetForState(state1)
         ssn.getAttributes.get(TestQualificationHookSimple.ATTR_NAME) mustBe null
      }
   }
}
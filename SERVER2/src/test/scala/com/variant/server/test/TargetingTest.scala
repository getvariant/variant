package com.variant.server.test

import java.nio.file.Files
import java.nio.file.Paths

import com.variant.core.error.ServerError
import com.variant.server.boot.ServerExceptionRemote
import com.variant.server.impl.SessionImpl
import com.variant.server.test.hooks.TestTargetingHook
import com.variant.server.test.hooks.TestTargetingHookSimple
import com.variant.server.test.spec.Async
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.spec.TempSchemataDir
import com.variant.server.test.util.ParameterizedString

class TargetingTest extends EmbeddedServerSpec with TempSchemataDir with Async {

   /**
    *  No schemata to start with.
    */
   override protected lazy val schemata = Set.empty

   val trials = 500000
   val deltaAsFraction = .05f

   val schemaPrototype = """
{
   'meta':{
      'name':'${schemaName:}'
   },
   'states':[
      {
         'name':'state1'
      },
      {
         'name':'state2'
      } 
   ],
   'variations':[
      {
         'name':'test1',
         'hooks':[${hooks:}], // Defaults to null list
         'experiences':[ 
            {
               'name':'A', 
               'weight':1 , 
               'isControl':true 
            }, 
            {  
               'name':'B',
               'weight':2  
            },  
            {  
               'name':'C',
               'weight':97 
            }
         ],
         'onStates':[ 
            { 
               'stateRef':'state1' 
            }
         ]
      },
      {
         'name':'test2',
         'experiences':[ 
            {
               'name':'A', 
               'weight':1 , 
               'isControl':true 
            }, 
            {  
               'name':'B',
               'weight':2  
            },  
            {  
               'name':'C',
               'weight':97 
            }
         ],
         'onStates':[ 
            { 
               'stateRef':'state1'
            }
         ]
      } 
   ]
}"""

   val schemaNames = Array(
      "TargetingTestNoHooks",
      "TargetingTest1NullHook",
      "TargetingTest2NullHooks",
      "TargetingTestABHook",
      "TargetingTestABCHook")

   val schemaStrings = Array(

      ParameterizedString(schemaPrototype).expand(
         "schemaName" -> schemaNames(0)),

      ParameterizedString(schemaPrototype).expand(
         "schemaName" -> schemaNames(1),
         "hooks" ->
            """ {
               'init':{'value':'h1'},
               'class':'com.variant.server.test.hooks.TestTargetingHookSimple'
             }
         """),

      ParameterizedString(schemaPrototype).expand(
         "schemaName" -> schemaNames(2),
         "hooks" ->
            """ {
               'init':{'value':'h2'},
               'class':'com.variant.server.test.hooks.TestTargetingHookSimple'
             },
             {
               'init':{'value':'h3'},
               'class':'com.variant.server.test.hooks.TestTargetingHookSimple'
             }
         """),

      ParameterizedString(schemaPrototype).expand(
         "schemaName" -> schemaNames(3),
         "hooks" ->
            """ {
               'init':{'value':'h4'},
               'class':'com.variant.server.test.hooks.TestTargetingHookSimple'
             },
             {
               'class':'com.variant.server.test.hooks.TestTargetingHook',
               'init': {'weights':[1,1,0]}
             }
         """),

      ParameterizedString(schemaPrototype).expand(
         "schemaName" -> schemaNames(4),
         "hooks" ->
            """ {
               'init':{'value':'h5'},
               'class':'com.variant.server.test.hooks.TestTargetingHookSimple'
             },
             {
               'class':'com.variant.server.test.hooks.TestTargetingHook',
               'init': {'weights':[1,1,1]}
             }
         """))

   "Server" should {

      "come up with no schemata" in {
         server.schemata.size mustBe 0
      }

      "deploy multiple schemata from strings" in {

         for ((name, schema) <- schemaNames zip schemaStrings) {
            Files.write(Paths.get(s"${schemataDir}/${name}.json"), schema.getBytes)
         }

         Thread.sleep(dirWatcherLatencyMsecs)
         server.schemata.size mustBe 5

         server.bootExceptions.size mustBe 0

         schemaNames foreach {
            server.schemata.get(_).isDefined mustBe true
         }

      }

   }

   "Runtime" should {

      "target according to weights with no targeting hooks" in {

         async {

            val schema = server.schemata.get(schemaNames(0)).get.liveGen.get
            val state = schema.getState("state1").get
            val test = schema.getVariation("test1").get

            val counts = Array(0, 0, 0)
            for (i <- 1 to trials) {
               val ssn = SessionImpl.empty("sid", schema)
               ssn.targetForState(state)
               val expName = ssn.coreSession.getStateRequest.get.getLiveExperience(test).get.getName()
               expName match {
                  case "A" => counts(0) += 1
                  case "B" => counts(1) += 1
                  case "C" => counts(2) += 1
               }
            }
            verifyCounts(counts, Array(1f, 2f, 97f))
         }
      }

      "target according to weights with null targeting hook" in {

         async {

            val schema = server.schemata.get(schemaNames(1)).get.liveGen.get
            val state = schema.getState("state1").get
            val test = schema.getVariation("test1").get

            val counts = Array(0, 0, 0)
            for (i <- 1 to trials) {
               val ssn = SessionImpl.empty("sid" + i, schema)
               ssn.getAttributes.get(TestTargetingHookSimple.ATTR_NAME) mustBe null
               val req = ssn.targetForState(state)
               ssn.getAttributes.get(TestTargetingHookSimple.ATTR_NAME) mustBe "h1.test1.state1"
               val expName = req.getLiveExperience(test).get.getName()
               expName match {
                  case "A" => counts(0) += 1
                  case "B" => counts(1) += 1
                  case "C" => counts(2) += 1
               }
            }
            verifyCounts(counts, Array(1f, 2f, 97f))
         }
      }

      "target according to weights with two null targeting hooks" in {

         async {

            val schema = server.schemata.get(schemaNames(2)).get.liveGen.get
            val state = schema.getState("state1").get
            val test = schema.getVariation("test1").get

            val counts = Array(0, 0, 0)
            for (i <- 1 to trials) {
               val ssn = SessionImpl.empty("sid" + i, schema)
               ssn.getAttributes.get(TestTargetingHookSimple.ATTR_NAME) mustBe null
               val req = ssn.targetForState(state)
               ssn.getAttributes.get(TestTargetingHookSimple.ATTR_NAME) mustBe "h2.test1.state1 h3.test1.state1"
               val expName = req.getLiveExperience(test).get.getName()
               expName match {
                  case "A" => counts(0) += 1
                  case "B" => counts(1) += 1
                  case "C" => counts(2) += 1
               }
            }
            verifyCounts(counts, Array(1f, 2f, 97f))
         }
      }

      "target at 1/1/0 with the null hook and the A/B hook" in {

         async {

            val schema = server.schemata.get(schemaNames(3)).get.liveGen.get
            val state = schema.getState("state1").get
            val test = schema.getVariation("test1").get

            val counts = Array(0, 0, 0)
            for (i <- 1 to trials) {
               val ssn = SessionImpl.empty("sid" + i, schema)
               ssn.getAttributes.get(TestTargetingHookSimple.ATTR_NAME) mustBe null
               ssn.getAttributes.get(TestTargetingHook.ATTR_KEY) mustBe null
               val req = ssn.targetForState(state)
               ssn.getAttributes.get(TestTargetingHookSimple.ATTR_NAME) mustBe "h4.test1.state1"
               ssn.getAttributes.get(TestTargetingHook.ATTR_KEY) mustBe "test1"
               val expName = req.getLiveExperience(test).get.getName()
               expName match {
                  case "A" => counts(0) += 1
                  case "B" => counts(1) += 1
                  case "C" => counts(2) += 1
               }
            }
            verifyCounts(counts, Array(1f, 1f, 0f))
         }
      }

      "still target at 1/1/1 with the null hook and the A/B/C hook" in {

         async {

            val schema = server.schemata.get(schemaNames(4)).get.liveGen.get
            val state = schema.getState("state1").get
            val test = schema.getVariation("test1").get

            val counts = Array(0, 0, 0)
            for (i <- 1 to trials) {
               val ssn = SessionImpl.empty("sid" + i, schema)
               ssn.getAttributes.get(TestTargetingHookSimple.ATTR_NAME) mustBe null
               ssn.getAttributes.get(TestTargetingHook.ATTR_KEY) mustBe null
               val req = ssn.targetForState(state)
               ssn.getAttributes.get(TestTargetingHookSimple.ATTR_NAME) mustBe "h5.test1.state1"
               ssn.getAttributes.get(TestTargetingHook.ATTR_KEY) mustBe "test1"
               val expName = req.getLiveExperience(test).get.getName()
               expName match {
                  case "A" => counts(0) += 1
                  case "B" => counts(1) += 1
                  case "C" => counts(2) += 1
               }
            }
            verifyCounts(counts, Array(1f, 1f, 1f))
         }
      }

      "join all" in {
         joinAll(60000)
      }

      "Throw exception if targeting hook sets wrong experience" in {

         val schemaName = "bad_experiencee"
         val schemaString = ParameterizedString(schemaPrototype).expand(
            "schemaName" -> schemaName,
            "hooks" ->
               """ {
                     'init':{'value':'h6'},
                     'class':'com.variant.server.test.hooks.TestTargetingHookSimple'
                   },
                   {
                     'class':'com.variant.server.test.hooks.TestTargetingHook',
                     'init': {'experience':'test2.A'}
                   }
               """)

         Files.write(Paths.get(s"${schemataDir}/${schemaName}.json"), schemaString.getBytes)

         Thread.sleep(dirWatcherLatencyMsecs)

         server.schemata.size mustBe 6
         server.bootExceptions.size mustBe 0
         server.schemata.get(schemaName).isDefined mustBe true

         val schema = server.schemata.get(schemaName).get.liveGen.get
         val state1 = schema.getState("state1").get

         var ssn = SessionImpl.empty(newSid(), schema)

         // targeting hook returns an experience not from the wrong test.
         val caughtEx = intercept[ServerExceptionRemote] {
            ssn.targetForState(state1)
         }
         caughtEx.getMessage mustBe
            ServerError.HOOK_TARGETING_BAD_EXPERIENCE.asMessage(classOf[TestTargetingHook].getName, "test1", "test2.A")

      }
   }

   /**
    *
    * @param counts
    * @param weights
    */
   private def verifyCounts(counts: Array[Int], weights: Array[Float]) {
      //println(counts.mkString(","))
      //println(weights.mkString(","))
      var sumCounts = 0
      var sumWeights = 0f
      for (i <- 0 until counts.length) {
         sumCounts += counts(i)
         sumWeights += weights(i)
      }
      for (i <- 0 until counts.length) {
         //System.out.println("Delta: " + (weights[i]/sumWeights * DELTA_AS_FRACTION));
         val countFraction = counts(i) / sumCounts.asInstanceOf[Float]
         // +- will complain if weightsFraction is 0: if so, give it a small positive value instead
         val weightsFraction = weights(i) / sumWeights.asInstanceOf[Float]

         if (weightsFraction == 0)
            countFraction mustEqual 0
         else
            countFraction mustEqual (weightsFraction +- weightsFraction * deltaAsFraction)
      }
   }
}

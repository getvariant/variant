package com.variant.server.test

import com.variant.server.impl.SessionImpl
import com.variant.server.test.hooks.TestTargetingHook
import com.variant.server.test.hooks.TestTargetingHookNil
import com.variant.server.test.util.ParameterizedString
import com.variant.server.api.ServerException
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.schema.SchemaDeployerString

class TargetingTest extends BaseSpecWithServer {

	val trials = 500000
	val deltaAsFraction = .05f

		

   val schemaJson = """
{
   'meta':{
      'name':'TargetingTest'
   },
   'states':[
      {
         'name':'state1',
         'parameters':{
            'path':'/path/to/state1'
         }
      },
      {
         'name':'state2',
         'parameters':{
            'path':'/path/to/state2'
         }
      } 
   ],
   'tests':[
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
               'stateRef':'state1',
               'variants':[ 
                  {
                     'experienceRef': 'B',
                     'parameters':{
                        'path':'/path/to/state1/test1.B'
                     }
                  },
                  { 
                     'experienceRef': 'C',
                     'parameters':{ 
                        'path':'/path/to/state1/test1.C'
                     }
                  }
               ] 
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
               'stateRef':'state1',
               'variants':[ 
                  {
                     'experienceRef': 'B',
                     'parameters':{
                        'path':'/path/to/state1/test2.B'
                     }
                  },
                  { 
                     'experienceRef': 'C',
                     'parameters':{ 
                        'path':'/path/to/state1/test2.C'
                     }
                  }
               ] 
            }
         ]
      } 
   ]
}"""

	"Runtime" should {

      "target according to weights with no targeting hooks" in {

         val schemaSrc = ParameterizedString(schemaJson).expand()
         
         val schemaDeployer = SchemaDeployerString(schemaSrc)
         server.useSchemaDeployer(schemaDeployer)
         val response = schemaDeployer.parserResponse
         server.schema.isDefined mustBe true
         val schema = server.schema.get
         val state = schema.getState("state1")
         val test = schema.getTest("test1")
   		
         val counts = Array(0, 0, 0)
   		for (i <- 1 to trials) {
   			val ssn = SessionImpl.empty("sid")
   			ssn.targetForState(state)
   			val expName = ssn.coreSession.getStateRequest.getLiveExperience(test).getName()
   			expName match {
   			   case "A" => counts(0) += 1
      			case "B" => counts(1) += 1
      			case "C" => counts(2) += 1
   			}
   		} 
   		verifyCounts(counts, Array(1f, 2f, 97f))
      }
      
   	"target according to weights with null targeting hook" in {

   	   val schemaSrc = ParameterizedString(schemaJson).expand(         
               "hooks" -> 
               """ {
                     'name' :'nullTargetingHook',
                     'class':'com.variant.server.test.hooks.TestTargetingHookNil'
                   }
               """)
               
         val schemaDeployer = SchemaDeployerString(schemaSrc)
         server.useSchemaDeployer(schemaDeployer)
         val response = schemaDeployer.parserResponse

         server.schema.isDefined mustBe true
         val schema = server.schema.get
         val state = schema.getState("state1")
         val test = schema.getTest("test1")

   		val counts = Array(0, 0, 0)
   		for (i <- 1 to trials) {
   			val ssn = SessionImpl.empty("sid" + i)
   			ssn.getAttribute(TestTargetingHookNil.ATTR_KEY) mustBe null
   			val req = ssn.targetForState(state)
   			ssn.getAttribute(TestTargetingHookNil.ATTR_KEY) mustBe "test1"
   			val expName = req.getLiveExperience(test).getName()
   			expName match {
   			   case "A" => counts(0) += 1
      			case "B" => counts(1) += 1
      			case "C" => counts(2) += 1
   			}
   		} 
   		verifyCounts(counts, Array(1f, 2f, 97f))
      }
     
      "target according to weights with two null targeting hooks" in {

         val schemaSrc = ParameterizedString(schemaJson).expand(         
               "hooks" -> 
               """ {
                     'name' :'nullTargetingHook1',
                     'class':'com.variant.server.test.hooks.TestTargetingHookNil'
                   },
                   {
                     'name' :'nullTargetingHook2',
                     'class':'com.variant.server.test.hooks.TestTargetingHookNil'
                   }
               """)
               
         val schemaDeployer = SchemaDeployerString(schemaSrc)
         server.useSchemaDeployer(schemaDeployer)
         val response = schemaDeployer.parserResponse

         server.schema.isDefined mustBe true
         val schema = server.schema.get
         val state = schema.getState("state1")
         val test = schema.getTest("test1")

   		val counts = Array(0, 0, 0)
   		for (i <- 1 to trials) {
   			val ssn = SessionImpl.empty("sid" + i)
   			ssn.getAttribute(TestTargetingHookNil.ATTR_KEY) mustBe null
   			val req = ssn.targetForState(state)
   			ssn.getAttribute(TestTargetingHookNil.ATTR_KEY) mustBe "test1 test1"
   			val expName = req.getLiveExperience(test).getName()
   			expName match {
   			   case "A" => counts(0) += 1
      			case "B" => counts(1) += 1
      			case "C" => counts(2) += 1
   			}
   		} 
   		verifyCounts(counts, Array(1f, 2f, 97f))
      }


      "target at 1/1/0 with the null hook and the A/B hook" in {

         
         val schemaSrc = ParameterizedString(schemaJson).expand(         
               "hooks" -> 
               """ {
                     'name' :'nullHook',
                     'class':'com.variant.server.test.hooks.TestTargetingHookNil'
                   },
                   {
                     'name' :'A_B_Hook',
                     'class':'com.variant.server.test.hooks.TestTargetingHook',
                     'init': {'weights':[1,1,0]}
                   }
               """)
               
         val schemaDeployer = SchemaDeployerString(schemaSrc)
         server.useSchemaDeployer(schemaDeployer)
         val response = schemaDeployer.parserResponse

         server.schema.isDefined mustBe true
         val schema = server.schema.get
         val state = schema.getState("state1")
         val test = schema.getTest("test1")

   		val counts = Array(0, 0, 0)
   		for (i <- 1 to trials) {
   			val ssn = SessionImpl.empty("sid" + i)
   			ssn.getAttribute(TestTargetingHookNil.ATTR_KEY) mustBe null
   			ssn.getAttribute(TestTargetingHook.ATTR_KEY) mustBe null
   			val req = ssn.targetForState(state)
   			ssn.getAttribute(TestTargetingHookNil.ATTR_KEY) mustBe "test1"
   			ssn.getAttribute(TestTargetingHook.ATTR_KEY) mustBe "test1"
   			val expName = req.getLiveExperience(test).getName()
   			expName match {
   			   case "A" => counts(0) += 1
      			case "B" => counts(1) += 1
      			case "C" => counts(2) += 1
   			}
   		} 
   		verifyCounts(counts, Array(1f, 1f, 0f))
      }

		"still target at 1/1/1 with the null hook and the A/B/C hook" in {
         
         val schemaSrc = ParameterizedString(schemaJson).expand(         
               "hooks" -> 
               """ {
                     'name' :'nullHook',
                     'class':'com.variant.server.test.hooks.TestTargetingHookNil'
                   },
                   {
                     'name' :'A_B_CHook',
                     'class':'com.variant.server.test.hooks.TestTargetingHook',
                     'init': {'weights':[1,1,1]}
                   }
               """)
               
         val schemaDeployer = SchemaDeployerString(schemaSrc)
         server.useSchemaDeployer(schemaDeployer)
         val response = schemaDeployer.parserResponse

         server.schema.isDefined mustBe true
         val schema = server.schema.get
         val state = schema.getState("state1")
         val test = schema.getTest("test1")

   		val counts = Array(0, 0, 0)
   		for (i <- 1 to trials) {
   			val ssn = SessionImpl.empty("sid" + i)
   			ssn.getAttribute(TestTargetingHookNil.ATTR_KEY) mustBe null
   			ssn.getAttribute(TestTargetingHook.ATTR_KEY) mustBe null
   			val req = ssn.targetForState(state)
   			ssn.getAttribute(TestTargetingHookNil.ATTR_KEY) mustBe "test1"
   			ssn.getAttribute(TestTargetingHook.ATTR_KEY) mustBe "test1"
   			val expName = req.getLiveExperience(test).getName()
   			expName match {
   			   case "A" => counts(0) += 1
      			case "B" => counts(1) += 1
      			case "C" => counts(2) += 1
   			}
   		} 
   		verifyCounts(counts, Array(1f, 1f, 1f))
		}
		
	   "Throw exception if targeting hook sets bad experience" in {

         val schemaSrc = ParameterizedString(schemaJson).expand(         
               "hooks" -> 
               """ {
                     'name' :'nullHook',
                     'class':'com.variant.server.test.hooks.TestTargetingHookNil'
                   },
                   {
                     'name' :'A_B_CHook',
                     'class':'com.variant.server.test.hooks.TestTargetingHook',
                     'init': {'experience':'test2.A'}
                   }
               """)
               
         val schemaDeployer = SchemaDeployerString(schemaSrc)
         server.useSchemaDeployer(schemaDeployer)
         val response = schemaDeployer.parserResponse
         
         server.schema.isDefined mustBe true
         val schema = server.schema.get

         val state1 = schema.getState("state1")
		   
	
		   var ssn = SessionImpl.empty(newSid())
   	   
   	   val caughtEx = intercept[ServerException.User] {
             ssn.targetForState(state1)   // targeting hook returns an experience not from test1
         }
         caughtEx.getMessage mustBe (
                     new ServerException.User(
                           ServerErrorLocal.HOOK_TARGETING_BAD_EXPERIENCE, classOf[TestTargetingHook].getName, "test1", "test2.A"
                     ).getMessage)
 
	   }

	}
	
	/**
	 * 
	 * @param counts
	 * @param weights
	 */
	private def verifyCounts(counts: Array[Int], weights: Array[Float]) {
	   println(counts.mkString(","))
	   println(weights.mkString(","))
		var sumCounts = 0
		var sumWeights = 0f
		for (i <- 0 until counts.length) {
			sumCounts += counts(i)
			sumWeights += weights(i)
		}
		for (i <- 0 until counts.length) {
			//System.out.println("Delta: " + (weights[i]/sumWeights * DELTA_AS_FRACTION));
		   val countFraction = counts(i)/sumCounts.asInstanceOf[Float]
		   // +- will complain if weightsFraction is 0: if so, give it a small positive value instead
		   val weightsFraction = weights(i)/sumWeights.asInstanceOf[Float]

		   if (weightsFraction == 0)
      		countFraction mustEqual 0		      
		   else
      		countFraction mustEqual (weightsFraction +- weightsFraction * deltaAsFraction)
		}
	}
}

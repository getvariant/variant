package com.variant.server.test

import scala.collection.JavaConverters
import com.variant.server.schema.SchemaDeployer
import com.variant.core.schema.State
import com.variant.server.session.ServerSession
import com.variant.core.session.CoreStateRequest
import com.variant.core.util.VariantStringUtils
import scala.collection.JavaConverters
import com.variant.core.HookListener
import com.variant.core.TestTargetingHook
import scala.util.Random

class TargetingTest extends BaseSpecWithServer {

	val trials = 500000
	val deltaAsFraction = .05f

	/**
	 * Use the null event flusher and null session store, 
	 * because the test will be generating lots sessions and events.
	 *
	static {
		// Do nothing to save sessions to speed things up. We won't need them.
		injectorConfigAsResourceName = "/com/variant/core/conf/injector-session-store-null.json";
	}
	*/
		

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
      } 
   ]
}"""

   server.installSchemaDeployer(SchemaDeployer.fromString(schemaJson))
   server.schema.isDefined mustBe true
   val schema = server.schema.get
   val state = schema.getState("state1")
   val test = schema.getTest("test1")

	"Runtime" should {

      "target according to weights if no targeting hooks" in {
				
   		val counts = Array(0, 0, 0)
   		for (i <- 1 to trials) {
   			val ssn = ServerSession.empty("sid")
   			val req = ssn.targetForState(state)
   			val expName = req.getLiveExperience(test).getName()
   			expName match {
   			   case "A" => counts(0) += 1
      			case "B" => counts(1) += 1
      			case "C" => counts(2) += 1
   			}
   		} 
   		verifyCounts(counts, Array(1f, 2f, 97f))
      }
       
   	"target according to weights with null targeting listener" in {

   		val nullListener = new NullTargetingHookListener()
   	   nullListener.postCount mustBe 0
   		server.hooker.addListener(nullListener)
   		val counts = Array(0, 0, 0)
   		for (i <- 1 to trials) {
   			val ssn = ServerSession.empty("sid" + i)
   			val req = ssn.targetForState(state)
   			val expName = req.getLiveExperience(test).getName()
   			expName match {
   			   case "A" => counts(0) += 1
      			case "B" => counts(1) += 1
      			case "C" => counts(2) += 1
   			}
   		} 
   		verifyCounts(counts, Array(1f, 2f, 97f))
   		nullListener.postCount mustEqual trials
      }
      
      "target according to weights with two null targeting listeners" in {

         server.hooker.clear()
         val nullListener1 = new NullTargetingHookListener()
         val nullListener2 = new NullTargetingHookListener()
   		server.hooker.addListener(nullListener1)
   		server.hooker.addListener(nullListener2)
   		val counts = Array(0, 0, 0)
   		for (i <- 1 to trials) {
   			val ssn = ServerSession.empty("sid" + 1)
   			val req = ssn.targetForState(state)
   			val expName = req.getLiveExperience(test).getName()
   			expName match {
   			   case "A" => counts(0) += 1
      			case "B" => counts(1) += 1
      			case "C" => counts(2) += 1
   			}
   		} 
   		verifyCounts(counts, Array(1f, 2f, 97f))
   		nullListener1.postCount mustEqual trials
   		nullListener2.postCount mustEqual trials
      }

      "target at 1/1/0 with the null listener and the A/B listener" in {

         server.hooker.clear()
         val nullListener = new NullTargetingHookListener()
   		val abListener = new ABTargetingHookListener()
   		server.hooker.addListener(nullListener)
   		server.hooker.addListener(abListener)
   		val counts = Array(0, 0, 0)
   		for (i <- 1 to trials) {
   			val ssn = ServerSession.empty("sid" + 1)
   			val req = ssn.targetForState(state)
   			val expName = req.getLiveExperience(test).getName()
   			expName match {
   			   case "A" => counts(0) += 1
      			case "B" => counts(1) += 1
      			case "C" => counts(2) += 1
   			}
   		} 
   		verifyCounts(counts, Array(1f, 1f, 0f))
   		nullListener.postCount mustEqual trials
   		abListener.postCount mustEqual trials
      }
      
		"still target at 1/0/1 with the null listener and the AC listener" in {
         
         server.hooker.clear()
         val nullListener = new NullTargetingHookListener()
   		val acListener = new ACTargetingHookListener()
   		server.hooker.addListener(nullListener)
   		server.hooker.addListener(acListener)
         val counts = Array(0,0,0)
   		for (i <- 1 to trials) {
   			val ssn = ServerSession.empty("sid" + 1)
   			val req = ssn.targetForState(state)
   			val expName = req.getLiveExperience(test).getName()
   			expName match {
   			   case "A" => counts(0) += 1
      			case "B" => counts(1) += 1
      			case "C" => counts(2) += 1
   			}
   		} 
   		nullListener.postCount mustBe trials
   		acListener.postCount mustBe trials
   		verifyCounts(counts, Array(1, 0, 1))
		}

		"trarget at 2/1/1 with the a/b and a/c listeners" in {

		   server.hooker.clear()
   		val abNullListener = new ABNullTargetingHookListener()
		   val acListener = new ACTargetingHookListener()
   		server.hooker.addListener(abNullListener)
   		server.hooker.addListener(acListener)
         val counts = Array(0,0,0)
   		for (i <- 1 to trials) {
   			val ssn = ServerSession.empty("sid" + 1)
   			val req = ssn.targetForState(state)
   			val expName = req.getLiveExperience(test).getName()
   			expName match {
   			   case "A" => counts(0) += 1
      			case "B" => counts(1) += 1
      			case "C" => counts(2) += 1
   			}
   		}
   		abNullListener.postCount mustBe trials
   		acListener.postCount mustBe trials
   		verifyCounts(counts, Array(50, 25, 25))
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

/**
 * targeting listener does nothing, except increments the post counter.
 */
class NullTargetingHookListener extends HookListener[TestTargetingHook] {
	
	var postCount = 0;
	
	override def getHookClass() = classOf[TestTargetingHook]
	
	override def post(hook: TestTargetingHook) { 
	   postCount += 1 
	}

}
/**
 * Targeting listener returns A or B with equal probability.
 */
class ABTargetingHookListener extends HookListener[TestTargetingHook] {

	var postCount = 0;
   val rand = new Random(System.currentTimeMillis())
   
	override def getHookClass() = classOf[TestTargetingHook]
	
	override def post(hook: TestTargetingHook) {
		postCount += 1
		val test = hook.getTest()
		val experience = if (rand.nextBoolean()) test.getExperience("A") else test.getExperience("B")
		hook.setTargetedExperience(experience)
	}
}

/**
 * returns A or C in equal probabilities.
 */
class ACTargetingHookListener extends HookListener[TestTargetingHook] {
	
	var postCount = 0;
	val rand = new Random(System.currentTimeMillis());
	
	override def getHookClass() = classOf[TestTargetingHook]
	
	override def post(hook: TestTargetingHook) {
		postCount += 1
		val test = hook.getTest()
		if (test.getName().equals("test1") && hook.getTargetedExperience() == null) {
			val experience = if (rand.nextBoolean()) test.getExperience("A") else test.getExperience("C")
			hook.setTargetedExperience(experience);
		}
	}
}

/**
 * returns A 25% of the time, B 25% of the time and null 50% of the time..
 */
class ABNullTargetingHookListener extends HookListener[TestTargetingHook] {
	
	var postCount = 0
	val rand = new Random(System.currentTimeMillis())
	
	override def getHookClass() = classOf[TestTargetingHook]
	
	override def post(hook: TestTargetingHook) {
		postCount += 1
		var test = hook.getTest()
		if (test.getName().equals("test1") && hook.getTargetedExperience() == null) {
			val experience = if (rand.nextBoolean()) (if (rand.nextBoolean()) test.getExperience("A") else test.getExperience("B")) else null
			hook.setTargetedExperience(experience);
		}
	}

}

package com.variant.server.test

import java.nio.file.Files
import java.nio.file.Paths

import com.variant.share.error.ServerError._
import com.variant.server.boot.ServerExceptionRemote
import com.variant.server.impl.SessionImpl
import com.variant.server.test.hooks.TestTargetingHook
import com.variant.server.test.hooks.TestTargetingHookSimple
import com.variant.server.test.spec.Async
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.spec.TempSchemataDir
import com.variant.server.test.util.ParameterizedString
import java.util.concurrent.atomic.AtomicInteger
import com.variant.server.api.lifecycle.LifecycleHook
import com.variant.server.api.lifecycle.VariationTargetingLifecycleEvent

class TargetingPhantomTest extends EmbeddedServerSpec with TempSchemataDir with Async {

   /**
    *  No schemata to start with.
    */
   override protected lazy val schemata = Set.empty
   
   "phantom_schema" should {

      val schemaPrototype = ParameterizedString("""
{
   'meta':{
      'name':'phantom_schema' 
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
         'experiences':[ 
            {
               'name':'A',
               'weight': 1,
               'isControl':true 
            }, 
            {  
               'name':'B',
               'weight': 1
            },
            {  
               'name':'C',
               'weight': 1
            }
         ],
         'onStates':[ 
            { 
               'stateRef':'state1' 
            },
            { 
               'stateRef':'state2',
               'variants': [
                 {
                   'experienceRef':'B',
                   'isphantom':true
                 }
               ]
            }            
         ]
      }
   ]
}""")

      "come up" in {
         server.schemata.size mustBe 0
          
         val schemaString = schemaPrototype.expand()
         
         Files.write(Paths.get(s"${schemataDir}/phantom_schema.json"), schemaString.getBytes)

         Thread.sleep(dirWatcherLatencyMillis)
         server.schemata.size mustBe 1
         
      }

      "not target for phantom experience" in {
                  
         val schema = server.schemata.get("phantom_schema").get.liveGen.get
         val state2 = schema.getState("state2").get
         val test = schema.getVariation("test1").get
         val counts = Array.tabulate(3) { _ => new AtomicInteger(0) }
         val trials = 200
         for (i <- 1 to trials) async {
            val ssn = SessionImpl.empty(newSid, schema)
            ssn.targetForState(state2)
            val expName = ssn.coreSession.getStateRequest.get.getLiveExperience(test).get.getName()
            expName match {
                  case "A" => counts(0).incrementAndGet()
                  case "B" => counts(1).incrementAndGet()
                  case "C" => counts(2).incrementAndGet()
            }
         }

         joinAll(6000)
         counts(1).intValue mustBe 0
         counts(0).intValue + counts(2).intValue mustBe trials
      }
      
      "emit server error 707 if session attempts to target for a phantom state" in {
                  
         val schema = server.schemata.get("phantom_schema").get.liveGen.get
         val state1 = schema.getState("state1").get
         val state2 = schema.getState("state2").get
         val test = schema.getVariation("test1").get
         val counts = Array.tabulate(3) { _ => new AtomicInteger(0) }
         val trials = 20 
         for (i <- 1 to trials) async {
            val ssn = SessionImpl.empty(newSid, schema)
            ssn.targetForState(state1)
            val expName = ssn.coreSession.getStateRequest.get.getLiveExperience(test).get.getName()
            expName match {
               case "A" => counts(0).incrementAndGet()
               case "B" => 
                  intercept[ServerExceptionRemote] {
                     ssn.targetForState(state2)
                  }.getMessage mustBe STATE_PHANTOM_IN_EXPERIENCE.asMessage("state2", "test1.B")
                  counts(2).incrementAndGet()
               case "C" => counts(2).incrementAndGet()
            }

            joinAll(6000)
            counts(0).intValue must be > 0
            counts(1).intValue must be > 0
            counts(2).intValue must be > 0
            counts(0).intValue + + counts(1).intValue + counts(2).intValue mustBe trials
            
         }
      }
   }

   "phantom_schema_with_hook" should {

      val schemaPrototype = ParameterizedString("""
{
   'meta':{
      'name':'phantom_schema' 
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
         'hooks':[
           {
              'class': 'com.variant.server.test.Taget4B'
           }
         ],
         'experiences':[ 
            {
               'name':'A',
               'weight': 1,
               'isControl':true 
            }, 
            {  
               'name':'B',
               'weight': 1
            },
            {  
               'name':'C',
               'weight': 1
            }
         ],
         'onStates':[ 
            { 
               'stateRef':'state1' 
            },
            { 
               'stateRef':'state2',
               'variants': [
                 {
                   'experienceRef':'B',
                   'isphantom':true
                 }
               ]
            }            
         ]
      }
   ]
}""")

      "come up" in {
         
         reboot()
         
         server.schemata.size mustBe 0
          
         val schemaString = schemaPrototype.expand()
         
         Files.write(Paths.get(s"${schemataDir}/phantom_schema.json"), schemaString.getBytes)

         Thread.sleep(dirWatcherLatencyMillis)
         server.schemata.size mustBe 1
         
      }
      
      "emit server error 665 if hook attempts to set experience in which target state is phantom" in {
                  
         val schema = server.schemata.get("phantom_schema").get.liveGen.get
         val state2 = schema.getState("state2").get
         val test = schema.getVariation("test1").get
         val counts = Array.tabulate(3) { _ => new AtomicInteger(0) }
         val ssn = SessionImpl.empty(newSid, schema)
         intercept[ServerExceptionRemote] {
            ssn.targetForState(state2)
         }.getMessage() mustBe HOOK_TARGETING_BAD_EXPERIENCE.asMessage("com.variant.server.test.Taget4B", "test1.B", "state2")
      }
   }
}

/**
 * The hook
 */
class Taget4B extends LifecycleHook[VariationTargetingLifecycleEvent] {
			
	override def getLifecycleEventClass(): Class[VariationTargetingLifecycleEvent] = {
		classOf[VariationTargetingLifecycleEvent]
    }
   
	override def post(event: VariationTargetingLifecycleEvent): java.util.Optional[VariationTargetingLifecycleEvent.PostResult] = {
	
	   val result = event.mkPostResult
	   result.setTargetedExperience(event.getVariation.getExperience("B").get)
	   java.util.Optional.of(result)
	}
}

package com.variant.server.test

import com.variant.core.schema.ParserResponse
import com.variant.core.test.VariantBaseTest
import com.variant.core.impl.UserHooker
import com.variant.core.schema.parser.SchemaParser
import com.variant.server.schema.SchemaDeployer
import com.variant.core.exception.VariantRuntimeException
import com.variant.core.exception.RuntimeError
import com.variant.core.exception.RuntimeErrorException
import com.variant.server.session.ServerSession

class RuntimeExceptionTest extends ServerBaseSpec {

   val schemaJson = """
{
   'states':[
      {'name':'state1'},
      {'NAME':'state2'},
   	{'NaMe':'state3'}
   ],		            
	    	    
   'tests':[
      {
         'name':'test1',
         'isOn':true,
   		'experiences':[
   		   {         
   			   'name':'A',
   			   'isControl':true 
   			},
   			{ 
   			   'name':'B' 
   			}
         ],
   		'onStates':[
   		   {
   			   'stateRef':'state1',
   			   'variants':[
   			      {
   			    	   'experienceRef':'B',
   						'parameters':{
   			    	      'path':'/path/to/state1/test1.B'
   			    	   }
   			      }
   			   ]
   			}
         ] 
      },
     //--------------------------------------------------------------------------//	
      {
         'name':'test2', 
   		'isOn': false,
   		'experiences':[
      	   {
      		   'name':'C',
      			'weight':0.5,
      			'isControl':true
   			},
   			{
   			   'name':'D',
   			   'weight':0.6
   			}
         ],
         'onStates':[
            {
               'stateRef':'state1',
   			   'variants':[
   			      {
   			    	   'experienceRef':'D',
   						'parameters':{
   			    	      'path':'/path/to/state1/test2.D'
   			    	   }
   			      }
               ]
   			},
   			{ 
   			   'stateRef':'state2',
   			   'isNonvariant':false,
   			   'variants':[
   			      {    
   			    	   'experienceRef':'D',
   						'parameters':{  
   			    	      'path':'/path/to/state2/test2.D'
   			    	   } 
   			    	} 
   			   ] 
   			},  
   			{
   			   'stateRef':'state3',
   			   'isNonvariant':true
   		   }
         ] 
      }
   //--------------------------------------------------------------------------//	
	]
}
"""
	
   "Runtime" should {

      "throw STATE_NOT_INSTRUMENTED_BY_TEST" in  {

         server.installSchemaDeployer(SchemaDeployer.fromString("my schema", schemaJson))
         server.schema.isDefined mustBe true
         val schema = server.schema.get
         val state2 = schema.getState("state2")

         try {
			   state2.isNonvariantIn(schema.getTest("non-existent"))
			   fail("Expected exception not thrown")
         }
         catch {
            case npe: NullPointerException => // Expected
            case t: Throwable => fail("Unexpected Exception %s".format(t.getMessage))
         }
         
         try {
			   state2.isNonvariantIn(schema.getTest("test1"))
			   fail("Expected exception not thrown")
   		}
	   	catch {
	   	   case vre: VariantRuntimeException =>  vre.getMessage() mustEqual
      			new RuntimeErrorException(RuntimeError.STATE_NOT_INSTRUMENTED_BY_TEST, "state2", "test1").getMessage()
		   }
	   	
      }

      "throw WEIGHT_MISSING" in {

         server.hooker.clear()
         server.installSchemaDeployer(SchemaDeployer.fromString("my schema", schemaJson))
         server.schema.isDefined mustBe true
         val schema = server.schema.get
         val ssn = ServerSession("sid")
   
   		try {
   		   ssn.targetForState(schema.getState("state1"))
			   fail("Expected exception not thrown")
   			}
         catch {
            case vre: VariantRuntimeException =>  vre.getMessage() mustEqual
      			new RuntimeErrorException(RuntimeError.EXPERIENCE_WEIGHT_MISSING, "test1", "A").getMessage()
   		}
      }
   }
}

/*
	/**
	 * Basic targeting
	 * @throws Exception
	 */
	@Test
	public void noWeightsExceptionTest() throws Exception {

		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
			    "        'parameters':{                                        \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
			    "     {  'name':'state2',                                      \n" +
			    "        'parameters':{                                        \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        }                                                     \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':1 ,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B'                                      \n" +
/*			    "              'weight':2                                      \n" + */
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'C',                                     \n" +
			    "              'weight':97                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                       'path':'/path/to/state1/test1.B'       \n" +
			    "                    }                                         \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                       'path':'/path/to/state1/test1.C'       \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		

	}
		
}
*/
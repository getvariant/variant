package com.variant.server.test
/*
import com.variant.core.schema.ParserResponse
import com.variant.core.test.VariantBaseTest
import com.variant.core.impl.UserHooker
import com.variant.core.schema.parser.SchemaParser

object RuntimeExceptionTest {
   val schema = """
{
//==========================================================================//
'states':[
   {'name':'state1'},
   {'NAME':'state2'},                                                   \n" +
	{'NaMe':'state3'}                                                    \n" +
],                                                                      \n" +
			            
//=========================================================================//
			    	    
'tests':[                                                              \n" +
   {                                                                   \n" +
      'name':'test1',                                                  \n" +
      'isOn':false,                                                    \n" +
		'experiences':[                                                  \n" +
		   {                                                             \n" +
			   'name':'A',                                                \n" +
			   'weight':10,                                               \n" +
			   'isControl':true                                           \n" +
			},                                                            \n" +
			{                                                             \n" +
			   'name':'B',                                                \n" +
			   'weight':20                                                \n" +
			}                                                             \n" +
      ],                                                               \n" +
		'onStates':[                                                      \n" +
		   {                                                             \n" +
			   'stateRef':'state1',                                        \n" +
			   'variants':[                                               \n" +
			      {                                                       \n" +
			    	   'experienceRef':'B',                                 \n" +
						'parameters':{                                       \n" +
			    	      'path':'/path/to/state1/test1.B'                  \n" +
			    	   }                                                    \n" +
			      }                                                       \n" +
			   ]                                                          \n" +
			}                                                             \n" +
      ]                                                                \n" +
   },                                                                  \n" +
//--------------------------------------------------------------------------//	
   {                                                                   \n" +
      'name':'test2',                                                  \n" +
		'isOn': false,                                                   \n" +
		'experiences':[                                                  \n" +
   		{                                                             \n" +
   		   'name':'C',                                                \n" +
   			'weight':0.5,                                              \n" +
   			'isControl':true                                           \n" +
			},                                                            \n" +
			{                                                             \n" +
			   'name':'D',                                                \n" +
			   'weight':0.6                                               \n" +
			}                                                             \n" +
			],                                                               \n" +
			    	    "        'onStates':[                                                      \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                        \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test2.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                        \n" +
			    	    "              'isNonvariant':false,                                      \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state2/test2.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state3',                                        \n" +
			    	    "              'isNonvariant':true                                        \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";

}

class RuntimeExceptionTest extends ServerBaseSpec {
	
   /**
	 * RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST
	 */
   "Foo" should {

      "bar" in  {

         val parser = new SchemaParser(new UserHooker())
		   val response = parser.parse(ParserDisjointOkayTest.SCHEMA)
   
	      if (response.hasMessages()) printMessages(response);
      	response.hasMessages() mustBe false
      }
   }
}

	      	final Schema schema = core.getSchema();
		
		State view = schema.getState("state1");
		try {
			assertFalse(view.isNonvariantIn(schema.getTest("non-existent")));
		}
		catch (NullPointerException npe ) { /* expected */ } 

		try {
			assertFalse(view.isNonvariantIn(schema.getTest("test2")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeUserErrorException(Error.RUN_STATE_NOT_INSTRUMENTED_FOR_TEST, "state1", "test2").getMessage(), vre.getMessage());
		}

		view = schema.getState("state2");
		try {
			assertFalse(view.isNonvariantIn(schema.getTest("test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeUserErrorException(Error.RUN_STATE_NOT_INSTRUMENTED_FOR_TEST, "state2", "test1").getMessage(), vre.getMessage());
		}
		try {
			assertFalse(view.isNonvariantIn(schema.getTest("Test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeUserErrorException(Error.RUN_STATE_NOT_INSTRUMENTED_FOR_TEST, "state2", "Test1").getMessage(), vre.getMessage());
		}

		view = schema.getState("state4");
		try {
			assertFalse(view.isNonvariantIn(schema.getTest("test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeUserErrorException(Error.RUN_STATE_NOT_INSTRUMENTED_FOR_TEST, "state4", "test1").getMessage(), vre.getMessage());
		}
		
		view = schema.getState("state5");
		try {
			assertFalse(view.isNonvariantIn(schema.getTest("test1")));
		}
		catch (VariantRuntimeException vre ) { 
			assertEquals(new VariantRuntimeUserErrorException(Error.RUN_STATE_NOT_INSTRUMENTED_FOR_TEST, "state5", "test1").getMessage(), vre.getMessage());
		}

	}

	/**
	 * RUN_NO_VIEW_FOR_PATH
	 */
	@Test
	public void runNoViewForPath_Test() throws Exception {

		ParserResponse response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		final Schema schema = core.getSchema();		
		assertNull(schema.getState("non-existent"));

	}
}
*/

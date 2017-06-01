package com.variant.server.test;

import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.*;
import static com.variant.core.UserError.Severity.*;

import com.variant.core.UserError.Severity;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.State;
import com.variant.core.schema.StateParsedLifecycleEvent;
import com.variant.server.api.UserHook;
import com.variant.server.schema.SchemaDeployerFromString;


/**
 * @author Igor
 * 
 */
@SuppressWarnings("serial")
public class StateParsedHookTestJava extends BaseSpecWithServer {

   private static final String MESSAGE = "Info-Message-State";
   
   @Test
   public void stateParsedHookTest() {
	   
	   final String SCHEMA = 

					"{                                                                                 \n" +
						    "  'meta':{                                                                \n" +		    	    
						    "     'name':'allTestsOffTest',                                            \n" +
						    "     'comment':'!@#$%^&*',                                                \n" +
						    "     'hooks':[                                                            \n" +
						    "        {'name':'one', 'className':'c.v.s.one'},                          \n" +
						    "        {'name':'two', 'className':'c.v.s.two'},                          \n" +
						    "        {'name':'anotherOne', 'className':'c.v.s.one'},                   \n" +
						    "        {'name':'three', 'className':'c.v.s.three'},                      \n" +
						    "        {'name':'four', 'className':'c.v.s.four'},                        \n" +
						    "        {'name':'anotherFour', 'className':'c.v.s.four'}                  \n" +
						    "      ]                                                                   \n" +
						    "  },                                                                      \n" +
				    	    //==========================================================================//
				    	   
				    	    "   'states':[                                                             \n" +
				    	    "     {'name':'state1'},                                                   \n" +
				    	    "     {'NAME':'state2'},                                                   \n" +
				    	    "     {'NaMe':'state3'}                                                    \n" +
				            "  ],                                                                      \n" +
				            
				    	    //=========================================================================//
				    	    
					        "  'tests':[                                                              \n" +
				    	    "     {                                                                   \n" +
				    	    "        'name':'test1',                                                  \n" +
				    	    "        'isOn':false,                                                    \n" +
				    	    "        'experiences':[                                                  \n" +
				    	    "           {                                                             \n" +
				    	    "              'name':'A',                                                \n" +
				    	    "              'weight':10,                                               \n" +
				    	    "              'isControl':true                                           \n" +
				    	    "           },                                                            \n" +
				    	    "           {                                                             \n" +
				    	    "              'name':'B',                                                \n" +
				    	    "              'weight':20                                                \n" +
				    	    "           }                                                             \n" +
				    	    "        ],                                                               \n" +
				    	    "        'onStates':[                                                      \n" +
				    	    "           {                                                             \n" +
				    	    "              'stateRef':'state1',                                        \n" +
				    	    "              'variants':[                                               \n" +
				    	    "                 {                                                       \n" +
				    	    "                    'experienceRef':'B',                                 \n" +
							"                    'parameters':{                                       \n" +
				    	    "                       'path':'/path/to/state1/test1.B'                  \n" +
				    	    "                    }                                                    \n" +
				    	    "                 }                                                       \n" +
				    	    "              ]                                                          \n" +
				    	    "           }                                                             \n" +
				    	    "        ]                                                                \n" +
				    	    "     },                                                                  \n" +
				    	    //--------------------------------------------------------------------------//	
				    	    "     {                                                                   \n" +
				    	    "        'name':'test2',                                                  \n" +
				    	    "        'isOn': false,                                                   \n" +
				    	    "        'experiences':[                                                  \n" +
				    	    "           {                                                             \n" +
				    	    "              'name':'C',                                                \n" +
				    	    "              'weight':0.5,                                              \n" +
				    	    "              'isControl':true                                           \n" +
				    	    "           },                                                            \n" +
				    	    "           {                                                             \n" +
				    	    "              'name':'D',                                                \n" +
				    	    "              'weight':0.6                                               \n" +
				    	    "           }                                                             \n" +
				    	    "        ],                                                               \n" +
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
	   
	ParserResponse response = new SchemaDeployerFromString(SCHEMA).deploy();
	assertTrue(response.getMessages(FATAL).isEmpty());
	assertFalse(response.getMessages(ERROR).isEmpty());
	System.out.println("********************");
	/*
   		server.schema.isDefined mustBe false
   		response.getMessages.size mustBe 5
   		for (msg <- response.getMessages) {
   			msg.getSeverity mustBe ERROR
   			msg.getText mustBe (new ParserMessageImpl(HOOK_LISTENER_ERROR, message).getText)
   			
   		} */ 	   
   }

	/**
	 * 
	 */
	public static class StateParsedHook implements UserHook<StateParsedLifecycleEvent> {
	   private ArrayList<State> stateList = new ArrayList<State>();

	   @Override
	   public Class getLifecycleEventClass() {
		   return StateParsedLifecycleEvent.class;
	   }
	   
      @Override
      public void post(StateParsedLifecycleEvent event) {
	      stateList.add(event.getState());
	      event.addMessage(Severity.INFO, MESSAGE);
      }
	}
}

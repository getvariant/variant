package com.variant.web;

import static org.junit.Assert.assertEquals;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.Severity;
import com.variant.web.adapter.StateParsedFlashpointListenerImpl;

public class StateParsedFlashpointTest {

	@Test
	public void leadingSlashTest() {
		
		String schema = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
			    "        'parameters':{                                        \n" +
			    "           'path':'does/not/start/with/slash'                 \n" +
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
			    "              'name':'B',                                     \n" +
			    "              'weight':2                                      \n" +
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
		
		VariantWeb api = new VariantWeb();
		api.bootstrap();
		api.addFlashpointListener(new StateParsedFlashpointListenerImpl());
		ParserResponse response = api.parseSchema(IOUtils.toInputStream(schema));
		assertEquals(1, response.getMessages().size());
		ParserMessage msg = response.getMessages().get(0);
		assertEquals(Severity.ERROR, msg.getSeverity());
		assertEquals("USER_DEFINED_MESSAGE", msg.getCode());
		assertEquals("Path property [does/not/start/with/slash] must start with a '/' in State [state1]", msg.getMessage());

	}
}

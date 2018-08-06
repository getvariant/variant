package com.variant.core.test;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.SchemaParser;

/**
 * Various comments 
 * @author Igor
 *
 */
public class ParserCommentsTest extends BaseTestCore {
	
	/**
	 * JSON_PARSE
	 * @throws Exception
	 */
	@Test
	public void jsonParse_Test() throws Exception {
		
		String schema = 
				"// Some comments in the beginning                               \n" +
				"/// Some more comments in the beginning                       //\n" +
				"////////////////////////////////////////////////////////////////\n" +
				"{// comment                                                     \n" +
				"         // mid line                                            \n" +
			    "   'meta':{///   more comment                                   \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                    //more comment\n" +		    	    
			    "     {  'name':'state1'                                       \n" +
			    "     }                                                        \n" +
			    "  ],/// after punctuation                                     \n" +
				"  'tests':[                                                   \n" +
			    "     {                                                        \n" +
			    "        'name':'Test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'B'                       \n" +
			    "                 }                                           \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                           //\n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);
		printMessages(response);
		assertFalse(response.hasMessages());
	}
}
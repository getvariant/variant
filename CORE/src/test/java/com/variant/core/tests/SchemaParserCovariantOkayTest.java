package com.variant.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.variant.core.Variant;
import com.variant.core.schema.Schema;
import com.variant.core.schema.View;
import com.variant.core.schema.impl.ParserResponse;


/**
 * All Tests:
 * Tests Views
 *       1 2 3 4 5
 * test1   + + - -
 * test2 - + + +  
 * test3 + + - - -
 * blank: not instrumented, -: invariant, +: has variants.
 * 
 * @author Igor
 */
public class SchemaParserCovariantOkayTest extends BaseTest {
	

	/**
	 * 
	 * @throws Exception
	 */
	@Before
	public void beforeEachTest() throws Exception {

		// Bootstrap the Variant container with defaults.
		Variant.Config variantConfig = new Variant.Config();
		variantConfig.getSessionServiceConfig().setKeyResolverClassName("com.variant.ext.session.SessionKeyResolverJunit");
		Variant.bootstrap(variantConfig);

	}

	/**
	 * A, B(A), C
	 * 
	 */
	@Test
	public void oneTest() throws Exception {
		
		final String schema = 

		"{                                                                                \n" +
	    	    //==========================================================================//
	    	   
	    	    "   'views':[                                                             \n" +
	    	    "     {  'name':'view1',                                                  \n" +
	    	    "        'path':'/path/to/view1'                                          \n" +
	    	    "     },                                                                  \n" +
	    	    "     {  'NAME':'view2',                                                  \n" +
	    	    "        'path':'/path/to/view2'                                          \n" +
	    	    "     },                                                                  \n" +
	    	    "     {  'nAmE':'view3',                                                  \n" +
	    	    "        'path':'/path/to/view3'                                          \n" +
	    	    "     },                                                                  \n" +
	    	    "     {  'name':'view4',                                                  \n" +
	    	    "        'path':'/path/to/view4'                                          \n" +
	    	    "     },                                                                  \n" +
	    	    "     {  'name':'view5',                                                  \n" +
	    	    "        'path':'/path/to/view5'                                          \n" +
	    	    "     }                                                                   \n" +
	            "  ],                                                                     \n" +
	            
	    	    //=========================================================================//
	    	    
		        "  'tests':[                                                              \n" +
	    	    "     {                                                                   \n" +
	    	    "        'name':'test1',                                                  \n" +
	    	    "        'experiences':[                                                  \n" +
	    	    "           {                                                             \n" +
	    	    "              'name':'A',                                                \n" +
	    	    "              'weight':10,                                               \n" +
	    	    "              'isControl':true                                           \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'name':'B',                                                \n" +
	    	    "              'weight':20                                                \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'name':'C',                                                \n" +
	    	    "              'weight':30                                                \n" +
	    	    "           }                                                             \n" +
	    	    "        ],                                                               \n" +
	    	    "        'onViews':[                                                      \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'view2',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'path':'/path/to/view2/test1.B'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'path':'/path/to/view2/test1.C'                      \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'view3',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'path':'/path/to/view3/test1.B'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'path':'/path/to/view3/test1.C'                      \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'view4',                                         \n" +
	    	    "              'isInvariant':true                                         \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'view5',                                         \n" +
	    	    "              'isInvariant':true                                         \n" +
	    	    "           }                                                             \n" +
	    	    "        ]                                                                \n" +
	    	    "     },                                                                  \n" +
	    	    //--------------------------------------------------------------------------//	
	    	    "     {                                                                   \n" +
	    	    "        'name':'test1',                                                  \n" +
	    	    "        'experiences':[                                                  \n" +
	    	    "           {                                                             \n" +
	    	    "              'name':'A',                                                \n" +
	    	    "              'weight':10,                                               \n" +
	    	    "              'isControl':true                                           \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'name':'B',                                                \n" +
	    	    "              'weight':20                                                \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'name':'C',                                                \n" +
	    	    "              'weight':30                                                \n" +
	    	    "           }                                                             \n" +
	    	    "        ],                                                               \n" +
	    	    "        'onViews':[                                                      \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'view2',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'path':'/path/to/view2/test1.B'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'path':'/path/to/view2/test1.C'                      \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'view3',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'path':'/path/to/view3/test1.B'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'path':'/path/to/view3/test1.C'                      \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'view4',                                         \n" +
	    	    "              'isInvariant':true                                         \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'view5',                                         \n" +
	    	    "              'isInvariant':true                                         \n" +
	    	    "           }                                                             \n" +
	    	    "        ]                                                                \n" +
	    	    "     }                                                                   \n" +
	    	    //--------------------------------------------------------------------------//	
	     	    "  ]                                                                      \n" +
	    	    "}                                                                         ";

		Variant.main();
		System.out.println("%%%%%%%%%" + System.getProperty("variant.version"));

		ParserResponse response = Variant.parseSchema(schema);
		if (response.hasErrors()) printErrors(response);
		assertFalse(response.hasErrors());

	}

	
}


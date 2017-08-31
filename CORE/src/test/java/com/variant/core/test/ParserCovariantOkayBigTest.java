package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.variant.core.UserError.Severity;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Test;
import com.variant.core.schema.parser.ParserResponseImpl;
import com.variant.core.schema.parser.SchemaParser;
import com.variant.core.util.VariantCollectionsUtils;


/**
 * @author Igor
 */
public class ParserCovariantOkayBigTest extends BaseTestCore {

	/**
	 * 
	 */
	@org.junit.Test
	public void test() throws Exception {
		
		SchemaParser parser = getSchemaParser();
		ParserResponseImpl response = (ParserResponseImpl) parser.parse(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNotNull(response.getSchema());
		assertNotNull(response.getSchemaSrc());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertFalse(response.hasMessages(Severity.ERROR));
		assertFalse(response.hasMessages(Severity.WARN));
		assertFalse(response.hasMessages(Severity.INFO));
		
		assertNotNull(response.getSchemaSrc());
		Schema schema = response.getSchema();

		assertEquals(schema.getName(), "OkayBigTest");
		assertEquals(schema.getComment(), "Schema for okay big test!");
		
		final Test test1 = schema.getTest("test1");
		final Test test2 = schema.getTest("test2");
		final Test test3 = schema.getTest("test3");
		final Test test4 = schema.getTest("test4");
		final Test test5 = schema.getTest("test5");
		final Test test6 = schema.getTest("test6");

		final State state1 = schema.getState("state1");
		final State state2 = schema.getState("state2");
		final State state3 = schema.getState("state3");
		final State state4 = schema.getState("state4");
		final State state5 = schema.getState("state5");

		// 
		// Declared test covariance.
		//
		assertNull(test1.getCovariantTests());
		assertNull(test2.getCovariantTests());
		assertEquals(VariantCollectionsUtils.list(test2), test3.getCovariantTests());
		assertEquals(VariantCollectionsUtils.list(test1), test4.getCovariantTests());
		assertEquals(VariantCollectionsUtils.list(test2, test4), test5.getCovariantTests());
		assertEquals(VariantCollectionsUtils.list(test1, test2, test4, test5), test6.getCovariantTests());

		//
		// Test disjointness. 
		//

		// test1
		for (final Test t: schema.getTests()) {			
			
			new ExceptionInterceptor<IllegalArgumentException>() {
				@Override public void toRun() { assertFalse(test1.isSerialWith(t)); }
				@Override public void onThrown(IllegalArgumentException e) { assertEquals(test1, t); }
				@Override public void onNotThrown() { assertNotEquals(test1, t); }
				@Override public Class<IllegalArgumentException> getExceptionClass() { return IllegalArgumentException.class; }
			}.run();
		}
		
		// test2
		for (final Test t: schema.getTests()) {
			
			new ExceptionInterceptor<IllegalArgumentException>() {
				@Override public void toRun() { assertFalse(test2.isSerialWith(t)); }
				@Override public void onThrown(IllegalArgumentException e) { assertEquals(test2, t); }
				@Override public void onNotThrown() { assertNotEquals(test2, t); }
				@Override public Class<IllegalArgumentException> getExceptionClass() { return IllegalArgumentException.class; }
			}.run();
			
		}

		// test3
		for (final Test t: schema.getTests()) {

			new ExceptionInterceptor<IllegalArgumentException>() {
					@Override public void toRun() { 
						if (t.equals(test6)) assertTrue(test3.isSerialWith(t));
						else assertFalse(test3.isSerialWith(t));
					}
				@Override public void onThrown(IllegalArgumentException e) { assertEquals(test3, t); }
				@Override public void onNotThrown() { assertNotEquals(test3, t); }
				@Override public Class<IllegalArgumentException> getExceptionClass() { return IllegalArgumentException.class; }
			}.run();
		}

		// test4
		for (final Test t: schema.getTests()) {
			new ExceptionInterceptor<IllegalArgumentException>() {
				@Override public void toRun() { assertFalse(test4.isSerialWith(t)); }
				@Override public void onThrown(IllegalArgumentException e) { assertEquals(test4, t); }
				@Override public void onNotThrown() { assertNotEquals(test4, t); }
				@Override public Class<IllegalArgumentException> getExceptionClass() { return IllegalArgumentException.class; }
			}.run();
		}

		// test5
		for (final Test t: schema.getTests()) {
			new ExceptionInterceptor<IllegalArgumentException>() {
				@Override public void toRun() { assertFalse(test5.isSerialWith(t)); }
				@Override public void onThrown(IllegalArgumentException e) { assertEquals(test5, t); }
				@Override public void onNotThrown() { assertNotEquals(test5, t); }
				@Override public Class<IllegalArgumentException> getExceptionClass() { return IllegalArgumentException.class; }
			}.run();
		}

		// 
		// test1 OnState objects
		//
		List<Test.OnState> onStates = test1.getOnStates();
		assertEquals(4, onStates.size());
		
		// state2
		Test.OnState onState = onStates.get(0);
		assertFalse(onState.isNonvariant());
		assertEquals(state2, onState.getState());
		List<StateVariant> variants = onState.getVariants();
		assertEquals(2, variants.size());

		StateVariant variant = variants.get(0);
		assertEquals(variant.getExperience(), test1.getExperience("B"));
		assertEquals("/path/to/state2/test1.B", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test1.getExperience("C"));
		assertEquals("/path/to/state2/test1.C", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());

		// state3
		onState = onStates.get(1);
		assertFalse(onState.isNonvariant());
		assertEquals(state3, onState.getState());
		variants = onState.getVariants();
		assertEquals(2, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test1.getExperience("B"));
		assertEquals("/path/to/state3/test1.B", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test1.getExperience("C"));
		assertEquals("/path/to/state3/test1.C", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());

		// state4
		onState = onStates.get(2);
		assertTrue(onState.isNonvariant());
		assertEquals(state4, onState.getState());
		variants = onState.getVariants();
		assertEquals(0, variants.size());

		// state5
		onState = onStates.get(3);
		assertTrue(onState.isNonvariant());
		assertEquals(state5, onState.getState());
		variants = onState.getVariants();
		assertEquals(0, variants.size());

		// 
		// test2 OnSate objects
		//
		onStates = test2.getOnStates();
		assertEquals(4, onStates.size());
		
		// state1
		onState = onStates.get(0);
		assertTrue(onState.isNonvariant());
		assertTrue(onState.getVariants().isEmpty());
		assertEquals(state1, onState.getState());

		// state2
		onState = onStates.get(1);
		assertFalse(onState.isNonvariant());
		assertEquals(state2, onState.getState());
		variants = onState.getVariants();
		assertEquals(2, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals("/path/to/state2/test2.B", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals("/path/to/state2/test2.C", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());

		// state3
		onState = onStates.get(2);
		assertFalse(onState.isNonvariant());
		assertEquals(state3, onState.getState());
		variants = onState.getVariants();
		assertEquals(2, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals("/path/to/state3/test2.B", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals("/path/to/state3/test2.C", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());

		// state4
		onState = onStates.get(3);
		assertFalse(onState.isNonvariant());
		assertEquals(state4, onState.getState());
		variants = onState.getVariants();
		assertEquals(2, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals("/path/to/state4/test2.B", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals("/path/to/state4/test2.C", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());

		// 
		// test3 OnState objects
		//
		onStates = test3.getOnStates();
		assertEquals(4, onStates.size());
		
		// state1
		onState = onStates.get(0);
		assertTrue(onState.isNonvariant());
		assertEquals(state1, onState.getState());
		variants = onState.getVariants();
		assertEquals(0, variants.size());

		// state2
		onState = onStates.get(1);
		assertFalse(onState.isNonvariant());
		assertEquals(state2, onState.getState());
		variants = onState.getVariants();
		assertEquals(6, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals("/path/to/state2/test3.B", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals("/path/to/state2/test3.C", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());
		
		variant = variants.get(2);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test2.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test2.B+test3.B", variant.getParameter("path"));

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test2.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test2.C+test3.B", variant.getParameter("path"));

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test2.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test2.B+test3.C", variant.getParameter("path"));

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test2.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test2.C+test3.C", variant.getParameter("path"));

		// state3
		onState = onStates.get(2);
		assertTrue(onState.isNonvariant());
		assertEquals(state3, onState.getState());
		variants = onState.getVariants();
		assertEquals(0, variants.size());

		// state4
		onState = onStates.get(3);
		assertFalse(onState.isNonvariant());
		assertEquals(state4, onState.getState());
		variants = onState.getVariants();
		assertEquals(6, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals("/path/to/state4/test3.B", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals("/path/to/state4/test3.C", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());
		
		variant = variants.get(2);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test2.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state4/test2.B+test3.B", variant.getParameter("path"));

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test2.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state4/test2.C+test3.B", variant.getParameter("path"));

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test2.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state4/test2.B+test3.C", variant.getParameter("path"));

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test2.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state4/test2.C+test3.C", variant.getParameter("path"));

		
		// 
		// test4 OnState objects
		//
		onStates = test4.getOnStates();
		assertEquals(4, onStates.size());
		
		// state1
		onState = onStates.get(0);
		assertFalse(onState.isNonvariant());
		assertEquals(state1, onState.getState());
		variants = onState.getVariants();
		assertEquals(2, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test4.getExperience("B"));
		assertEquals("/path/to/state1/test4.B", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals("/path/to/state1/test4.C", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());

		// state2
		onState = onStates.get(1);
		assertFalse(onState.isNonvariant());
		assertEquals(state2, onState.getState());
		variants = onState.getVariants();
		assertEquals(6, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test4.getExperience("B"));
		assertEquals("/path/to/state2/test4.B", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals("/path/to/state2/test4.C", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test4.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test1.B+test4.B", variant.getParameter("path"));

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test4.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test1.C+test4.B", variant.getParameter("path"));

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test1.B+test4.C", variant.getParameter("path"));

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test1.C+test4.C", variant.getParameter("path"));
		
		// state4
		onState = onStates.get(2);
		assertTrue(onState.isNonvariant());
		assertEquals(state4, onState.getState());
		assertEquals(0, onState.getVariants().size());

		// state5
		onState = onStates.get(3);
		assertTrue(onState.isNonvariant());
		assertEquals(state5, onState.getState());
		assertEquals(0, onState.getVariants().size());

		// 
		// test5 OnState objects
		//
		onStates = test5.getOnStates();
		assertEquals(5, onStates.size());
		
		// state1
		onState = onStates.get(0);
		assertFalse(onState.isNonvariant());
		assertEquals(state1, onState.getState());
		variants = onState.getVariants();
		assertEquals(6, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals("/path/to/state1/test5.B", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals("/path/to/state1/test5.C", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());

		// state2
		onState = onStates.get(1);
		assertFalse(onState.isNonvariant());
		assertEquals(state2, onState.getState());
		variants = onState.getVariants();
		assertEquals(10, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals("/path/to/state2/test5.B", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals("/path/to/state2/test5.C", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/state2/test2.B+test5.B", variant.getParameter("path"));

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/state2/test2.C+test5.B", variant.getParameter("path"));

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/state2/test2.B+test5.C", variant.getParameter("path"));

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/state2/test2.C+test5.C", variant.getParameter("path"));

		variant = variants.get(6);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/state2/test4.B+test5.B", variant.getParameter("path"));

		variant = variants.get(7);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/state2/test4.C+test5.B", variant.getParameter("path"));

		variant = variants.get(8);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/state2/test4.B+test5.C", variant.getParameter("path"));

		variant = variants.get(9);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/state2/test4.C+test5.C", variant.getParameter("path"));
		
		// state3
		onState = onStates.get(2);
		assertTrue(onState.isNonvariant());
		assertEquals(state3, onState.getState());
		assertEquals(0, onState.getVariants().size());
		
		// state4
		onState = onStates.get(3);
		assertFalse(onState.isNonvariant());
		assertEquals(state4, onState.getState());
		variants = onState.getVariants();
		assertEquals(6, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals("/path/to/state4/test5.B", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals("/path/to/state4/test5.C", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/state4/test2.B+test5.B", variant.getParameter("path"));

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/state4/test2.C+test5.B", variant.getParameter("path"));

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/state4/test2.B+test5.C", variant.getParameter("path"));

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/state4/test2.C+test5.C", variant.getParameter("path"));
				
		// state5
		onState = onStates.get(4);
		assertTrue(onState.isNonvariant());
		assertEquals(state5, onState.getState());
		assertEquals(0, onState.getVariants().size());

		// 
		// test6 OnState objects
		//
		onStates = test6.getOnStates();
		assertEquals(4, onStates.size());
		
		// state1
		onState = onStates.get(0);
		assertFalse(onState.isNonvariant());
		assertEquals(state1, onState.getState());
		variants = onState.getVariants();
		assertEquals(18, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals("/path/to/state1/test6.B", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals("/path/to/state1/test6.C", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/state1/test4.B+test6.B", variant.getParameter("path"));

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/state1/test4.C+test6.B", variant.getParameter("path"));

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/state1/test4.B+test6.C", variant.getParameter("path"));

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/state1/test4.C+test6.C", variant.getParameter("path"));

		variant = variants.get(6);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test5.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/state1/test5.B+test6.B", variant.getParameter("path"));

		variant = variants.get(7);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test5.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/state1/test5.C+test6.B", variant.getParameter("path"));

		variant = variants.get(8);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test5.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/state1/test5.B+test6.C", variant.getParameter("path"));

		variant = variants.get(9);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test5.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/state1/test5.C+test6.C", variant.getParameter("path"));

		variant = variants.get(10);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("B"), test5.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/state1/test4.B+test5.B+test6.B", variant.getParameter("path"));
		
		variant = variants.get(11);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("B"), test5.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/state1/test4.B+test5.C+test6.B", variant.getParameter("path"));

		variant = variants.get(12);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C"), test5.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/state1/test4.C+test5.B+test6.B", variant.getParameter("path"));

		variant = variants.get(13);		
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C"), test5.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/state1/test4.C+test5.C+test6.B", variant.getParameter("path"));
		
		variant = variants.get(14);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("B"), test5.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/state1/test4.B+test5.B+test6.C", variant.getParameter("path"));
		
		variant = variants.get(15);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("B"), test5.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/state1/test4.B+test5.C+test6.C", variant.getParameter("path"));

		variant = variants.get(16);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C"), test5.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/state1/test4.C+test5.B+test6.C", variant.getParameter("path"));

		variant = variants.get(17);		
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C"), test5.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/state1/test4.C+test5.C+test6.C", variant.getParameter("path"));		

		// state2
		onState = onStates.get(1);
		assertTrue(onState.isNonvariant());
		assertEquals(state2, onState.getState());
		assertEquals(0, onState.getVariants().size());

		// state3
		onState = onStates.get(2);
		assertFalse(onState.isNonvariant());
		assertEquals(state3, onState.getState());
		variants = onState.getVariants();
		assertEquals(10, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals("/path/to/state3/test6.B", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals("/path/to/state3/test6.C", variant.getParameter("path"));
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/state3/test1.B+test6.B", variant.getParameter("path"));

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/state3/test1.C+test6.B", variant.getParameter("path"));

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/state3/test1.B+test6.C", variant.getParameter("path"));

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/state3/test1.C+test6.C", variant.getParameter("path"));

		variant = variants.get(6);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/state3/test2.B+test6.B", variant.getParameter("path"));

		variant = variants.get(7);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/state3/test2.C+test6.B", variant.getParameter("path"));

		variant = variants.get(8);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/state3/test2.B+test6.C", variant.getParameter("path"));

		variant = variants.get(9);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/state3/test2.C+test6.C", variant.getParameter("path"));
		
		
		// state2
		onState = onStates.get(3);
		assertTrue(onState.isNonvariant());
		assertEquals(state5, onState.getState());
		assertEquals(0, onState.getVariants().size());

	}

}


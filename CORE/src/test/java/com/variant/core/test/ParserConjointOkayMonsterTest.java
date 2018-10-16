package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.variant.core.UserError.Severity;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Variation;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.SchemaParser;
import com.variant.core.util.CollectionsUtils;
import com.variant.core.util.IoUtils;


/**
 * @author Igor
 */
public class ParserConjointOkayMonsterTest extends BaseTestCore {

	/**
	 * 
	 */
	@org.junit.Test
	public void test() throws Exception {
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = (ParserResponse) parser.parse(IoUtils.openResourceAsStream("/schema/monster.schema")._1());
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

		assertEquals(schema.getMeta().getName(), "OkayBigTest");
		assertEquals(schema.getMeta().getComment(), "Schema for okay big test!");
		
		final Variation test1 = schema.getVariation("test1").get();
		final Variation test2 = schema.getVariation("test2").get();
		final Variation test3 = schema.getVariation("test3").get();
		final Variation test4 = schema.getVariation("test4").get();
		final Variation test5 = schema.getVariation("test5").get();
		final Variation test6 = schema.getVariation("test6").get();

		final State state1 = schema.getState("state1").get();
		final State state2 = schema.getState("state2").get();
		final State state3 = schema.getState("state3").get();
		final State state4 = schema.getState("state4").get();
		final State state5 = schema.getState("state5").get();

		// 
		// Declared conjoint tests.
		//
		assertEquals(0, test1.getConjointVariations().size());
		assertEquals(0, test2.getConjointVariations().size());
		assertEquals(CollectionsUtils.list(test2), test3.getConjointVariations());
		assertEquals(CollectionsUtils.list(test1), test4.getConjointVariations());
		assertEquals(CollectionsUtils.list(test2, test4), test5.getConjointVariations());
		assertEquals(CollectionsUtils.list(test1, test2, test4, test5), test6.getConjointVariations());

		//
		// Resulting test concurrency 
		//

		// test1
		assertFalse(test1.isSerialWith(test1));
		assertTrue(test1.isConcurrentWith(test1));
		assertFalse(test1.isSerialWith(test2));
		assertTrue(test1.isConcurrentWith(test2));
		assertFalse(test1.isSerialWith(test3));
		assertTrue(test1.isConcurrentWith(test3));
		assertFalse(test1.isSerialWith(test4));
		assertTrue(test1.isConcurrentWith(test4));
		assertFalse(test1.isSerialWith(test5));
		assertTrue(test1.isConcurrentWith(test5));
		assertFalse(test1.isSerialWith(test6));
		assertTrue(test1.isConcurrentWith(test6));

		// test2
		assertFalse(test2.isSerialWith(test1));
		assertTrue(test2.isConcurrentWith(test1));
		assertFalse(test2.isSerialWith(test2));
		assertTrue(test2.isConcurrentWith(test2));
		assertFalse(test2.isSerialWith(test3));
		assertTrue(test2.isConcurrentWith(test3));
		assertFalse(test2.isSerialWith(test4));
		assertTrue(test2.isConcurrentWith(test4));
		assertFalse(test2.isSerialWith(test5));
		assertTrue(test2.isConcurrentWith(test5));
		assertFalse(test2.isSerialWith(test6));
		assertTrue(test2.isConcurrentWith(test6));

		// test3
		assertFalse(test3.isSerialWith(test1));
		assertTrue(test3.isConcurrentWith(test1));
		assertFalse(test3.isSerialWith(test2));
		assertTrue(test3.isConcurrentWith(test2));
		assertFalse(test3.isSerialWith(test3));
		assertTrue(test3.isConcurrentWith(test3));
		assertTrue(test3.isSerialWith(test4));
		assertFalse(test3.isConcurrentWith(test4));
		assertFalse(test3.isSerialWith(test5));
		assertTrue(test3.isConcurrentWith(test5));
		assertFalse(test3.isSerialWith(test6));
		assertTrue(test3.isConcurrentWith(test6));

		// test4
		assertFalse(test4.isSerialWith(test1));
		assertTrue(test4.isConcurrentWith(test1));
		assertFalse(test4.isSerialWith(test2));
		assertTrue(test4.isConcurrentWith(test2));
		assertTrue(test4.isSerialWith(test3));
		assertFalse(test4.isConcurrentWith(test3));
		assertFalse(test4.isSerialWith(test4));
		assertTrue(test4.isConcurrentWith(test4));
		assertFalse(test4.isSerialWith(test5));
		assertTrue(test4.isConcurrentWith(test5));
		assertFalse(test4.isSerialWith(test6));
		assertTrue(test4.isConcurrentWith(test6));

		// test5
		assertFalse(test5.isSerialWith(test1));
		assertTrue(test5.isConcurrentWith(test1));
		assertFalse(test5.isSerialWith(test2));
		assertTrue(test5.isConcurrentWith(test2));
		assertFalse(test5.isSerialWith(test3));
		assertTrue(test5.isConcurrentWith(test3));
		assertFalse(test5.isSerialWith(test4));
		assertTrue(test5.isConcurrentWith(test4));
		assertFalse(test5.isSerialWith(test5));
		assertTrue(test5.isConcurrentWith(test5));
		assertFalse(test5.isSerialWith(test6));
		assertTrue(test5.isConcurrentWith(test6));

		// test6
		assertFalse(test6.isSerialWith(test1));
		assertTrue(test6.isConcurrentWith(test1));
		assertFalse(test6.isSerialWith(test2));
		assertTrue(test6.isConcurrentWith(test2));
		assertFalse(test6.isSerialWith(test3));
		assertTrue(test6.isConcurrentWith(test3));
		assertFalse(test6.isSerialWith(test4));
		assertTrue(test6.isConcurrentWith(test4));
		assertFalse(test6.isSerialWith(test5));
		assertTrue(test6.isConcurrentWith(test5));
		assertFalse(test6.isSerialWith(test6));
		assertTrue(test6.isConcurrentWith(test6));

		// 
		// test1 OnState objects
		//
		List<Variation.OnState> onStates = test1.getOnStates();
		assertEquals(4, onStates.size());
		
		// state2
		Variation.OnState onState = onStates.get(0);
		assertEquals(state2, onState.getState());
		StateVariant[] variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(2, variants.length);

		StateVariant variant = variants[0];
		assertEquals(variant.getExperience(), test1.getExperience("B").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());
		
		variant = variants[1];
		assertEquals(variant.getExperience(), test1.getExperience("C").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		// state3
		onState = onStates.get(1);
		assertEquals(state3, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(2, variants.length);
		
		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test1.getExperience("B").get());
		assertEquals("/path/to/state3/test1.B", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());
		
		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test1.getExperience("C").get());
		assertEquals("/path/to/state3/test1.C", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		// state4
		onState = onStates.get(2);
		assertEquals(state4, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(2, variants.length);

		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test1.getExperience("B").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());
		
		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test1.getExperience("C").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		// state5
		onState = onStates.get(3);
		assertEquals(state5, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(2, variants.length);

		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test1.getExperience("B").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());
		
		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test1.getExperience("C").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		// 
		// test2 OnSate objects
		//
		onStates = test2.getOnStates();
		assertEquals(4, onStates.size());
		
		// state1
		onState = onStates.get(0);
		assertEquals(state1, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(2, variants.length);
		
		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test2.getExperience("B").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test2.getExperience("C").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		// state2
		onState = onStates.get(1);
		assertEquals(state2, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(2, variants.length);
		
		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test2.getExperience("B").get());
		assertEquals("/path/to/state2/test2.B", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test2.getExperience("C").get());
		assertEquals("/path/to/state2/test2.C", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		// state3
		onState = onStates.get(2);
		assertEquals(state3, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(2, variants.length);
		
		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test2.getExperience("B").get());
		assertEquals("/path/to/state3/test2.B", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test2.getExperience("C").get());
		assertEquals("/path/to/state3/test2.C", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		// state4
		onState = onStates.get(3);
		assertEquals(state4, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(2, variants.length);
		
		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test2.getExperience("B").get());
		assertEquals("/path/to/state4/test2.B", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test2.getExperience("C").get());
		assertEquals("/path/to/state4/test2.C", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		// 
		// test3 OnState objects
		//
		onStates = test3.getOnStates();
		assertEquals(2, onStates.size());
		
		// state1
		onState = onStates.get(0);
		assertEquals(state1, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(6, variants.length);

		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test3.getExperience("B").get());
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test3.getExperience("C").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());
		
		variant = variants[2];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test3.getExperience("B").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test2.getExperience("B").get(), variant.getConjointExperiences().get(0));
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());


		variant = variants[3];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test3.getExperience("C").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test2.getExperience("B").get(), variant.getConjointExperiences().get(0));
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[4];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test3.getExperience("B").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test2.getExperience("C").get(), variant.getConjointExperiences().get(0));
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[5];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test3.getExperience("C").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test2.getExperience("C").get(), variant.getConjointExperiences().get(0));
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());


		// state3
		onState = onStates.get(1);
		assertEquals(state3, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(6, variants.length);

		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test3.getExperience("B").get());
		assertEquals("/path/to/state3/test3.B", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test3.getExperience("C").get());
		assertEquals("/path/to/state3/test3.C", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());
		
		variant = variants[2];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test3.getExperience("B").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test2.getExperience("B").get(), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state3/test2.B+test3.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[3];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test3.getExperience("C").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test2.getExperience("B").get(), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state3/test2.B+test3.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[4];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test3.getExperience("B").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test2.getExperience("C").get(), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state3/test2.C+test3.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[5];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test3.getExperience("C").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test2.getExperience("C").get(), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state3/test2.C+test3.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());
		
		// 
		// test4 OnState objects
		//
		onStates = test4.getOnStates();
		assertEquals(3, onStates.size());
		
		// state2
		onState = onStates.get(0);
		assertEquals(state2, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(6, variants.length);

		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test4.getExperience("B").get());
		assertEquals("/path/to/state2/test4.B", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());
		
		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test4.getExperience("C").get());
		assertEquals("/path/to/state2/test4.C", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		variant = variants[2];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test4.getExperience("B").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("B").get(), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state2/test1.B+test4.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[3];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test4.getExperience("C").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("B").get(), variant.getConjointExperiences().get(0));
		// This one is inferred
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[4];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test4.getExperience("B").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("C").get(), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state2/test1.C+test4.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[5];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test4.getExperience("C").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("C").get(), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state2/test1.C+test4.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());
		
		// state4
		onState = onStates.get(1);
		assertEquals(state4, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(6, variants.length);

		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test4.getExperience("B").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());
		
		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test4.getExperience("C").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		variant = variants[2];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test4.getExperience("B").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("B").get(), variant.getConjointExperiences().get(0));
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[3];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test4.getExperience("C").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("B").get(), variant.getConjointExperiences().get(0));
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[4];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test4.getExperience("B").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("C").get(), variant.getConjointExperiences().get(0));
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[5];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test4.getExperience("C").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("C").get(), variant.getConjointExperiences().get(0));
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		// state5
		onState = onStates.get(2);
		assertEquals(state5, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(6, variants.length);

		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test4.getExperience("B").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());
		
		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test4.getExperience("C").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		variant = variants[2];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test4.getExperience("B").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("B").get(), variant.getConjointExperiences().get(0));
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[3];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test4.getExperience("C").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("B").get(), variant.getConjointExperiences().get(0));
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[4];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test4.getExperience("B").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("C").get(), variant.getConjointExperiences().get(0));
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[5];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test4.getExperience("C").get());
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("C").get(), variant.getConjointExperiences().get(0));
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		// 
		// test5 OnState objects
		//
		onStates = test5.getOnStates();
		assertEquals(5, onStates.size());
		
		// state1
		onState = onStates.get(0);
		assertEquals(state1, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(6, variants.length);

		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("B").get());
		assertEquals("/path/to/state1/test5.B", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("C").get());
		assertEquals("/path/to/state1/test5.C", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		// state2
		onState = onStates.get(1);
		assertEquals(state2, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(10, variants.length);

		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("B").get());
		assertEquals("/path/to/state2/test5.B", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());
		
		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("C").get());
		assertEquals("/path/to/state2/test5.C", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		variant = variants[2];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state2/test2.B+test5.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[3];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state2/test2.B+test5.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[4];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state2/test2.C+test5.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[5];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state2/test2.C+test5.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());		
		
		variant = variants[6];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state2/test4.B+test5.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[7];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state2/test4.B+test5.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());
		
		variant = variants[8];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state2/test4.C+test5.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[9];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state2/test4.C+test5.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());
		
		// state3
		onState = onStates.get(2);
		assertEquals(state3, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(6, variants.length);
		
		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("B").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());
		
		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("C").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		variant = variants[2];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[3];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[4];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[5];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());		
		
		// state4
		onState = onStates.get(3);
		assertEquals(state4, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(10, variants.length);

		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("B").get());
		assertEquals("/path/to/state4/test5.B", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());
		
		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("C").get());
		assertEquals("/path/to/state4/test5.C", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		variant = variants[2];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state4/test2.B+test5.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[3];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state4/test2.B+test5.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[4];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state4/test2.C+test5.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[5];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state4/test2.C+test5.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());		
		
		variant = variants[6];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[7];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());
		
		variant = variants[8];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[9];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());
				
		// state5
		onState = onStates.get(4);
		assertEquals(state5, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(6, variants.length);
		
		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("B").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());
		
		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("C").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		variant = variants[2];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[3];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[4];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[5];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test5.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());		
		
		// 
		// test6 OnState objects
		//
		onStates = test6.getOnStates();
		assertEquals(4, onStates.size());
		
		// state1
		onState = onStates.get(0);
		assertEquals(state1, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(18, variants.length);

		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());
		
		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		variant = variants[2];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[3];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[4];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[5];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[6];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[7];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[8];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[9];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[10];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());
		
		variant = variants[11];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[12];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[13];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());
		
		variant = variants[14];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());
		
		variant = variants[15];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[16];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[17];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));		
		assertFalse(variant.isProper());

		// state2
		onState = onStates.get(1);
		assertEquals(state2, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		
		variant = variants[0];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(0, variant.getConjointExperiences().size());
		assertNull(variant.getParameters().get("path"));
		assertTrue(variant.isProper());

		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(0, variant.getConjointExperiences().size());
		assertNull(variant.getParameters().get("path"));
		assertTrue(variant.isProper());

		variant = variants[2];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state2/test1.B+test6.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[3];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[4];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[5];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[6];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[7];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[8];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[9];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[10];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[11];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[12];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("B").get(), test4.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[13];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("B").get(), test4.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[14];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("C").get(), test4.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[15];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("C").get(), test4.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state2/test1.C+test4.B+test6.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[16];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[17];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[18];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("B").get(), test4.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[19];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("B").get(), test4.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[20];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("C").get(), test4.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[21];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("C").get(), test4.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[22];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[23];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[24];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[25];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[26];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[27];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[28];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("B").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[29];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("B").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[30];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("C").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[31];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("C").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[32];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[33];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[34];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[35];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[36];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[37];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[38];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("B").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[39];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("B").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[40];		
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("C").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[41];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("C").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		
		
		// state3
		onState = onStates.get(2);
		assertEquals(state3, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(22, variants.length);

		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());
		
		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		variant = variants[2];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state3/test1.B+test6.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[3];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state3/test1.B+test6.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());
		
		variant = variants[4];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state3/test1.C+test6.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());
		
		variant = variants[5];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state3/test1.C+test6.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());
		
		variant = variants[6];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state3/test2.B+test6.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[7];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state3/test2.B+test6.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[8];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state3/test2.C+test6.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());
		
		variant = variants[9];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state3/test2.C+test6.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[10];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[11];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());
		
		variant = variants[12];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[13];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[14];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[15];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[16];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[17];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());
		
		variant = variants[18];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[19];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("B").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[20];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[21];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test2.getExperience("C").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());
				
		
		// state5
		onState = onStates.get(3);
		assertEquals(state5, onState.getState());
		variants = onState.getVariants().toArray(new StateVariant[0]);
		assertEquals(30, variants.length);
		//for (int i = 0; i < variants.length; i++) System.out.println(i + ": " + variants[i]);

		variant = variants[0];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals("/path/to/state5/test6.B", variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());
		
		variant = variants[1];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertNull(variant.getParameters().get("path"));
		assertEquals(0, variant.getConjointExperiences().size());
		assertTrue(variant.isProper());

		variant = variants[2];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test1.B+test6.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[3];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test1.B+test6.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[4];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test1.C+test6.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[5];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test1.C+test6.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[6];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test4.B+test6.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[7];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test4.B+test6.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[8];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("B").get(), test4.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[9];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("B").get(), test4.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[10];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("C").get(), test4.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[11];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("C").get(), test4.getExperience("B").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[12];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test4.C+test6.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());
		
		variant = variants[13];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test4.C+test6.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[14];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("B").get(), test4.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[15];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("B").get(), test4.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[16];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("C").get(), test4.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[17];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test1.getExperience("C").get(), test4.getExperience("C").get()), variant.getConjointExperiences());
		assertNull(variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[18];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test5.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test5.B+test6.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());
		
		variant = variants[19];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test5.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test5.B+test6.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[20];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("B").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test4.B+test5.B+test6.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[21];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("B").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test4.B+test5.B+test6.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[22];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("C").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test4.C+test5.B+test6.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[23];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("C").get(), test5.getExperience("B").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test4.C+test5.B+test6.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());
		
		variant = variants[24];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test5.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test5.C+test6.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[25];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test5.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test5.C+test6.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[26];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("B").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test4.B+test5.C+test6.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[27];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("B").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test4.B+test5.C+test6.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[28];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("B").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("C").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test4.C+test5.C+test6.B", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

		variant = variants[29];
		assertEquals(onState, variant.getOnState());
		assertEquals(variant.getExperience(), test6.getExperience("C").get());
		assertEquals(CollectionsUtils.list(test4.getExperience("C").get(), test5.getExperience("C").get()), variant.getConjointExperiences());
		assertEquals("/path/to/state5/test4.C+test5.C+test6.C", variant.getParameters().get("path"));
		assertFalse(variant.isProper());

	}

}


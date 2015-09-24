package com.variant.web.sample.smvc4;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.jdbc.JdbcUtil;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.web.VariantWeb;

public class EventDataPopulator {

	private static final int COUNT = 10000;
	
	@Test
	public void populate() throws Exception {

		VariantWeb webApi = new VariantWeb();
		
		webApi.bootstrap("/variant/variant-EventDataPopulator.props");

		JdbcUtil.recreateSchema();

		ParserResponse response = webApi.parseSchema(EventDataPopulator.class.getResourceAsStream("/variant/schema.json"));
		if (response.hasMessages()) {
			for (ParserMessage msg: response.getMessages()) {
				System.out.println(msg);
			}
		}
		assertFalse(response.hasMessages());

		Schema schema = webApi.getSchema();
		com.variant.core.schema.Test test = schema.getTest("NewOwnerTest");
		assertNotNull(test);
		State newOwnerView = schema.getState("newOwner");
		assertNotNull(newOwnerView);
		State ownerDetailView = schema.getState("ownerDetail");
		assertNotNull(ownerDetailView);
		int ssnId = 1;
		for (int i = 0; i < COUNT; i++) {
			
			VariantSession ssn = Variant.Factory.getInstance().getSession(String.valueOf(ssnId++));
			
			// Everyone gets to the first page... Emulating new visits.
			VariantStateRequest request = Variant.Factory.getInstance().newStateRequest(ssn, newOwnerView, "");
			Variant.Factory.getInstance().commitStateRequest(request, null);
			
			// Some experiences don't get to the next page, simulating drop-off.
			com.variant.core.schema.Test.Experience exp = request.getTargetedExperience(test);
			boolean skip = false;
			if      (exp.getName().equals("outOfTheBox"))      skip = nextBoolean(0.05);
			else if (exp.getName().equals("tosCheckbox"))      skip = nextBoolean(0.1);
			else if (exp.getName().equals("tos&mailCheckbox")) skip = nextBoolean(0.15);
			
			if (!skip) {
				request = Variant.Factory.getInstance().newStateRequest(ssn, ownerDetailView, request.getTargetingPersister().toString());
				Variant.Factory.getInstance().commitStateRequest(request, null);
			}
			Thread.sleep(rand.nextInt(10));
		}
		
		
		// Hang on a bit so that Junit doesn't kill the writer thread when this method is over.
		Thread.sleep(10000);
	}
	
	/**
	 * Next ran
	 * @param probOfTruth - range from [0,1)
	 * @return
	 */
	private boolean nextBoolean(double probOfTruth) {
		return rand.nextDouble() < probOfTruth;
	}
	private static final Random rand = new Random();
}

package com.variant.web.sample.smvc4;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.VariantViewRequest;
import com.variant.core.jdbc.JdbcUtil;
import com.variant.core.schema.Schema;
import com.variant.core.schema.View;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.web.VariantWeb;

public class EventDataPopulator {

	private static final int SESSION_COUNT = 1000;
	
	@Test
	public void populate() throws Exception {

		VariantWeb.bootstrap("/variant/variant-EventDataPopulator.props");

		JdbcUtil.recreateSchema();

		ParserResponse response = VariantWeb.parseSchema(EventDataPopulator.class.getResourceAsStream("/variant/schema.json"));
		if (response.hasMessages()) {
			for (ParserMessage msg: response.getMessages()) {
				System.out.println(msg);
			}
		}
		assertFalse(response.hasMessages());

		Schema schema = VariantWeb.getSchema();
		com.variant.core.schema.Test test = schema.getTest("NewOwnerTest");
		assertNotNull(test);
		View newOwnerView = schema.getView("newOwner");
		assertNotNull(newOwnerView);
		View ownerDetailView = schema.getView("ownerDetail");
		assertNotNull(ownerDetailView);

		for (int i= 0; i < SESSION_COUNT; i++) {
			
			VariantSession ssn = Variant.Factory.getInstance().getSession(String.valueOf(i));
			
			// Everyone gets to the first page... Emulating new visits.
			VariantViewRequest request = Variant.Factory.getInstance().startViewRequest(ssn, newOwnerView, "");
			Variant.Factory.getInstance().commitViewRequest(request, null);
			
			// Some variant experiences don't get to the next page, simulating drop-off.
			if (nextBoolean(0.95)) {
				request = Variant.Factory.getInstance().startViewRequest(ssn, ownerDetailView, request.getTargetingPersister().toString());
				Variant.Factory.getInstance().commitViewRequest(request, null);
			}
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

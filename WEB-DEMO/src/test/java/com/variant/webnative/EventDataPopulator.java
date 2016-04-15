package com.variant.webnative;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Random;

import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.jdbc.JdbcService;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserResponse;

public class EventDataPopulator {

	private static final int COUNT = 5000;
	private static final Random rand = new Random();

	
	@org.junit.Test
	public void populate() throws Exception {

		Variant coreApi = Variant.Factory.getInstance("/variant/variant-EventDataPopulator.props");

		// For getCoreApi() to work, we need to be in the right package.
		new JdbcService(coreApi).recreateSchema();

		ParserResponse response = coreApi.parseSchema(EventDataPopulator.class.getResourceAsStream("/variant/schema.json"));
		if (response.hasMessages()) {
			for (ParserMessage msg: response.getMessages()) {
				System.out.println(msg);
			}
		}
		assertFalse(response.hasMessages());

		Schema schema = coreApi.getSchema();
		Test test = schema.getTest("NewOwnerTest");
		assertNotNull(test);
		State newOwnerView = schema.getState("newOwner");
		assertNotNull(newOwnerView);
		State ownerDetailView = schema.getState("ownerDetail");
		assertNotNull(ownerDetailView);

		int ssnId = 1;
		int[] countsPerSecond = null;
		long currentSecond = System.currentTimeMillis() / 1000;
		
		for (int i = 0; i < COUNT;) {
		
			long thisSecond = System.currentTimeMillis() / 1000;
			if (countsPerSecond == null || thisSecond > currentSecond) {
				currentSecond = thisSecond;
				//System.out.println(currentSecond);
				countsPerSecond = new int[] {
						5 + new Random(thisSecond % 7 + test.getExperiences().get(0).hashCode()).nextInt(10) + new Random(test.getExperiences().get(0).hashCode()).nextInt(10), 
						5 + new Random(thisSecond % 7 + test.getExperiences().get(1).hashCode()).nextInt(10) + new Random(test.getExperiences().get(1).hashCode()).nextInt(10), 
						5 + new Random(thisSecond % 7 + test.getExperiences().get(2).hashCode()).nextInt(10) + new Random(test.getExperiences().get(2).hashCode()).nextInt(10)};
			}
			//System.out.println(countsPerSecond[0] + "," + countsPerSecond[1] + "," + countsPerSecond[2]);
			VariantSession ssn = coreApi.getSession(String.valueOf(ssnId));
			
			// Everyone gets to the first page... Emulating new visits.
			VariantStateRequest request = coreApi.targetSession(ssn, newOwnerView, "");
			Test.Experience exp = request.getTargetedExperience(test);
			
			// If we've fulfilled quota for this experience and this second, stop untilt he end of current second;
			if (exp.getName().equals("outOfTheBox")      && --countsPerSecond[0] <= 0 ||
			    exp.getName().equals("tosCheckbox")      && --countsPerSecond[1] <= 0 ||
			    exp.getName().equals("tos&mailCheckbox") && --countsPerSecond[2] <= 0) {
			    	continue;
			    }
			
			i++;
			
			coreApi.commitStateRequest(request, String.valueOf(ssnId));
			
			// Some experiences don't get to the next page, simulating drop-off.
			boolean skip = false;
			if      (exp.getName().equals("outOfTheBox"))      skip = nextBoolean(0.90);
			else if (exp.getName().equals("tosCheckbox"))      skip = nextBoolean(0.91);
			else if (exp.getName().equals("tos&mailCheckbox")) skip = nextBoolean(0.93);
			
			if (!skip) {
				request = coreApi.targetSession(ssn, ownerDetailView, request.getTargetingTracker().toString());
				coreApi.commitStateRequest(request, String.valueOf(ssnId));
			}

			ssnId++;
			
			if (ssnId % 100 == 0) System.out.println(ssnId + " sessins completed.");
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
}

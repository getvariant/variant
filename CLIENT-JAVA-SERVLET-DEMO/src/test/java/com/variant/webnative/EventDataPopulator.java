package com.variant.webnative;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Random;

import com.variant.client.VariantClient;
import com.variant.client.VariantSession;
import com.variant.client.VariantStateRequest;
import com.variant.client.impl.VariantClientImpl;
import com.variant.core.VariantCoreSession;
import com.variant.core.VariantCoreStateRequest;
import com.variant.core.xdm.Schema;
import com.variant.core.xdm.State;
import com.variant.core.xdm.Test;
import com.variant.server.ParserMessage;
import com.variant.server.ParserResponse;
import com.variant.server.jdbc.JdbcService;

public class EventDataPopulator {

	private static final int COUNT = 5000;
	private static final Random rand = new Random();

	
	@org.junit.Test
	public void populate() throws Exception {

		VariantClient client = VariantClient.Factory.getInstance("/variant/variant-EventDataPopulator.props");

		// For getCoreApi() to work, we need to be in the right package.
		new JdbcService(((VariantClientImpl)client).getCoreApi()).recreateSchema();

		ParserResponse response = client.parseSchema(EventDataPopulator.class.getResourceAsStream("/variant/schema.json"));
		if (response.hasMessages()) {
			for (ParserMessage msg: response.getMessages()) {
				System.out.println(msg);
			}
		}
		assertFalse(response.hasMessages());

		Schema schema = client.getSchema();
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
			VariantSession ssn = client.getSession(String.valueOf(ssnId));
			
			// Everyone gets to the first page... Emulating new visits.
			VariantStateRequest request = ssn.targetForState(newOwnerView);
			Test.Experience exp = request.getLiveExperience(test);
			
			// If we've fulfilled quota for this experience and this second, stop untilt he end of current second;
			if (exp.getName().equals("outOfTheBox")      && --countsPerSecond[0] <= 0 ||
			    exp.getName().equals("tosCheckbox")      && --countsPerSecond[1] <= 0 ||
			    exp.getName().equals("tos&mailCheckbox") && --countsPerSecond[2] <= 0) {
			    	continue;
			    }
			
			i++;
			
			request.commit(String.valueOf(ssnId));
			
			// Some experiences don't get to the next page, simulating drop-off.
			boolean skip = false;
			if      (exp.getName().equals("outOfTheBox"))      skip = nextBoolean(0.90);
			else if (exp.getName().equals("tosCheckbox"))      skip = nextBoolean(0.91);
			else if (exp.getName().equals("tos&mailCheckbox")) skip = nextBoolean(0.93);
			
			if (!skip) {
				request = ssn.targetForState(ownerDetailView);
				request.commit(String.valueOf(ssnId));
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

package com.variant.client.test.perf;


import static org.junit.Assert.assertNotNull;

import java.util.Random;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.StateRequest;
import com.variant.client.VariantClient;
import com.variant.client.test.ClientBaseTestWithServer;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;

public class EventDataGen extends ClientBaseTestWithServer {
      
   private static final int COUNT = 5000;
	private static final Random rand = new Random();
	
	/**
	 * Simulate massive petclinic traffic in order to generate
	 * event data on the server.
	 * 
	 * @throws Exception
	 */
   @org.junit.Test
	public void genPetclinicData() throws Exception {

///////      startServer("conf-test/petclinic-with-postgres.conf");  <<<< This has changed!!!
		VariantClient client = VariantClient.Factory.getInstance();
/*
		// For getCoreApi() to work, we need to be in the right package.
		new JdbcService(((VariantClientImpl)client).getCoreApi()).recreateSchema();

		ParserResponse response = client.parseSchema(EventDataPopulator.class.getResourceAsStream("/variant/schema.json"));
		if (response.hasMessages()) {
			for (ParserMessage msg: response.getMessages()) {
				System.out.println(msg);
			}
		}
		assertFalse(response.hasMessages());
*/
		Connection conn = client.getConnection("petclinicNoHooks");
		assertNotNull(conn);
		Schema schema = conn.getSchema();
		Test test = schema.getTest("NewOwnerTest");
		assertNotNull(test);
		State newOwnerState = schema.getState("newOwner");
      assertNotNull(newOwnerState);
		State ownerDetailState = schema.getState("ownerDetail");
      assertNotNull(ownerDetailState);

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
			Session ssn = conn.getOrCreateSession(String.valueOf(ssnId));
			assertNotNull(ssn);
			
			// Everyone gets to the first page... Emulating new visits.
			StateRequest request = ssn.targetForState(newOwnerState);
			Test.Experience exp = request.getLiveExperience(test);
			
         request.commit(String.valueOf(ssnId));
         i++;

			// If we've fulfilled quota for all experiences and this second, stop until the end of current second;
			if (exp.getName().equals("outOfTheBox")      && --countsPerSecond[0] <= 0 ||
			    exp.getName().equals("tosCheckbox")      && --countsPerSecond[1] <= 0 ||
			    exp.getName().equals("tos&mailCheckbox") && --countsPerSecond[2] <= 0) {
			    	continue;
			    }
									
			// Some experiences don't get to the next page, simulating drop-off.
			boolean skip = false;
			if      (exp.getName().equals("outOfTheBox"))      skip = nextBoolean(0.90);
			else if (exp.getName().equals("tosCheckbox"))      skip = nextBoolean(0.91);
			else if (exp.getName().equals("tos&mailCheckbox")) skip = nextBoolean(0.93);
			
			if (!skip) {
				request = ssn.targetForState(ownerDetailState);
				request.commit(String.valueOf(ssnId));
			}

			ssnId++;
			
			if (ssnId % 100 == 0) System.out.println(ssnId + " sessins completed.");
		}
		
		System.out.println("Completed. Use /event-data-gen2csv.sh to convert to plottable .CSV file");
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


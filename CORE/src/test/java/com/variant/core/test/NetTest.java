package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.variant.core.VariantCoreSession;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.impl.VariantCore;
import com.variant.core.net.Payload;
import com.variant.core.net.PayloadWriter;
import com.variant.core.net.SessionPayloadReader;
import com.variant.core.schema.ParserResponse;

public class NetTest extends BaseTestCore {

	private VariantCore core = rebootApi();


	/**
	 * Basic Test
	 */
	@Test
	public void basicTest() throws Exception {
		
		ParserResponse response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		final String[] PARAM_VALUES = {"foo", "bar"};
		final String ssnId = "1234567";
		PayloadWriter pw = new PayloadWriter(new CoreSessionImpl(ssnId, core).toJson());
		pw.setProperty(Payload.Property.SVR_REL, PARAM_VALUES[0]);
		pw.setProperty(Payload.Property.SSN_TIMEOUT, PARAM_VALUES[1]);
		
		String payload = pw.getAsJson();
		//System.out.println(payload);

		
		SessionPayloadReader spr = new SessionPayloadReader(core, payload);
		assertEquals(PARAM_VALUES[0], spr.getProperty(Payload.Property.SVR_REL));
		assertEquals(PARAM_VALUES[1], spr.getProperty(Payload.Property.SSN_TIMEOUT));
		VariantCoreSession ssnFromReader = spr.getBody();
		assertEquals(ssnId, ssnFromReader.getId());
	}

}

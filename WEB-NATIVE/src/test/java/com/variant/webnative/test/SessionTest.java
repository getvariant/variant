package com.variant.webnative.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mockito.Mockito;

import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserResponse;

public class SessionTest extends BaseTest {

	/**
	 * No session in cookie.
	 * @throws Exception
	 */
	@org.junit.Test
	public void basicTest() throws Exception {
		
		ParserResponse response = api.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		Schema schema = api.getSchema();
		
		HttpServletRequest httpReq = mockHttpServletRequest();
		HttpServletResponse httpResp = Mockito.mock(HttpServletResponse.class);

		VariantSession ssn1 = api.getSession(httpReq, httpResp);
		assertNotNull(ssn1);
		assertNotNull(ssn1.getId());
		assertNull(ssn1.getStateRequest());		
		assertEquals(0, ssn1.getTraversedStates().size());
		assertEquals(0, ssn1.getTraversedTests().size());
		
		State state1 = schema.getState("state1");
		
		VariantStateRequest varReq = api.dispatchRequest(ssn1, state1, httpReq);
		
		api.commitStateRequest(varReq, httpResp);
	}

}

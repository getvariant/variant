package com.variant.web.test;

import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.impl.VariantCoreImplTestFacade;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.schema.parser.ParserResponse;

public class SessionTest extends BaseTest {

	/**
	 * No Schema.
	 *  
	 * @throws Exception
	 */
	@org.junit.Test
	public void noSchemaTest() throws Exception {

		assertNull(webApi.getSchema());
		final HttpServletRequest httpReq = mockHttpServletRequest("JSESSIONID", null);  // no vssn.
		final HttpServletResponse httpResp = mockHttpServletResponse();

		new ExceptionInterceptor<VariantRuntimeException>() {
			@Override public void toRun() {webApi.getSession(httpReq, httpResp); }
			@Override public Class<VariantRuntimeException> getExceptionClass() { return VariantRuntimeException.class;}
			@Override public void onThrown(VariantRuntimeException e) {
				assertEquals(new VariantRuntimeException(MessageTemplate.RUN_SCHEMA_UNDEFINED).getMessage(), e.getMessage());
			}
		}.assertThrown();	
	}

	/**
	 * Old Schema.
	 *  
	 * @throws Exception
	 */
	@org.junit.Test
	public void oldSchemaTest() throws Exception {

		ParserResponse response = webApi.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());
		
		final HttpServletRequest httpReq = mockHttpServletRequest("JSESSIONID", null);
		final HttpServletResponse httpResp = mockHttpServletResponse();

		VariantSession ssn1 = webApi.getSession(httpReq, httpResp);
		VariantCoreImplTestFacade coreFacade = new VariantCoreImplTestFacade(coreApi);
		coreFacade.getSessionService().saveSession(ssn1, httpReq);     // succeeds
		
		// replace the schema and the session save should fail.
		response = webApi.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNull(response.highestMessageSeverity());
		
		coreFacade.getSessionService().saveSession(ssn1, httpReq);
	}

	/**
	 * No session ID in cookie.
	 *  
	 * @throws Exception
	 */
	//@org.junit.Test
	public void basicTest() throws Exception {
		
		ParserResponse response = webApi.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		Schema schema = webApi.getSchema();
		
		HttpServletRequest httpReq = mockHttpServletRequest("JSESSIONID", null);  // no vssn.
		HttpServletResponse httpResp = mockHttpServletResponse();

		VariantSession ssn1 = webApi.getSession(httpReq, httpResp);
		assertNotNull(ssn1);
		assertNotNull(ssn1.getId());
		assertNull(ssn1.getStateRequest());		
		assertEquals(0, ssn1.getTraversedStates().size());
		assertEquals(0, ssn1.getTraversedTests().size());
		
		VariantSession ssn2 = webApi.getSession(httpReq, httpResp);
		assertNotNull(ssn2);
		
		// getSession() will create a new one
		assertNotEquals(ssn1, ssn2);
				
		State state1 = schema.getState("state1");		
		VariantStateRequest varReq = webApi.dispatchRequest(ssn1, state1, httpReq);
		webApi.commitStateRequest(varReq, httpResp);
	}

	
}

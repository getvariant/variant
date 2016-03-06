package com.variant.webnative.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.Mockito;

import com.variant.core.VariantSession;
import com.variant.core.session.VariantSessionImpl;


public class SessionTest extends BaseTest {

	@Test
	public void noSchemaTest() throws Exception {
		
		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);

		VariantSession ssn = api.getSession(req, resp);
		
/*		
		assertNotNull(ssn);
		assertNull(ssn.getStateRequest());
		assertEquals(0, ssn.getTraversedStates().size());
		assertEquals(0, ssn.getTraversedTests().size());		
		String json = ((VariantSessionImpl)ssn).toJson();
		VariantSessionImpl deserializedSsn = VariantSessionImpl.fromJson(api, json);
		assertEquals("foo", deserializedSsn.getId());
		assertNull(deserializedSsn.getStateRequest());
		assertEquals(0, deserializedSsn.getTraversedStates().size());
		assertEquals(0, deserializedSsn.getTraversedTests().size());
		Thread.sleep(10);
		VariantSession ssn2 = api.getSession("foo");
		assertTrue(ssn2.creationTimestamp() > ssn.creationTimestamp());
*/	}

}

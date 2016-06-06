package com.variant.client;


public class VariantSessionTestFacade {

	public static com.variant.core.VariantSession getCoreSession(VariantSession clientSession) {
		return clientSession.getCoreSession();
	}
}

package com.variant.core.net;

import java.util.Map;

import com.variant.core.VariantSession;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.impl.VariantCore;

public class SessionPayloadReader extends PayloadReader<VariantSession> {

	/**
	 * 
	 * @param core
	 * @param payload
	 */
	public SessionPayloadReader(VariantCore core, String payload) {
		super(core, payload);
	}

	/**
	 * Deserealizer.
	 */
	@Override
	protected VariantSession deserealizeBody(VariantCore core, Map<String, ?> jsonParseTree) {
		return CoreSessionImpl.fromJson(core, jsonParseTree);
	}

}

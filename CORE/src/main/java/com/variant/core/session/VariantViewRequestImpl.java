package com.variant.core.session;

import com.variant.core.VariantSession;
import com.variant.core.VariantViewRequest;

/**
 * View Request implementation.
 *
 * @author Igor
 *
 */
public class VariantViewRequestImpl implements VariantViewRequest {

	/**
	 * Called from a different package so has to be public.
	 */
	@Override
	public VariantSession getSession() {
		
		return new VariantSessionImpl();
	}

}

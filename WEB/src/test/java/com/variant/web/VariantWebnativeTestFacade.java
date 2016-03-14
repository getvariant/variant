package com.variant.web;

import com.variant.core.Variant;
import com.variant.web.VariantWeb;

/**
 * Expose VariantWebnative's package methbods to tests.
 *
 */
public class VariantWebnativeTestFacade {

	/**
	 * Underlying Core API
	 * @param webnativeApi
	 * @return
	 */
	public static Variant getCoreApi(VariantWeb webnativeApi) {
		return webnativeApi.getCoreApi();
	}
}

package com.variant.client;

import com.variant.client.VariantWeb;
import com.variant.core.Variant;

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

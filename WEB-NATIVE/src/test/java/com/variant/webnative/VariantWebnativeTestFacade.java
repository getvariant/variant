package com.variant.webnative;

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
	public static Variant getCoreApi(VariantWebnative webnativeApi) {
		return webnativeApi.getCoreApi();
	}
}

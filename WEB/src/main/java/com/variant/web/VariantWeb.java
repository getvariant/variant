package com.variant.web;

import com.variant.core.Variant;
import com.variant.core.util.VariantArrayUtils;
import com.variant.core.util.VariantStringUtils;
import com.variant.webnative.VariantWebnative;

/**
 * Thin, HTTP based client. Extends the thick native client
 * and gradually re-implements all methods to operate as HTTP
 * clients communicating with the remote Variant server.
 * Once that migration is complete, this project dependency
 * on Webnative (and core) should be dropped.
 * 
 * @author Igor Urisman
 *
 */
public class VariantWeb extends VariantWebnative {

	/**
	 * Obtain an instance of the API. Can be held on to and reused for the life of the JVM.
	 * The instance must be bootstrapped with a call to {@link #bootstrap(String...)} before it
	 * is usable.
	 * 
	 * @param See {@link Variant.Factory#getInstance(String...)}
	 * @returns An instance of {@link VariantWebnative};
	 * @since 0.5
	 */
	public VariantWeb(String...resourceNames) {
		super(VariantArrayUtils.concat(
				resourceNames, 
				"/variant-web." + VariantStringUtils.RESOURCE_POSTFIX + ".props", 
				String.class));
	}

}

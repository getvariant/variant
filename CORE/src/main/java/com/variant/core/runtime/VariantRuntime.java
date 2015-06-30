package com.variant.core.runtime;

import com.variant.core.VariantRuntimeException;
import com.variant.core.config.TestConfig;
import com.variant.core.config.View;
import com.variant.core.error.ErrorTemplate;

/**
 * Entry point into the runtime.
 * 
 * @author Igor.
 *
 */
public class VariantRuntime {

	/**
	 * static singleton.
	 */
	private VariantRuntime() {}
	
	/**
	 * Target this session for all active tests.
	 * 
	 * @param config
	 * @param viewPath
	 */
	public static void targetSession(TestConfig config, String viewPath) {

		// Get 
		View view = null;
		
		for(View v: config.getViews()) {
			if (v.getPath().equalsIgnoreCase(viewPath)) {
				view = v;
				break;
			}
		}
		
		if (view == null) {
			throw new VariantRuntimeException(ErrorTemplate.RUN_NO_VIEW_FOR_PATH, viewPath);
		}

		
	}
}

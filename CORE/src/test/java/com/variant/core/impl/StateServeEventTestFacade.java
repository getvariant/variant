package com.variant.core.impl;

import java.util.Map;

import com.variant.core.VariantViewRequest;

/**
 * Exposes package methods to tests.
 * @author Igor
 *
 */
public class StateServeEventTestFacade extends StateServeEvent {

	/**
	 * 
	 * @param view
	 * @param session
	 * @param status
	 * @param viewResolvedPath
	 */
	public StateServeEventTestFacade(VariantViewRequest request, Map<String,String> params) {

		super((VariantViewRequestImpl)request, params);
	}

}

package com.variant.core.impl;

import java.util.Map;

import com.variant.core.VariantStateRequest;

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
	public StateServeEventTestFacade(VariantStateRequest request, Map<String,String> params) {

		super((VariantStateRequestImpl)request, params);
	}

}

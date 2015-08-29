package com.variant.core.impl;

import java.util.Collection;

import com.variant.core.VariantViewRequest;
import com.variant.core.schema.Test.Experience;

/**
 * Exposes package methods to tests.
 * @author Igor
 *
 */
public class ViewServeEventTestFacade extends ViewServeEvent {

	/**
	 * 
	 * @param view
	 * @param session
	 * @param status
	 * @param viewResolvedPath
	 */
	public ViewServeEventTestFacade(VariantViewRequest request, String viewResolvedPath, Collection<Experience> experiences) {

		super((VariantViewRequestImpl)request, viewResolvedPath, experiences);
	}

}

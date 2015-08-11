package com.variant.core.runtime;

import java.util.Collection;

import com.variant.core.VariantSession;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.View;

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
	public ViewServeEventTestFacade(View view, VariantSession session, String viewResolvedPath, Collection<Experience> experiences) {

		super(view, session, viewResolvedPath, experiences);
	}

}

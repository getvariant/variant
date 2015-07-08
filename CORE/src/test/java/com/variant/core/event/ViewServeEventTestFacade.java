package com.variant.core.event;

import com.variant.core.VariantSession;
import com.variant.core.schema.Test;
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
	public ViewServeEventTestFacade(View view, VariantSession session, Status status, String viewResolvedPath) {

		super(view, session, status, viewResolvedPath);
	}

	@Override
	public void addExperience(Test.Experience experience) {
		super.addExperience(experience);
	}
}

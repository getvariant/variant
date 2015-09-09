package com.variant.core;

import java.util.Collection;

import com.variant.core.impl.ViewServeEvent;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.View;
import com.variant.core.session.TargetingPersister;

/**
 * Encapsulates state relevant to a single view request.
 * 
 * @author Igor
 *
 */
public interface VariantViewRequest {

	/**
	 * This request's Variant session.
	 * @return
	 */
	public VariantSession getSession();
	
	/**
	 * The view for which this request was generated.
	 * @return
	 */
	public View getView();
	
	/**
	 * The actual path as resovled by the runtime.
	 * @return
	 */
	public String resolvedViewPath();
	
	/**
	 * Is this request's resolved path different from the reqeusted path?
	 * In other words, this is equivalent to 
	 * <code>!resolvedPath().equalsIgnoreCase(getView().getPath())</code>
	 * @return
	 */
	public boolean isForwarding();
	
	/**
	 * View serve event associated with this view request
	 * @return
	 */
	public ViewServeEvent getViewServeEvent();

	/**
	 * 
	 * @return
	 */
	public TargetingPersister getTargetingPersister();

	/**
	 * Get all experience targeted in this request.
	 * @return
	 */
	public Collection<Experience> getTargetedExperiences();

	/**
	 * Get the experience targeted in this request for a particular test.
	 * @param test
	 * @return
	 */
	public Experience getTargetedExperience(Test test);

	/**
	 * 
	 * @param status
	 */
	public void setStatus(Status status);
	
	/**
	 * 
	 */
	public static enum Status {
		OK, FAIL
	}
}

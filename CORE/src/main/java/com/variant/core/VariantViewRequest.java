package com.variant.core;

import com.variant.core.impl.ViewServeEvent;
import com.variant.core.schema.View;

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
}

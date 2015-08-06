package com.variant.core;

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
}

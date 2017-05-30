package com.variant.server.api;

import com.variant.core.LifecycleEvent;
import com.variant.core.session.CoreSession;


/**
 * <p>Super-interface for all life cycle event types that post their hooks at run time.
 * 
 * @author Igor.
 * @since 0.5
 *
 */
public interface RuntimeLifecycleEvent extends LifecycleEvent {

	/**
	 * Host code can obtain the current Variant session.
	 * 
	 * @return An object of type {@link com.variant.core.session.CoreSession}.
     * @since 0.5
	 */
	public CoreSession getSession() ;
	
}

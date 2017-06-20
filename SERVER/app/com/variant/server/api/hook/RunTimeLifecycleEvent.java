package com.variant.server.api.hook;

import com.variant.core.LifecycleEvent;
import com.variant.core.schema.Schema;

/**
 * <p>Super-interface for all life cycle event types that post their hooks at experiment run time.
 * 
 * @author Igor Urisman.
 * @since 0.7
 *
 */
public interface RunTimeLifecycleEvent extends LifecycleEvent {
	
	/**
	 * The experiment schema in effect when this event was generated.
     *
	 * @param instanc of type {@link Schema}.
	 * @since 0.7
	 *
	Schema getSchema();
	*/
}

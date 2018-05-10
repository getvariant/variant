package com.variant.client.lce;

import com.variant.client.Connection;

interface ClientLifecycleEvent {
	
	/**
	 * Interface to be implemented by a connection life cycle listener class, whose instance can
	 * be passed to {@link Connection#registerLifecycleListener(LifecycleListener)}.
	 * 
	 * @since 0.8
	 */
	static interface Listener<E extends ClientLifecycleEvent> {
			
		Class<? extends E> getEventClass();
		
	   /**
	    * Variant will call this method whenever the status of the target connection goes from
	    * OPEN to either CLOSED_BY_CLIENT, or CLOSED_BY_SERVER. If this code throws an exception,
	    * it will be reported in the application log only. 
	    * 
	    * @since 0.8
	    * @param connection: The target connection object.
	    */
	   void post(E connectionLifecycleEvent);
	}

}

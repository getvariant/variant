package com.variant.client.lifecycle;

import com.variant.client.Session;

public interface SessionLifecycleEvent extends LifecycleEvent {

	Session getSession();
	
}

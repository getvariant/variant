package com.variant.client.lce;

import com.variant.client.Session;

public interface SessionLifecycleEvent extends ClientLifecycleEvent {

	Session getSession();
	
	public static interface Expired extends SessionLifecycleEvent {}
}

package com.variant.client.lce;

import com.variant.client.Session;

public interface SessionLifecycleEvent extends LifecycleEvent {

	Session getSession();
	
	public static interface Expired extends SessionLifecycleEvent {}
}

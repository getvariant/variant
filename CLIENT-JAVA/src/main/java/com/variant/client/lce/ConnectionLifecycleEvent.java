package com.variant.client.lce;

import com.variant.client.Connection;

public interface ConnectionLifecycleEvent extends ClientLifecycleEvent {

	Connection getConnection();
	
	public static interface Closed extends ConnectionLifecycleEvent {}
	
	public static interface Listener extends ClientLifecycleEvent.Listener<ConnectionLifecycleEvent> {}

}

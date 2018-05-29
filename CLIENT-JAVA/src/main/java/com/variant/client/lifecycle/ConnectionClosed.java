package com.variant.client.lifecycle;

import com.variant.client.Connection;

/**
 * Connection closed life-cycle event. Raised when Variant client detects that
 * a connection is closed as the result of either {@link Connection#close()} or
 * a server restart. 
 * 
 * @since 0.9
 */
public interface ConnectionClosed extends ConnectionAwareLifecycleEvent {}

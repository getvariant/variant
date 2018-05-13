package com.variant.client.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.VariantClient;
import com.variant.client.lce.ConnectionClosed;
import com.variant.client.lce.ConnectionLifecycleEvent;
import com.variant.client.lce.LifecycleEvent;
import com.variant.client.lce.SessionExpired;
import com.variant.client.lce.SessionLifecycleEvent;
import com.variant.client.lce.UserHook;

public class LifecycleEventService {

	final private static Logger LOG = LoggerFactory.getLogger(LifecycleEventService.class);
	
	// Static thread pool is shared by all connections and all sessions
	// in a particular variant client.
	static final private ExecutorService threadPool = Executors.newFixedThreadPool(3);
	
	final private VariantClient client;
	final private AtomicInteger queueSize = new AtomicInteger();

	/**
	 * Post a single hook.
	 * Creates a callable and submits it to the concurrent executor.
	 * @param hook
	 * @param event
	 */
	@SuppressWarnings("unchecked")
	private void postLifecycleHook(UserHook<? extends LifecycleEvent> hook, LifecycleEvent event) {
		
		threadPool.submit(
					new Callable<Void>() {

						@Override
						public Void call() {
							
							try {
								((UserHook<LifecycleEvent>)hook).post(event);								
							}
							catch (Throwable t) {
								asyncExceptions.add(t);
								LOG.error(
										ClientUserError.LIFECYCLE_LISTENER_EXCEPTION
											.asMessage(t.getMessage(), this.getClass().getName()), t);
							}
							finally {
								queueSize.decrementAndGet();
							}
							
							return null;
						}
					}
				);
		queueSize.incrementAndGet();
	}
	
	/**
	 * 
	 */
	LifecycleEventService(VariantClient client) {
		this.client = client;
	}

	/**
	 * Raise a connection scoped event
	 */
	void raiseEvent(Class<? extends ConnectionLifecycleEvent> eventClass, ConnectionImpl conn) {
		
		if (conn.getClient() != client) 
			throw new ClientException.Internal("Bad client");
	

		for (UserHook<? extends LifecycleEvent> hook: conn.lifecycleHooks) {

			if (!eventClass.isAssignableFrom(hook.getLifecycleEventClass())) continue;

				LifecycleEvent event = new ConnectionClosed() {		
				@Override public Connection getConnection() {
					return conn;
				}
			};
			postLifecycleHook(hook, event);
		}
	}

	/**
	 * Raise a session scoped event
	 */
	void raiseEvent(Class<? extends SessionLifecycleEvent> eventClass, SessionImpl session) {
					
		if (session.getConnection().getClient() != client) 
			throw new ClientException.Internal("Bad client");

		// 1. Connection level 
		for (UserHook<? extends LifecycleEvent> hook: ((ConnectionImpl)session.getConnection()).lifecycleHooks) {
			
			if (!eventClass.isAssignableFrom(hook.getLifecycleEventClass())) continue;

				LifecycleEvent event = new SessionExpired() {		
				@Override public Session getSession() {
					return session;
				}
			};
			postLifecycleHook(hook, event);
		}
		
		// 2. Session level 

	/*
		for (UserHook<? extends LifecycleEvent> hook: session.lifecycleHooks) {
			
			if (!eventClass.isAssignableFrom(hook.getLifecycleEventClass())) continue;

				LifecycleEvent event = new SessionExpired() {		
				@Override public Session getSession() {
					return session;
				}
			};
			postLifecycleHook(hook, event);
		}
	*/
	}

	// Tests can block until all submitted async callables are done.
	public void awaitAll() throws Exception {
		long timeout = 2000;  
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() < start + timeout) {
			if (queueSize.get() == 0) break;
			Thread.sleep(200);
		}
	}
	
	// For the benefit of tests, we maintain most recent exceptions,
	// thrown asynchronously by life-cycle hooks.
	final public LinkedBlockingQueue<Throwable> asyncExceptions = 
			new LinkedBlockingQueue<Throwable>(10);

}

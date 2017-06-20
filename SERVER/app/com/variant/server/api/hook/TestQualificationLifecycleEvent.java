package com.variant.server.api.hook;


/**
 * <p>Run time hook. Posts its listeners whenever the session targeter is about to target a user session. 
 * If no listeners are supplied, Variant server treats all user sessions as qualifying. A custom listener
 * may explicitly (dis)qualify the session by calling the setQualified() method. The application 
 * programmer can also control targeting stability for those users who are disqualified. But default, 
 * disqualification does not affect experiment scoped targeting stability . However, application developer 
 * may direct the server to remove the entry for this test from this user's targeting tracker by calling 
 * {@link #setQualified(boolean)}.
 * 
 * @author Igor Urisman.
 * @since 0.5
 *
 */
public interface TestQualificationLifecycleEvent extends StateRequestAwareLifecycleEvent, TestScopedLifecycleEvent {}

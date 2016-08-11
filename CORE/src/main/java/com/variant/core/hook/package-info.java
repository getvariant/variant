/**
 * Variant user hooks.
 * User hooks provide a consistent way of extending the functionality of Variant server 
 * with custom semantics. They are predefined points in the execution path, to which the 
 * application developer may attach a callback method to be invoked by the server whenever 
 * that point is reached. To attach a custom callback method to a user hook, application 
 * programmer must register a hook listener by passing it to <code>VariantClient.addHookListener(HookListener)</code>.
 * 
 * @since 0.5
 */
package com.variant.core.hook;

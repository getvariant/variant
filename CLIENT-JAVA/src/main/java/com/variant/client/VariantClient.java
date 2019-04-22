package com.variant.client;


import static com.variant.client.impl.ConfigKeys.*;

import java.util.HashMap;

import com.variant.client.impl.ClientUserError;
import com.variant.client.impl.VariantClientImpl;
import com.variant.core.util.immutable.ImmutableMap;

/**
 * Variant Java Client object.
 * <p>
 * Provides connectivity to Variant Experience Server from any JVM process. 
 * Makes no assumptions about the host application other than 
 * it is running on a JVM. 
 * 
 * @since 0.5
 */
public interface VariantClient {
		
	/**
	 * Connect to a variation schema on a Variant server by its URI.
	 * Variant schema URI has the following format:
	 * [variant:]//netloc[:port]/schema
	 * 
	 * @param uri The Variant URI to the schema.
	 *        
	 * @return An instance of the {@link Connection} type.
	 * 
	 * @throws UnknownSchemaException if given schema does not exist on the server.
	 * @since 0.7
	 */
	Connection connectTo(String uri);
	
	/**
	 * Variant client builder helper class. Implements the builder design pattern.
	 * Subclasses of the containing {@link VariantClient} class can simply extend
	 * the builder as is, though most likely override the {@link #build()} method.
	 * 
	 * @since 0.10
	 */
	public static class Builder {
		
		@SuppressWarnings("serial")
		private HashMap<String, Object> props = new HashMap<String, Object>() {{
			put(TARGETING_STABILITY_DAYS, 0);
		}};

		/**
		 * Instantiate a Variant clicent builder. 
		 */
		public Builder() {}
		
		/**
		 * Set targeting stability days. Must be 0 or greater.
		 * @param days
		 * @return this object.
		 * @since 0.10
		 */
		public Builder withTargetingStabilityDays(int days) {
			props.put(TARGETING_STABILITY_DAYS, days);
			return this;
		}
		
		/**
		 * Set targeting tracker class.
		 * @param Class implementing the {@link TargetingTracker} interface.
		 * @return this object.
		 * @since 0.10
		 */
		public Builder withTargetingTrackerClass(Class<? extends TargetingTracker> klass) {

			if (!TargetingTracker.class.isAssignableFrom(klass))
				throw new VariantException(ClientUserError.TARGETING_TRACKER_NO_INTERFACE, klass.getName());
		
			props.put(TARGETING_TRACKER_CLASS, klass);
			return this;
		}
		
		/**
		 * Set session ID tracker class.
		 * @param Class implementing the {@link SessionIdTracker} interface.
		 * @return this object.
		 * @since 0.10
		 */
		public Builder withSessionIdTrackerClass(Class<? extends SessionIdTracker> klass) {

			if (!SessionIdTracker.class.isAssignableFrom(klass))
				throw new VariantException(ClientUserError.SESSION_ID_TRACKER_NO_INTERFACE, klass.getName());

			props.put(SESSION_ID_TRACKER_CLASS, klass);
			return this;
		}
		

		/**
		 * Instantiate a new instance of {@link VariantClient} 
		 * Host application should hold on to and reuse the object returned by this method whenever possible.
		 * In most cases, one {@link VariantClient} instance per application should be sufficient.
		 * 
		 * @return Instance of the {@link VariantClient} type.
		 * @since 0.10
		 */
		public VariantClient build() {
			
			if (props.get(TARGETING_TRACKER_CLASS) == null)
				throw new VariantException(ClientUserError.TARGETING_TRACKER_MISSING);
			
			if (props.get(SESSION_ID_TRACKER_CLASS) == null)
				throw new VariantException(ClientUserError.SESSION_ID_TRACKER_MISSING);

			return new VariantClientImpl(new ImmutableMap<String, Object>(props));
		}
	}
}

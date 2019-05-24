package com.variant.client;


import static com.variant.client.impl.ConfigKeys.SESSION_ID_TRACKER_CLASS;
import static com.variant.client.impl.ConfigKeys.TARGETING_STABILITY_DAYS;
import static com.variant.client.impl.ConfigKeys.TARGETING_TRACKER_CLASS;

import java.util.HashMap;

import com.variant.client.impl.ClientUserError;
import com.variant.client.impl.VariantClientImpl;
import com.variant.core.util.immutable.ImmutableMap;

/**
 * Variant Java client.
 * 
 * <p>Provides connectivity to Variant AIM Server from any JVM. 
 * Makes no assumptions about the host application other than 
 * it is running on a JVM. Instantiated using the Builder pattern as follows:
 * 
 * <pre>
 * VariantClient client = 
 *         new VariantClient.Builder()
 *         .withSessionIdTrackerClass(MySessionIdTracker.class)
 *         .build();
 * </pre>
 * 
 * 
 * @since 0.5
 */
public interface VariantClient {
		
	/**
	 * Connect to a variation schema on a Variant server by its URI.
	 * Variant schema URI has the following format: <code>[variant:]//netloc[:port]/schema</code>
	 * If omitted, port 5377 is assumed. For example, to connect to the petclinic demo schema,
	 * <pre>
	 *   Connection conn = client.connectTo("myserver.com:5377/petclinic")
	 * </pre>
	 * 
	 * @param uri The URI to the variation schema.
	 *        
	 * @return An object of type {@link Connection}. Cannot be null.
	 * 
	 * @throws UnknownSchemaException if given schema does not exist on the server.
	 * @since 0.7
	 */
	Connection connectTo(String uri);
	
	/**
	 * Variant client builder helper class. Implements the builder design pattern.
	 * 
	 * @since 0.10
	 */
	public static class Builder {
		
		@SuppressWarnings("serial")
		private HashMap<String, Object> props = new HashMap<String, Object>() {{
			put(TARGETING_STABILITY_DAYS, 0);
		}};

		/**
		 * Public constructor. 
		 * @since 0.10
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
		 * Instantiate a new instance of {@link VariantClient}.
		 * Host application should hold on to and reuse the object returned by this method whenever possible.
		 * In most cases, one {@link VariantClient} instance per application is sufficient.
		 * 
		 * @return Instance of the {@link VariantClient} type. Cannot be null.
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

package com.variant.client.servlet;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DateUtils;

import com.variant.client.VariantClient;
import com.variant.client.VariantInitParams;
import com.variant.client.VariantTargetingTracker;
import com.variant.client.VariantTargetingTracker.Entry;
import com.variant.client.servlet.util.VariantCookie;
import com.variant.client.session.TargetingTrackerString;
import com.variant.core.VariantProperties;
import com.variant.core.VariantStateRequest;

/**
 * Concrete implementation of the Variant targeting tracker based on HTTP cookie. 
 * Targeting information is saved in a persistent
 * cookie between Variant sessions. As such, provides weak experience stability: a returning user
 * will see familiar experiences, but only so long as he is using the same browser.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public class TargetingTrackerHttpCookie extends TargetingTrackerString implements VariantTargetingTracker {
	
	/**
	 * The cookie which tracks the experiences
	 */
	private static class TargetingCookie extends VariantCookie {

		private TargetingCookie() {
			super(COOKIE_NAME);
		}
		
		private TargetingCookie(HttpServletRequest request) {
			super(COOKIE_NAME, request);
		}

		/**
		 * We want this cookie to never expire, so set a year in the future.
		 */
		@Override
		public int getMaxAge() {
			return (int) DateUtils.MILLIS_PER_DAY / 1000 * 365;
		}
	}

	private TargetingCookie cookie;
	private VariantClient client;	
	//private Logger LOG = LoggerFactory.getLogger(TargetingTrackerHttpCookie.class);

	/**
	 * Superclass needs properties but doesn't have them.
	 */
	@Override
	protected VariantProperties getProperties() {
		return client.getProperties();
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	public static final String COOKIE_NAME = "variant-target";

	/**
	 * No-argument constructor must be provided by contract. Called by Variant client within the scope
	 * of the {@link VariantClient#getSession(Object...)} call.
	 */
	public TargetingTrackerHttpCookie() {}

	/**
	 * <p>Called by Variant to initialize a new instance, within the scope of the 
	 * {@link VariantClient#getSession(Object...)} method. Use this to inject state from configuration.
	 * 
	 * @param initParams An instance of type {@link VariantInitParams}, containing parsed JSON object, 
	 *                   specified by the <code>targeting.tracker.class.init</code> application property.
	 * @param userData   This implementation expects userData to be a one-element array whose single element
	 *                   is the current {@link HttpServletRequest}.
	 *
	 * @since 0.6
	 */
	@Override
	public void init(VariantInitParams initParams, Object...userData){
		client = initParams.getVariantClient();
		HttpServletRequest request =  (HttpServletRequest) userData[0];
		cookie = new TargetingCookie(request);
	}		

	/**
	 * <p>Retrieve the current value of the session ID from this tracker. 
	 * This value may have been set by {@link #init(VariantInitParams, Object...)} or by {@link #set(Collection)}.
	 * 
	 * @return Collection of zero or more of objects of type {@link Entry} each corresponding to
	 *         a test experience currently tracked by this object.
	 * @since 0.6
	 */
	@Override
	public Collection<Entry> get() {
		String input = cookie.getValue();
		// If the targeting cookie existed and returned a value, the superclass will parse it.
		return input == null ? null : fromString(cookie.getValue(), client.getSchema());
	}

	/**
	 * Set the value of all currently tracked test experiences.
	 * 
	 * @param entries Collection of objects of type {@link Entry}. The caller must guarantee 
	 *                consistency of this collection, i.e. that all entries are pairwise independent,
	 *                which is to say that there be no two entries which refer to the same test.
	 * 
	 * @since 0.6
	 */
	@Override
	public void set(Collection<Entry> entries) {
		cookie.setValue(toString(entries));
	}

	/**
	 * <p>Called by Variant to save the currently tracked experiences to the underlying persistence mechanism. 
	 * Variant client calls this method within the scope of the {@link VariantStateRequest#commit(Object...)} method.
	 * 
	 * @param userData This implementation expects userData to be a one-element array whose single element
	 *                   is the current {@link HttpServletResponse}.
	 *                 
	 * @since 0.6
	 */
	@Override
	public void save(Object...userData) {
		HttpServletResponse response = (HttpServletResponse) userData[0];
		cookie.send(response);
	}
}

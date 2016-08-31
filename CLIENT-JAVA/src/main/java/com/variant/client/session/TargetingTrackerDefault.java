package com.variant.client.session;

import java.util.Collection;

import com.variant.client.VariantClientPropertyKeys;
import com.variant.client.VariantInitParams;
import com.variant.client.VariantTargetingTracker;

/**
 * Unusable default implementation of {@link VariantTargetingTracker}. 
 * Must be overridden.
 * 
 * @author Igor
 *
 */
public class TargetingTrackerDefault implements VariantTargetingTracker {
	
	private final static String MESSAGE = 
			"Supply a functional implementation in applicaiton property " + 
					VariantClientPropertyKeys.TARGETING_TRACKER_CLASS_NAME.propertyName();

	@Override
	public void init(VariantInitParams initParams, Object...userData) {}		

	/**
	 * User data is expected as an <code>HttpServletResponse</code> object.
	 */
	@Override
	public void save(Object...userData) {
		throw new UnsupportedOperationException(MESSAGE);
	}

	@Override
	public Collection<Entry> get() {
		throw new UnsupportedOperationException(MESSAGE);
	}

	@Override
	public void set(Collection<Entry> entries) {
		throw new UnsupportedOperationException(MESSAGE);
	}


}
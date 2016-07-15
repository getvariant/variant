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
			"Supply a better implementation in applicaiton property " + 
					VariantClientPropertyKeys.TARGETING_TRACKER_CLASS_NAME.propertyName();

	@Override
	public void initialized(VariantInitParams initParams, Object... userData) throws Exception {}		

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

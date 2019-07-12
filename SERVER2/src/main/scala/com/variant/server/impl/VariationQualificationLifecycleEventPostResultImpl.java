package com.variant.server.impl;

import com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent;

public class VariationQualificationLifecycleEventPostResultImpl implements VariationQualificationLifecycleEvent.PostResult {

	private boolean qualified = false;
	private boolean removeFromTT = false;
	
	public VariationQualificationLifecycleEventPostResultImpl(VariationQualificationLifecycleEvent event) {
		// Nothing so far. Taking events as the arg for consistency with sister-types.
	}
	
	@Override
	public void setQualified(boolean qualified) {
		this.qualified = qualified;
	}
/*
	@Override
	public void setRemoveFromTargetingTracker(boolean removeFromTT) {
		this.removeFromTT = removeFromTT;
	}
*/
	public boolean isQualified() { 
		return qualified; 
	}
	
	public boolean isRemoveFromTT() { 
		return removeFromTT; 
	}

}


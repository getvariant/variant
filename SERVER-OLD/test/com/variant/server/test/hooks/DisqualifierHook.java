package com.variant.server.test.hooks;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.server.api.lifecycle.LifecycleEvent;
import com.variant.server.api.lifecycle.LifecycleHook;
import com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent;

public class DisqualifierHook implements LifecycleHook<VariationQualificationLifecycleEvent>{

	private final static Logger LOG = LoggerFactory.getLogger(DisqualifierHook.class);
	
	@Override
    public Class<VariationQualificationLifecycleEvent> getLifecycleEventClass() {
		return VariationQualificationLifecycleEvent.class;
    }
   
	@Override
	public Optional<LifecycleEvent.PostResult> post(VariationQualificationLifecycleEvent event) {

		VariationQualificationLifecycleEvent.PostResult result = null;
		String attrValue = event.getSession().getAttributes().get("disqual");
		if (attrValue != null && Boolean.parseBoolean(attrValue)) {
			result = event.mkPostResult();
			result.setQualified(false);
			LOG.info(String.format("Disqulified session ID [%s] form variation [%s]", event.getSession().getId(), event.getVariation().getName()));
		}
		return Optional.of(result);
	}
	
}

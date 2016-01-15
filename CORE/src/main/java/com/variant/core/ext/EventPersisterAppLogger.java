package com.variant.core.ext;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.event.EventPersister;
import com.variant.core.event.VariantEvent;
import com.variant.core.event.VariantEventVariant;

public class EventPersisterAppLogger implements EventPersister {
	
	private static final Logger LOG = LoggerFactory.getLogger(EventPersisterAppLogger.class);

	@Override
	public void initialized() {
		LOG.info("Initialized");
	}

	@Override
	public void persist(Collection<VariantEvent> events) throws Exception {

		for (VariantEvent event: events) {
			StringBuilder msg = new StringBuilder();
			msg.append("EVENT:{")
			.append("session_id:'").append(event.getSession().getId()).append("', ")
			.append("created_on:'").append(event.getCreateDate()).append("', ")
			.append("event_name:'").append(event.getEventName()).append("', ")
			.append("event_value:'").append(event.getEventValue()).append("'")
			.append("}");

			LOG.info(msg.toString());
		}
								
		for (VariantEvent event: events) {
			for (VariantEventVariant ee: event.getEventVariants()) {
				
				StringBuilder msg = new StringBuilder();
				msg.append("EVENT_VARIANTS:{")
				.append("event_name:'").append(ee.getEvent().getEventName()).append("', ")
				.append("test_name:'").append(ee.getExperience().getTest().getName()).append("', ")
				.append("experience_name:'").append(ee.getExperience().getName()).append("', ")
				.append("is_experience_control:").append(ee.isExperienceControl()).append("', ")
				.append("is_state_nonvariant:").append(ee.isStateNonvariant())
				.append("}");

				LOG.info(msg.toString());
			}
		}
		
		for (VariantEvent event: events) {
			for (String key: event.getParameterKeys()) {

				StringBuilder msg = new StringBuilder();
				msg.append("EVENT_PARAMS:{")
				.append("event_name:'").append(event.getEventName()).append("', ")
				.append("key:'").append(key).append("', ")
				.append("value:'").append(event.getParameter(key)).append("'")
				.append("}");

				LOG.info(msg.toString());
			}
		}		
	}
}

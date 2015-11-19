package com.variant.core.ext;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.VariantEventExperience;
import com.variant.core.event.EventPersister;
import com.variant.core.event.VariantEventSupport;

public class EventPersisterAppLogger implements EventPersister {
	
	private static final Logger LOG = LoggerFactory.getLogger(EventPersisterAppLogger.class);

	@Override
	public void initialized() {
		LOG.info("Initialized");
	}

	@Override
	public void persist(Collection<VariantEventSupport> events) throws Exception {

		for (VariantEventSupport event: events) {
			StringBuilder msg = new StringBuilder();
			msg.append("EVENT:{")
			.append("session_id:'").append(event.getSession().getId()).append("', ")
			.append("created_on:'").append(event.getCreateDate()).append("', ")
			.append("event_name:'").append(event.getEventName()).append("', ")
			.append("event_value:'").append(event.getEventValue()).append("'")
			.append("}");

			LOG.info(msg.toString());
		}
								
		for (VariantEventSupport event: events) {
			for (VariantEventExperience ee: event.getEventExperiences()) {
				
				StringBuilder msg = new StringBuilder();
				msg.append("EVENT_EXPERIENCES:{")
				.append("event_name:'").append(ee.getEvent().getEventName()).append("', ")
				.append("test_name:'").append(ee.getExperience().getTest().getName()).append("', ")
				.append("experience_name:'").append(ee.getExperience().getName()).append("', ")
				.append("is_control:").append(ee.getExperience().isControl())
				.append("}");

				LOG.info(msg.toString());
			}
		}
		
		for (VariantEventSupport event: events) {
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
		
		
		for (VariantEventSupport event: events) {
			for (VariantEventExperience ee: event.getEventExperiences()) {
				for (String key: ee.getParameterKeys()) {

					StringBuilder msg = new StringBuilder();
					msg.append("EVENT_EXPERIENCE_PARAMS:{")
					.append("event_name:'").append(ee.getEvent().getEventName()).append("', ")
					.append("test_name:'").append(ee.getExperience().getTest().getName()).append("', ")
					.append("experience_name:'").append(ee.getExperience().getName()).append("', ")
					.append("key:'").append(key).append("', ")
					.append("value:'").append(ee.getParameter(key)).append("'")
					.append("}");
	
					LOG.info(msg.toString());
				}
			}
		}
	}

}

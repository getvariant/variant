package com.variant.core.ext;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.InitializationParams;
import com.variant.core.event.EventPersister;
import com.variant.core.event.PersistableVariantEvent;
import com.variant.core.schema.Test.Experience;

public class EventPersisterAppLogger implements EventPersister {
	
	private static final Logger LOG = LoggerFactory.getLogger(EventPersisterAppLogger.class);

	@Override
	public void initialized(InitializationParams initParams) {
		LOG.info("Initialized");
	}

	@Override
	public void persist(Collection<PersistableVariantEvent> events)
			throws Exception {

		for (PersistableVariantEvent event: events) {
			StringBuilder msg = new StringBuilder();
			msg.append("EVENT:{")
			.append("session_id:'").append(event.getSession().getId()).append("', ")
			.append("created_on:'").append(event.getCreateDate()).append("', ")
			.append("event_name:'").append(event.getEventName()).append("', ")
			.append("event_value:'").append(event.getEventValue()).append("'")
			.append("}");

			LOG.info(msg.toString());
		}
								
		for (PersistableVariantEvent event: events) {
			for (Experience e: event.getActiveExperiences()) {
				StringBuilder msg = new StringBuilder();
				msg.append("EVENT_VARIANTS:{")
				.append("event_name:'").append(event.getEventName()).append("', ")
				.append("test_name:'").append(e.getTest().getName()).append("', ")
				.append("experience_name:'").append(e.getName()).append("', ")
				.append("is_experience_control:").append(e.isControl()).append("', ")
				.append("}");

				LOG.info(msg.toString());
			}
		}
		
		for (PersistableVariantEvent event: events) {
			for (Map.Entry<String, Object> param: event.getParameterMap().entrySet()) {

				StringBuilder msg = new StringBuilder();
				msg.append("EVENT_PARAMS:{")
				.append("event_name:'").append(event.getEventName()).append("', ")
				.append("key:'").append(param.getKey()).append("', ")
				.append("value:'").append(param.getValue()).append("'")
				.append("}");

				LOG.info(msg.toString());
			}
		}		
	}

}

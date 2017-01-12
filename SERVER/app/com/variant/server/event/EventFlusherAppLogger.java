package com.variant.server.event;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.ConfigObject;
import com.variant.core.schema.Test.Experience;
import com.variant.server.EventFlusher;
import com.variant.server.FlushableEvent;

/**
 * <p>An environment independent implementation of {@link EventFlusher}, which appends
 * events to the application logger. Can't really be used in production, but is good
 * enough for the demo application because it does not require any external support.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public class EventFlusherAppLogger implements EventFlusher {
	
	private static final Logger LOG = LoggerFactory.getLogger(EventFlusherAppLogger.class);

	@Override
	public void init(ConfigObject config) {
		LOG.debug("Bootstrapped");
	}

	@Override
	public void flush(Collection<FlushableEvent> events)
			throws Exception {

		for (FlushableEvent event: events) {
			StringBuilder msg = new StringBuilder();
			msg.append("EVENT:{")
			.append("session_id:'").append(event.getSession().getId()).append("', ")
			.append("created_on:'").append(event.getCreateDate()).append("', ")
			.append("event_name:'").append(event.getName()).append("', ")
			.append("event_value:'").append(event.getValue()).append("'")
			.append("}");

			LOG.info(msg.toString());
		}
								
		for (FlushableEvent event: events) {
			for (Experience e: event.getLiveExperiences()) {
				StringBuilder msg = new StringBuilder();
				msg.append("EVENT_EXPERIENCES:{")
				.append("event_name:'").append(event.getName()).append("', ")
				.append("test_name:'").append(e.getTest().getName()).append("', ")
				.append("experience_name:'").append(e.getName()).append("', ")
				.append("is_control:").append(e.isControl()).append("', ")
				.append("}");

				LOG.info(msg.toString());
			}
		}
		
		for (FlushableEvent event: events) {
			for (Map.Entry<String, String> param: event.getParameterMap().entrySet()) {

				StringBuilder msg = new StringBuilder();
				msg.append("EVENT_PARAMS:{")
				.append("event_name:'").append(event.getName()).append("', ")
				.append("key:'").append(param.getKey()).append("', ")
				.append("value:'").append(param.getValue()).append("'")
				.append("}");

				LOG.info(msg.toString());
			}
		}		
	}

}

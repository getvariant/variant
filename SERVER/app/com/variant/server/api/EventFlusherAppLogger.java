package com.variant.server.api;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.variant.core.schema.Test.Experience;

/**
 * An implementation of {@link EventFlusher}, which appends Variant events
 * to the application logger. This is the default, out of the box event flusher,
 * which is completely independent of the operational environment. Probably not for production use, 
 * but is good enough for the demo application.
 * <p>
 * Configuration. You may use the {@code variant.event.flusher.class.init} configuration property
 * to pass configuration details to this object.
 * <ul>
 * <li><code>level</code> - specifies the logging level to be used. Defaults to 'INFO'.<br/>
 * </ul>
 * Example:<br/>
 * <code>variant.event.flusher.class.init = {init="INFO"}</code>
 * 
 * 
 * @since 0.5
 */
public class EventFlusherAppLogger implements EventFlusher {
	
	private static final Logger LOG = LoggerFactory.getLogger(EventFlusherAppLogger.class);

	private String level = null;

	public EventFlusherAppLogger(Config config) {
		level = config.getString("level");
		
		if (level == null) {
			level = "INFO";
			LOG.info("No level specified. Will use INFO.");
		}

	}

	@Override
	public void flush(Collection<FlushableEvent> events)
			throws Exception {

		for (FlushableEvent event: events) {
			StringBuilder msg = new StringBuilder();
			msg.append("{")
         .append("event_name:'").append(event.getName()).append("', ")
			.append("created_on:'").append(event.getCreateDate().getTime()).append("', ")
			.append("event_value:'").append(event.getValue()).append("', ")
         .append("session_id:'").append(event.getSession().getId()).append("'");

			if (!event.getLiveExperiences().isEmpty()) {
			   msg.append(", event_experiences:[");
			   boolean first = true;
   			for (Experience e: event.getLiveExperiences()) {
   			   if (first) first = false;
   			   else msg.append(", ");
   			   msg.append("{")
               .append("test_name:'").append(e.getTest().getName()).append("', ")
               .append("experience_name:'").append(e.getName()).append("', ")
               .append("is_control:").append(e.isControl())
               .append("}");
            }
   			msg.append("]");
			}			
			
	      if (!event.getParameterMap().isEmpty()) {
            msg.append(", event_params:[");
            boolean first = true;
	         for (Map.Entry<String, String> param: event.getParameterMap().entrySet()) {
	            if (first) first = false;
	            else msg.append(", ");
	            msg.append("{")
	            .append("key:'").append(param.getKey()).append("', ")
	            .append("value:'").append(param.getValue()).append("'")
	            .append("}");
	         }
	         msg.append("]");
	      }     

	      msg.append("}");

	      switch (level.toUpperCase()) {
	      case "TRACE": LOG.trace(msg.toString()); break;
	      case "DEBUG": LOG.debug(msg.toString()); break;
	      case "INFO": LOG.info(msg.toString()); break;
	      case "ERROR": LOG.error(msg.toString()); break;
	      }
		}
								
	}

}

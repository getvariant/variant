package com.variant.web;


import com.variant.core.flashpoint.FlashpointListener;
import com.variant.core.flashpoint.StateParsedFlashpoint;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.Severity;

/**
 * An implementation of {@link FlashpointListener} listening to {@link StateParsedFlashpoint}.
 * Performs additional parse checks, not implemented by the core API and specific to the 
 * Servlet environment.
 * 
 * @author Igor Urisman
 * @see FlashpointListener
 * @since 0.5
 *
 */
public class StateParsedFlashpointListenerImpl implements FlashpointListener<StateParsedFlashpoint> {

	/**
	 * Target Flashpoint type, available at run time.
	 */
	@Override
	public Class<StateParsedFlashpoint> getFlashpointClass() {
		return StateParsedFlashpoint.class;
	}

	/**
	 * Ensure that the <code>path</code> state parameter starts with a forward slash.
	 */
	@Override
	public void post(StateParsedFlashpoint flashpoint) {
		State state = flashpoint.getState();
		String path = state.getParameterMap().get("path");
		if (!path.startsWith("/")) {
			flashpoint.getParserResponse().addMessage(
					Severity.ERROR, 
					"Path property [" + path + "] must start with a '/' in State [" + state.getName() + "]");
		}
	}	

}

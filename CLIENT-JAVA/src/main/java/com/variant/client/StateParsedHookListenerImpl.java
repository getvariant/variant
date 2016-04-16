package com.variant.client;


import com.variant.core.hook.HookListener;
import com.variant.core.hook.StateParsedHook;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.Severity;

/**
 * An implementation of {@link HookListener} listening to {@link StateParsedHook}.
 * Performs additional parse checks, not implemented by the core API and specific to the 
 * Servlet environment.
 * 
 * @author Igor Urisman
 * @see HookListener
 * @since 0.5
 *
 */
public class StateParsedHookListenerImpl implements HookListener<StateParsedHook> {

	/**
	 * Target Hook type, available at run time.
	 */
	@Override
	public Class<StateParsedHook> getHookClass() {
		return StateParsedHook.class;
	}

	/**
	 * Ensure that the <code>path</code> state parameter starts with a forward slash.
	 */
	@Override
	public void post(StateParsedHook hook) {
		State state = hook.getState();
		String path = state.getParameterMap().get("path");
		if (!path.startsWith("/")) {
			hook.getParserResponse().addMessage(
					Severity.ERROR, 
					"Path property [" + path + "] must start with a '/' in State [" + state.getName() + "]");
		}
	}	

}

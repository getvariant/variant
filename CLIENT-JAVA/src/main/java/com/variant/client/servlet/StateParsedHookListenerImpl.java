package com.variant.client.servlet;


import com.variant.core.hook.HookListener;
import com.variant.core.hook.StateParsedHook;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserMessage.Severity;

/**
 * An implementation of {@link HookListener} listening to the {@link StateParsedHook}.
 * Performs semantical checks on the parsed {@link State}, applicable for the
 * Servlet environment.
 * 
 * @author Igor Urisman
 * 
 * @since 0.5
 * @see HookListener
 */
public class StateParsedHookListenerImpl implements HookListener<StateParsedHook> {

	/**
	 * The hook type we want to be posted on.
	 */
	@Override
	public Class<StateParsedHook> getHookClass() {
		return StateParsedHook.class;
	}

	/**
	 * Ensure that the <code>path</code> state parameter starts with a forward slash.
	 * We check this in order to avoid relative path references.
     *
	 * @see HookListener#post(com.variant.core.hook.UserHook)
	 */
	@Override
	public void post(StateParsedHook hook) {
		State state = hook.getState();
		String path = state.getParameterMap().get("path");
		if (path != null && !path.startsWith("/")) {
			hook.getParserResponse().addMessage(
					Severity.ERROR, 
					"Path property [" + path + "] must start with a '/' in State [" + state.getName() + "]");
		}
	}	

}

package com.variant.server.hook;


import com.variant.core.HookListener;
import com.variant.core.schema.State;
import com.variant.core.schema.StateParsedHook;

/**
 * A user hook listener subscribed to the {@link StateParsedHook}, which is posted
 * during schema deployment 
 * Performs semantical checks on the parsed {@link State}, applicable for the
 * servlet environment and intended to be used in conjunction with
 * {@link VariantFilter}.
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
		String path = state.getParameter("path");
		if (path != null && !path.startsWith("/")) {
			hook.addMessage("Path property [" + path + "] must start with a '/' in State [" + state.getName() + "]");
		}
	}	

}

package com.variant.server.api.hook;

import com.variant.core.UserHook;
import com.variant.core.UserError.Severity;

///TODO
public interface StateParsedLifecycleEventPostResult extends UserHook.PostResult {
	
    public void addMessage(Severity severity, String message);

}
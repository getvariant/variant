package com.variant.web;

import com.variant.core.annotations.ParserEventListener;
import com.variant.core.exception.VariantException;
import com.variant.core.schema.SchemaElement;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.StateParsedEventListener;

/**
 * Additional parse checks not implemented by the core library and specific to the Servlet environment.
 * @author Igor
 *
 */
@ParserEventListener(SchemaElement.VIEW)
public class StateParsedEventListenerImpl implements StateParsedEventListener{

	@Override
	public void stateParsed(State state) {
		if (!state.getParameterMap().get("path").startsWith("/")) {
			throw new VariantException("View path must start with a '/' in View [" + state.getName() + "]");
		}
	}
	

}

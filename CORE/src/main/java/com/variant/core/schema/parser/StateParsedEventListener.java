package com.variant.core.schema.parser;

import com.variant.core.exception.VariantException;
import com.variant.core.schema.State;

public interface StateParsedEventListener {

	public void stateParsed(State state) throws VariantException;
}

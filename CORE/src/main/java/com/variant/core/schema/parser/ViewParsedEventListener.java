package com.variant.core.schema.parser;

import com.variant.core.exception.VariantException;
import com.variant.core.schema.State;

public interface ViewParsedEventListener {

	public void viewParsed(State view) throws VariantException;
}

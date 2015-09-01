package com.variant.core.schema.parser;

import com.variant.core.exception.VariantException;
import com.variant.core.schema.Test;

public interface TestParsedEventListener {

	public void testParsed(Test test) throws VariantException;
}

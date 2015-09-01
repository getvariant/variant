package com.variant.core.schema.parser;

import com.variant.core.exception.VariantException;
import com.variant.core.schema.View;

public interface ViewParsedEventListener {

	public void viewParsed(View view) throws VariantException;
}

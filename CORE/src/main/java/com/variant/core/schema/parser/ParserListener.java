package com.variant.core.schema.parser;

import com.variant.core.schema.ParserMessage;

public interface ParserListener {

	void messageAdded(ParserMessage message);
}

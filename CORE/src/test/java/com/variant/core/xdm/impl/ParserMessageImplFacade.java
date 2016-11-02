package com.variant.core.xdm.impl;

import com.variant.core.exception.Error;
import com.variant.server.schema.ParserMessageImpl;

public class ParserMessageImplFacade extends ParserMessageImpl {

	public ParserMessageImplFacade(Error template, String...args) {
		super(template, args);
	}

}

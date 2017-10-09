package com.variant.core.schema.parser;

import com.variant.core.schema.ParserMessage.Location;

/**
 * Base abstract class for all semantic errors.
 * 
 * @author Igor
 */
abstract public class SemanticErrorLocation implements Location {
		
	@Override
	public String toString() {
		return getPath();
	}

}

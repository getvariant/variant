package com.variant.core;

import java.util.List;

import com.variant.core.error.Severity;
import com.variant.core.schema.Schema;
import com.variant.core.schema.impl.ParserError;

public interface ParserResponse {

	/**
	 * Get the current test schema.
	 * @param view
	 */
	public Schema getSchema();

	/**
	 * Does this response contain parse errors?  Equivalent to
	 * <code>getErrors().size() > 0</code>
	 * @return
	 */
	public boolean hasErrors();
	
	/**
	 * All parse errors in order they were emitted, as an unmodifiable list.
	 * @return
	 */
	public List<ParserError> getErrors();

	/**
	 * Highest error severity
	 * @return Highest severity if there are errors or null otherwise.
	 */
	public Severity highestErrorSeverity();
	
}

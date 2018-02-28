package com.variant.core.util.immutable;

public class ImmutableCollectionException extends UnsupportedOperationException {

	ImmutableCollectionException() {
		super("Illegal attempt to modify an immutable collection");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}

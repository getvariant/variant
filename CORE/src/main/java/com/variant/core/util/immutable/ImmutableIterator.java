package com.variant.core.util.immutable;

import java.util.Iterator;

/**
 * 
 */
public class ImmutableIterator<T> implements Iterator<T> {

	private final Iterator<T> iter;
	
	ImmutableIterator(Iterator<T> iter) {
		this.iter = iter;
	}
	
	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}

	@Override
	public T next() {
		return iter.next();
	}

}

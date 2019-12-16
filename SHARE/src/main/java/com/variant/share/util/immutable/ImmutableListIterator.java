package com.variant.share.util.immutable;

import java.util.ListIterator;

/**
 * Decorates another ListIterator to ensure it can't be altered.
 * Attempts to modify it will result in an UnsupportedOperationException.
 */
public class ImmutableListIterator<E> implements ListIterator<E> {

	private final ListIterator<? extends E> fromIter;
	
	ImmutableListIterator(ListIterator<? extends E> fromIter) {
		this.fromIter = fromIter;
	}
	
	@Override
	public boolean hasNext() {
		return fromIter.hasNext();
	}

	@Override
	public E next() {
		return fromIter.next();
	}

	@Override
	public boolean hasPrevious() {
		return fromIter.hasPrevious();
	}

	@Override
	public E previous() {
		return fromIter.previous();
	}

	@Override
	public int nextIndex() {
		return fromIter.nextIndex();
	}

	@Override
	public int previousIndex() {
		return fromIter.previousIndex();
	}

	@Override
	public void remove() {
		throw new ImmutableCollectionException();
	}

	@Override
	public void set(E e) {
		throw new ImmutableCollectionException();
		
	}

	@Override
	public void add(E e) {
		throw new ImmutableCollectionException();
	}

}

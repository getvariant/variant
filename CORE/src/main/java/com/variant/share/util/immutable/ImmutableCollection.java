package com.variant.share.util.immutable;

import java.util.Collection;
import java.util.Iterator;

/**
 * Decorates another List to ensure it can't be altered.
 * Attempts to modify it will result in an UnsupportedOperationException.
 */

public class ImmutableCollection<E> implements Collection<E> {

	private final Collection<? extends E> fromCollection;
	
	public ImmutableCollection(Collection<? extends E> fromCollection) {
		this.fromCollection = fromCollection;
	}

	@Override
	public int size() {
		return fromCollection.size();
	}

	@Override
	public boolean isEmpty() {
		return fromCollection.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return fromCollection.contains(o);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Iterator<E> iterator() {
		return new ImmutableIterator(fromCollection.iterator());
	}

	@Override
	public Object[] toArray() {
		return fromCollection.toArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] toArray(Object[] a) {
		return fromCollection.toArray(a);
	}

	@Override
	public boolean add(Object e) {
		throw new ImmutableCollectionException();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new ImmutableCollectionException();
	}

	@Override
	public boolean remove(Object o) {
		throw new ImmutableCollectionException();
	}

	@Override
	public boolean containsAll(@SuppressWarnings("rawtypes") Collection c) {
		return fromCollection.containsAll(c);
	}


	@Override
	public boolean removeAll(@SuppressWarnings("rawtypes") Collection c) {
		throw new ImmutableCollectionException();
	}

	@Override
	public boolean retainAll(@SuppressWarnings("rawtypes") Collection c) {
		throw new ImmutableCollectionException();
	}

	@Override
	public void clear() {
		throw new ImmutableCollectionException();
	}

}

package com.variant.share.util.immutable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * An immutable hash set.
 *
 * @param <T>
 */
public class ImmutableSet<T> implements Set<T> {

	private final Set<T> set;
	
	public ImmutableSet(Set<T> set) {
		this.set = set;
	}
	
	@Override
	public int size() {
		return set.size();
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return set.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return new ImmutableIterator<T>(set.iterator());
	}

	@Override
	public Object[] toArray() {
		return set.toArray();
	}

	@Override
	public <V> V[] toArray(V[] a) {
		return set.toArray(a);
	}

	@Override
	public boolean add(T e) {
		throw new ImmutableCollectionException();
	}

	@Override
	public boolean remove(Object o) {
		throw new ImmutableCollectionException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new ImmutableCollectionException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new ImmutableCollectionException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new ImmutableCollectionException();
	}

	@Override
	public void clear() {
		throw new ImmutableCollectionException();
	}
}

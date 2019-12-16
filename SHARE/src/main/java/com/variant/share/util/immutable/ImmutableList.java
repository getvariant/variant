package com.variant.share.util.immutable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Decorates another List to ensure it can't be altered.
 * Attempts to modify it will result in an UnsupportedOperationException.
 */

public class ImmutableList<E> implements List<E> {

	private final List<? extends E> fromList;
	
	public ImmutableList(List<? extends E> fromList) {
		this.fromList = fromList;
	}

	@Override
	public int size() {
		return fromList.size();
	}

	@Override
	public boolean isEmpty() {
		return fromList.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return fromList.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return (Iterator<E>) new ImmutableListIterator<E>(fromList.listIterator());
	}

	@Override
	public Object[] toArray() {
		return fromList.toArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] toArray(Object[] a) {
		return fromList.toArray(a);
	}

	@Override
	public boolean add(Object e) {
		throw new ImmutableCollectionException();
	}

	@Override
	public boolean remove(Object o) {
		throw new ImmutableCollectionException();
	}

	@Override
	public boolean containsAll(@SuppressWarnings("rawtypes") Collection c) {
		return fromList.containsAll(c);
	}

	@Override
	public boolean addAll(@SuppressWarnings("rawtypes") Collection c) {
		throw new ImmutableCollectionException();
	}

	@Override
	public boolean addAll(int index, @SuppressWarnings("rawtypes") Collection c) {
		throw new ImmutableCollectionException();
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

	@Override
	public E get(int index) {
		return fromList.get(index);
	}

	@Override
	public E set(int index, Object element) {
		throw new ImmutableCollectionException();
	}

	@Override
	public void add(int index, Object element) {
		throw new ImmutableCollectionException();
	}

	@Override
	public E remove(int index) {
		throw new ImmutableCollectionException();
	}

	@Override
	public int indexOf(Object o) {
		return fromList.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return fromList.lastIndexOf(o);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ListIterator listIterator() {
		return new ImmutableListIterator<E>(fromList.listIterator());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ListIterator listIterator(int index) {
		return new ImmutableListIterator<E>(fromList.listIterator(index));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List subList(int fromIndex, int toIndex) {
		return new ImmutableList<E>(fromList.subList(fromIndex, toIndex));
	}
}

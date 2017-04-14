package com.venky.core.collections;

import java.util.ArrayList;
import java.util.Collection;

public class NullList<E> extends ArrayList<E>{
	
	private static final long serialVersionUID = -2034041072277983322L;
	
	@Override
	public boolean add(E e) {
		return false;
	}

	@Override
	public boolean remove(Object o) {
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return false;
	}

	@Override
	public E get(int index) {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public E set(int index, E element) {
		throw new IndexOutOfBoundsException(); 
	}

	@Override
	public void add(int index, E element) {
		if (index != 0 ) { throw new IndexOutOfBoundsException() ;} 
	}

}

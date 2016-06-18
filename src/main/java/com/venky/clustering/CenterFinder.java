package com.venky.clustering;

public interface CenterFinder<T> {
	public T center();
	public T center(T afterAddingNewPoint);
}

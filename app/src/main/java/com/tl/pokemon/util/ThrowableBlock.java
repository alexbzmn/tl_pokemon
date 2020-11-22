package com.tl.pokemon.util;

@FunctionalInterface
public interface ThrowableBlock<T> {
	T execute() throws Exception;
}

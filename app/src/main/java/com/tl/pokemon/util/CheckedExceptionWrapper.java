package com.tl.pokemon.util;

public class CheckedExceptionWrapper {

	public static <T> T runRethrowing(ThrowableBlock<T> throwableBlock) {
		try {
			return throwableBlock.execute();
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

}

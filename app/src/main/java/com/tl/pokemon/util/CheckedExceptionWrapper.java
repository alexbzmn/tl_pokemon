package com.tl.pokemon.util;

import com.tl.pokemon.exception.NotFoundException;
import com.tl.pokemon.exception.ServiceIsUnavailableException;

public class CheckedExceptionWrapper {

	public static <T> T runRethrowing(ThrowableBlock<T> throwableBlock) {
		try {
			return throwableBlock.execute();
		} catch (Exception exception) {

			if (exception instanceof NotFoundException) {
				throw (NotFoundException)exception;
			}

			if (exception instanceof ServiceIsUnavailableException) {
				throw (ServiceIsUnavailableException)exception;
			}
			throw new RuntimeException(exception);
		}
	}

}

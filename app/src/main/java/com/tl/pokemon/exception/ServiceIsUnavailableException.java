package com.tl.pokemon.exception;

public class ServiceIsUnavailableException extends RuntimeException {

	public ServiceIsUnavailableException(String description) {
		super(description);
	}

}

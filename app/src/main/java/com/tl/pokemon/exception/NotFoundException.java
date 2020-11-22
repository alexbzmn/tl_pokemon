package com.tl.pokemon.exception;

public class NotFoundException extends RuntimeException {

	public NotFoundException(String description) {
		super(description);
	}

}

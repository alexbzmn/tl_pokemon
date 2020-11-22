package com.tl.pokemon.model;

import java.util.Objects;

public class Pokemon {

	public final String name;
	public final String description;

	public Pokemon(String name, String description) {
		this.name = name;
		this.description = description;
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Pokemon pokemon = (Pokemon)o;
		return Objects.equals(name, pokemon.name) &&
			Objects.equals(description, pokemon.description);
	}

	@Override public int hashCode() {
		return Objects.hash(name, description);
	}

	@Override public String toString() {
		return "Pokemon{" +
			"name='" + name + '\'' +
			", description='" + description + '\'' +
			'}';
	}
}

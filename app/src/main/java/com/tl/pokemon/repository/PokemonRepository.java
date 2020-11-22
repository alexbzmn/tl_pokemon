package com.tl.pokemon.repository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tl.pokemon.exception.NotFoundException;
import com.tl.pokemon.exception.ServiceIsUnavailableException;

public final class PokemonRepository {

	public static final String POKEMON_RESOURCE = "https://pokeapi.co/api/v2/pokemon/";

	private final HttpClient httpClient;
	private final ObjectMapper mapper;

	public PokemonRepository(HttpClient httpClient, ObjectMapper mapper) {
		this.httpClient = httpClient;
		this.mapper = mapper;
	}

	public List<String> getPokemonDescriptionsByName(String name) throws Exception {
		final var pokemonInfo = httpClient.send(HttpRequest.newBuilder()
			.uri(URI.create(POKEMON_RESOURCE + name))
			.GET()
			.build(), HttpResponse.BodyHandlers.ofString());

		if (pokemonInfo.statusCode() == 404) {
			throw new NotFoundException("Pokemon doesn't exist");
		} else if (pokemonInfo.statusCode() == 500) {
			throw new ServiceIsUnavailableException("Remote service error, please try later");
		} else if (pokemonInfo.statusCode() != 200) {
			throw new IllegalStateException();
		}

		final var pokemonSpeciesUrl = mapper.readTree(pokemonInfo.body()).get("species").get("url").asText();
		final var pokemonSpecies = httpClient.send(HttpRequest.newBuilder()
			.uri(URI.create(pokemonSpeciesUrl))
			.GET()
			.build(), HttpResponse.BodyHandlers.ofString());

		final var descriptions = new ArrayList<String>();
		mapper.readTree(pokemonSpecies.body()).get("flavor_text_entries").iterator().forEachRemaining(description -> {
			if (description.get("language").get("name").asText().equals("en")) {
				descriptions.add(description.get("flavor_text").asText());
			}
		});

		return descriptions;
	}
}

package com.tl.pokemon.repository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tl.pokemon.exception.ServiceIsUnavailableException;

public final class ShakespeareTranslationRepository {

	public static final String SHAKESPEARE_TRANSLATION_RESOURCE = "https://api.funtranslations.com/translate/shakespeare.json";

	private final HttpClient httpClient;
	private final ObjectMapper mapper;

	public ShakespeareTranslationRepository(HttpClient client, ObjectMapper mapper) {
		this.httpClient = client;
		this.mapper = mapper;
	}

	public String translate(String from) throws Exception {
		final var translationResponse = httpClient.send(HttpRequest.newBuilder()
			.uri(URI.create(SHAKESPEARE_TRANSLATION_RESOURCE))
			.POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of("text", from))))
			.build(), HttpResponse.BodyHandlers.ofString());

		if (translationResponse.statusCode() != 200) {
			throw new ServiceIsUnavailableException("Translation service is not available");
		}

		return mapper.readTree(translationResponse.body()).get("contents").get("translated").asText();
	}
}

package com.tl.pokemon;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.databind.ObjectMapper;

import spark.Spark;

import static com.tl.pokemon.util.CheckedExceptionWrapper.runRethrowing;
import static java.net.http.HttpResponse.BodyHandlers;
import static spark.Spark.get;
import static spark.Spark.port;

public final class App {

	public static final String POKEMON_RESOURCE = "https://pokeapi.co/api/v2/pokemon/";

	public static final String SHAKESPEARE_TRANSLATION_RESOURCE = "https://api.funtranslations.com/translate/shakespeare.json";

	private final HttpClient httpClient;

	private final ObjectMapper mapper = new ObjectMapper();

	private final Map<String, List<String>> descriptionsCache = new ConcurrentHashMap<>();

	public App(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	private List<String> retrieveDescription(String name) throws Exception {
		final var pokemonInfo = httpClient.send(HttpRequest.newBuilder()
			.uri(URI.create(POKEMON_RESOURCE + name))
			.GET()
			.build(), BodyHandlers.ofString());

		final var pokemonSpeciesUrl = mapper.readTree(pokemonInfo.body()).get("species").get("url").asText();

		final var pokemonSpecies = httpClient.send(HttpRequest.newBuilder()
			.uri(URI.create(pokemonSpeciesUrl))
			.GET()
			.build(), BodyHandlers.ofString());

		final var descriptions = mapper.readTree(pokemonSpecies.body()).get("flavor_text_entries");
		final var list = new ArrayList<String>();
		descriptions.iterator().forEachRemaining(desc -> {
			if (desc.get("language").get("name").asText().equals("en")) {
				list.add(desc.get("flavor_text").asText());
			}
		});

		return list;

	}

	private String shakespeare(String pokemonDescription) throws Exception {
		final var translation = httpClient.send(HttpRequest.newBuilder()
			.uri(URI.create(SHAKESPEARE_TRANSLATION_RESOURCE))
			.POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of("text", pokemonDescription))))
			.build(), BodyHandlers.ofString());

		return mapper.readTree(translation.body()).get("contents").get("translated").asText();
	}

	public void shutdown() {
		Spark.stop();
		Spark.awaitStop();
	}

	public void registerResources() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Spark.stop();
			Spark.awaitStop();
		}));

		port(8080);
		get("/pokemon/:name", (req, res) -> {
			final var pokemonName = req.params("name");
			final var descriptions = descriptionsCache.computeIfAbsent(pokemonName, __ -> runRethrowing((() -> retrieveDescription(pokemonName))));
			final var randomlyPickedDescription = descriptions.get(ThreadLocalRandom.current().nextInt(descriptions.size()));
			return shakespeare(randomlyPickedDescription);
		});
	}

	public static void main(String[] args) {
		final var client = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.followRedirects(HttpClient.Redirect.NORMAL)
			.connectTimeout(Duration.ofSeconds(20))
			.build();

		final var app = new App(client);
		app.registerResources();
	}

}

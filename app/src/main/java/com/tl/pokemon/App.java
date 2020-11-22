package com.tl.pokemon;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.databind.ObjectMapper;

import spark.Spark;

import static java.net.http.HttpResponse.BodyHandlers;
import static spark.Spark.get;
import static spark.Spark.port;

public class App {

	private final static ObjectMapper mapper = new ObjectMapper();

	private static final HttpClient client = HttpClient.newBuilder()
		.version(HttpClient.Version.HTTP_1_1)
		.followRedirects(HttpClient.Redirect.NORMAL)
		.connectTimeout(Duration.ofSeconds(20))
		.build();

	private static final Map<String, List<String>> descriptionsCache = new ConcurrentHashMap<>();

	public static List<String> retrieveDescription(String name) {
		try {
			final var pokemonInfo = client.send(HttpRequest.newBuilder()
				.uri(URI.create("https://pokeapi.co/api/v2/pokemon/" + name))
				.GET()
				.build(), BodyHandlers.ofString());

			final var pokemonSpeciesUrl = mapper.readTree(pokemonInfo.body()).get("species").get("url").asText();

			final var pokemonSpecies = client.send(HttpRequest.newBuilder()
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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String shakespeare(String pockemonDescription) {

		try {
			var values = new HashMap<String, String>() {{
				put("text", pockemonDescription);
			}};

			var objectMapper = new ObjectMapper();
			String requestBody = objectMapper
				.writeValueAsString(values);

			final var translation = client.send(HttpRequest.newBuilder()
				.uri(URI.create("https://api.funtranslations.com/translate/shakespeare.json"))
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build(), BodyHandlers.ofString());

			return objectMapper.readTree(translation.body()).get("contents").get("translated").asText();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Spark.stop();
			Spark.awaitStop();
		}));

		port(8080);
		get("/pokemon/:name", (req, res) -> {
			final var pockemonName = req.params("name");
			final var descriptions = descriptionsCache.computeIfAbsent(pockemonName, name -> retrieveDescription(pockemonName));
			final var randomlyPickedDescription = descriptions.get(ThreadLocalRandom.current().nextInt(descriptions.size()));
			return shakespeare(randomlyPickedDescription);
		});
	}

}

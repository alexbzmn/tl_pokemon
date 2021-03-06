package com.tl.pokemon;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tl.pokemon.exception.NotFoundException;
import com.tl.pokemon.exception.ServiceIsUnavailableException;
import com.tl.pokemon.model.Pokemon;
import com.tl.pokemon.repository.PokemonRepository;
import com.tl.pokemon.repository.ShakespeareTranslationRepository;

import static com.tl.pokemon.util.CheckedExceptionWrapper.runRethrowing;
import static spark.Spark.*;

public final class App {

	private final PokemonRepository pokemonRepository;
	private final ShakespeareTranslationRepository translationRepository;
	private final ObjectMapper objectMapper;

	private final Map<String, List<String>> descriptionsCache = new ConcurrentHashMap<>();
	private final Map<String, String> shakespeareCache = new ConcurrentHashMap<>();

	public App(HttpClient httpClient) {
		this.objectMapper = new ObjectMapper();

		this.translationRepository = new ShakespeareTranslationRepository(httpClient, objectMapper);
		this.pokemonRepository = new PokemonRepository(httpClient, objectMapper);
	}

	public void registerResources() {
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

		exception(NotFoundException.class, (exception, request, response) -> {
			response.status(404);
			response.body(exception.getMessage());
		});

		exception(ServiceIsUnavailableException.class, (exception, request, response) -> {
			response.status(500);
			response.body(exception.getMessage());
		});

		exception(IllegalArgumentException.class, (exception, request, response) -> {
			response.status(400);
			response.body(exception.getMessage());
		});

		port(5000);
		get("/pokemon/:name", (req, res) -> {
			final var pokemonName = req.params("name");
			if (pokemonName == null || pokemonName.isEmpty()) {
				throw new IllegalArgumentException("Pokemon name should not be empty");
			}
			final var pokemonDescriptions = descriptionsCache.computeIfAbsent(
				pokemonName,
				__ -> runRethrowing((() -> pokemonRepository.getPokemonDescriptionsByName(pokemonName))));
			if (pokemonDescriptions.isEmpty()) {
				throw new NotFoundException("Descriptions don't exist for this pokemon");
			}

			final var randomlyPickedDescription = pokemonDescriptions.get(ThreadLocalRandom.current().nextInt(pokemonDescriptions.size()));
			final var shakespeareDescription = shakespeareCache.computeIfAbsent(
				randomlyPickedDescription,
				__ -> runRethrowing(() -> translationRepository.translate(randomlyPickedDescription)));

			return objectMapper.writeValueAsString(new Pokemon(pokemonName, shakespeareDescription));
		});
	}

	public void shutdown() {
		stop();
		awaitStop();
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

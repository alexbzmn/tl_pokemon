package com.tl.pokemon;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tl.pokemon.model.Pokemon;

import static com.tl.pokemon.repository.PokemonRepository.POKEMON_RESOURCE;
import static com.tl.pokemon.repository.ShakespeareTranslationRepository.SHAKESPEARE_TRANSLATION_RESOURCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AppTest {

	private static final String samplePokemonDescription = "Charizard flies around the sky in search of powerful opponents.\\nIt breathes fire of such great heat that it melts anything.\\nHowever, it never turns its fiery breath on any opponent\\nweaker than itself.";
	private static final String sampleShakespeareTranslation = "Charizard flies 'round the sky in search of powerful opponents.\\nit breathes fire of such most wondrous heat yond 't melts aught.\\nhowever,  't nev'r turns its fiery breath on any opponent\\nweaker than itself.";
	private static final ObjectMapper objectMapper = new ObjectMapper();

	private final HttpClient mockedHttpClient = Mockito.mock(HttpClient.class);
	private final HttpClient client = HttpClient.newBuilder()
		.version(HttpClient.Version.HTTP_1_1)
		.followRedirects(HttpClient.Redirect.NORMAL)
		.connectTimeout(Duration.ofSeconds(20))
		.build();

	private final App app = new App(mockedHttpClient);

	@BeforeEach
	void init() {
		app.registerResources();
	}

	@AfterEach
	void finish() {
		app.shutdown();
	}

	@Test
	void should_return_shakespeare_language_charizard_description() throws Exception {

		// given
		var pokemonResponse = Mockito.mock(HttpResponse.class);
		doReturn(pokemonResponse).when(mockedHttpClient).send(HttpRequest.newBuilder()
			.uri(URI.create(POKEMON_RESOURCE + "charizard"))
			.GET()
			.build(), HttpResponse.BodyHandlers.ofString());
		when(pokemonResponse.statusCode()).thenReturn(200);
		when(pokemonResponse.body()).thenReturn(fileContent("test_response_pokemon.json"));

		var speciesResponse = Mockito.mock(HttpResponse.class);
		doReturn(speciesResponse).when(mockedHttpClient).send(HttpRequest.newBuilder()
			.uri(URI.create("https://pokeapi.co/api/v2/pokemon-species/6/"))
			.GET()
			.build(), HttpResponse.BodyHandlers.ofString());
		when(speciesResponse.body()).thenReturn(fileContent("test_response_pokemon_species.json"));

		var shakespeareResponse = Mockito.mock(HttpResponse.class);
		doReturn(shakespeareResponse).when(mockedHttpClient).send(HttpRequest.newBuilder()
			.uri(URI.create(SHAKESPEARE_TRANSLATION_RESOURCE))
			.POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(Map.of("text", samplePokemonDescription))))
			.build(), HttpResponse.BodyHandlers.ofString());
		when(shakespeareResponse.statusCode()).thenReturn(200);
		when(shakespeareResponse.body()).thenReturn(fileContent("test_response_pokemon_shakespeare_translation.json"));

		// when
		final var result = client.send(
			HttpRequest.newBuilder().uri(URI.create("http://localhost:5000/pokemon/charizard")).GET().build(),
			HttpResponse.BodyHandlers.ofString());

		// then
		assertEquals(200, result.statusCode());
		assertEquals(objectMapper.writeValueAsString(new Pokemon("charizard", sampleShakespeareTranslation)), result.body());
	}

	@Test
	void should_return_404_on_unknown_pokemon() throws Exception {

		// given
		var pokemonResponse = Mockito.mock(HttpResponse.class);
		doReturn(pokemonResponse).when(mockedHttpClient).send(HttpRequest.newBuilder()
			.uri(URI.create(POKEMON_RESOURCE + "nonexistent"))
			.GET()
			.build(), HttpResponse.BodyHandlers.ofString());
		when(pokemonResponse.statusCode()).thenReturn(404);
		when(pokemonResponse.body()).thenReturn("Not Found");

		// when
		final var result = client.send(
			HttpRequest.newBuilder().uri(URI.create("http://localhost:5000/pokemon/nonexistent")).GET().build(),
			HttpResponse.BodyHandlers.ofString());

		// then
		verify(mockedHttpClient, only()).send(any(), any());
		assertEquals(404, result.statusCode());
		assertEquals("Pokemon doesn't exist", result.body());
	}

	@Test
	void should_use_caching_on_multiple_calls() throws Exception {
		// given
		var pokemonResponse = Mockito.mock(HttpResponse.class);
		doReturn(pokemonResponse).when(mockedHttpClient).send(HttpRequest.newBuilder()
			.uri(URI.create(POKEMON_RESOURCE + "charizard"))
			.GET()
			.build(), HttpResponse.BodyHandlers.ofString());
		when(pokemonResponse.statusCode()).thenReturn(200);
		when(pokemonResponse.body()).thenReturn(fileContent("test_response_pokemon.json"));

		var speciesResponse = Mockito.mock(HttpResponse.class);
		doReturn(speciesResponse).when(mockedHttpClient).send(HttpRequest.newBuilder()
			.uri(URI.create("https://pokeapi.co/api/v2/pokemon-species/6/"))
			.GET()
			.build(), HttpResponse.BodyHandlers.ofString());
		when(speciesResponse.body()).thenReturn(fileContent("test_response_pokemon_species.json"));

		var shakespeareResponse = Mockito.mock(HttpResponse.class);
		doReturn(shakespeareResponse).when(mockedHttpClient).send(HttpRequest.newBuilder()
			.uri(URI.create(SHAKESPEARE_TRANSLATION_RESOURCE))
			.POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(Map.of("text", samplePokemonDescription))))
			.build(), HttpResponse.BodyHandlers.ofString());
		when(shakespeareResponse.statusCode()).thenReturn(200);
		when(shakespeareResponse.body()).thenReturn(fileContent("test_response_pokemon_shakespeare_translation.json"));

		// when
		final var originalCall = client.send(
			HttpRequest.newBuilder().uri(URI.create("http://localhost:5000/pokemon/charizard")).GET().build(),
			HttpResponse.BodyHandlers.ofString());

		final var cachedFirst = client.send(
			HttpRequest.newBuilder().uri(URI.create("http://localhost:5000/pokemon/charizard")).GET().build(),
			HttpResponse.BodyHandlers.ofString());

		final var cachedSecond = client.send(
			HttpRequest.newBuilder().uri(URI.create("http://localhost:5000/pokemon/charizard")).GET().build(),
			HttpResponse.BodyHandlers.ofString());

		// then
		assertEquals(200, cachedSecond.statusCode());
		assertEquals(objectMapper.writeValueAsString(new Pokemon("charizard", sampleShakespeareTranslation)), cachedSecond.body());
		verify(mockedHttpClient, times(3)).send(any(), any());
	}

	@Test
	void should_return_empty_string_on_non_english_descriptions_only() throws Exception {

		// given
		var pokemonResponse = Mockito.mock(HttpResponse.class);
		doReturn(pokemonResponse).when(mockedHttpClient).send(HttpRequest.newBuilder()
			.uri(URI.create(POKEMON_RESOURCE + "charizard"))
			.GET()
			.build(), HttpResponse.BodyHandlers.ofString());
		when(pokemonResponse.statusCode()).thenReturn(200);
		when(pokemonResponse.body()).thenReturn(fileContent("test_response_pokemon.json"));

		var speciesResponse = Mockito.mock(HttpResponse.class);
		doReturn(speciesResponse).when(mockedHttpClient).send(HttpRequest.newBuilder()
			.uri(URI.create("https://pokeapi.co/api/v2/pokemon-species/6/"))
			.GET()
			.build(), HttpResponse.BodyHandlers.ofString());
		when(speciesResponse.body()).thenReturn(fileContent("test_response_pokemon_species_no_english.json"));

		// when
		final var result = client.send(
			HttpRequest.newBuilder().uri(URI.create("http://localhost:5000/pokemon/charizard")).GET().build(),
			HttpResponse.BodyHandlers.ofString());

		// then
		assertEquals(404, result.statusCode());
		verify(mockedHttpClient, times(2)).send(any(), any());
		assertEquals("Descriptions don't exist for this pokemon", result.body());

	}

	@Test
	void should_return_readable_error_if_remote_is_unavailable() throws Exception {

		// given
		var pokemonResponse = Mockito.mock(HttpResponse.class);
		doReturn(pokemonResponse).when(mockedHttpClient).send(HttpRequest.newBuilder()
			.uri(URI.create(POKEMON_RESOURCE + "problematicPokemon"))
			.GET()
			.build(), HttpResponse.BodyHandlers.ofString());
		when(pokemonResponse.statusCode()).thenReturn(500);
		when(pokemonResponse.body()).thenReturn("Internal error");

		// when
		final var result = client.send(
			HttpRequest.newBuilder().uri(URI.create("http://localhost:5000/pokemon/problematicPokemon")).GET().build(),
			HttpResponse.BodyHandlers.ofString());

		// then
		verify(mockedHttpClient, only()).send(any(), any());
		assertEquals(500, result.statusCode());
		assertEquals("Remote service error, please try later", result.body());

	}

	@Test
	void should_return_sensible_error_on_translations_service_unavailable() throws Exception {

		// given
		var pokemonResponse = Mockito.mock(HttpResponse.class);
		doReturn(pokemonResponse).when(mockedHttpClient).send(HttpRequest.newBuilder()
			.uri(URI.create(POKEMON_RESOURCE + "charizard"))
			.GET()
			.build(), HttpResponse.BodyHandlers.ofString());
		when(pokemonResponse.statusCode()).thenReturn(200);
		when(pokemonResponse.body()).thenReturn(fileContent("test_response_pokemon.json"));

		var speciesResponse = Mockito.mock(HttpResponse.class);
		doReturn(speciesResponse).when(mockedHttpClient).send(HttpRequest.newBuilder()
			.uri(URI.create("https://pokeapi.co/api/v2/pokemon-species/6/"))
			.GET()
			.build(), HttpResponse.BodyHandlers.ofString());
		when(speciesResponse.body()).thenReturn(fileContent("test_response_pokemon_species.json"));

		var shakespeareResponse = Mockito.mock(HttpResponse.class);
		doReturn(shakespeareResponse).when(mockedHttpClient).send(HttpRequest.newBuilder()
			.uri(URI.create(SHAKESPEARE_TRANSLATION_RESOURCE))
			.POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(Map.of("text", samplePokemonDescription))))
			.build(), HttpResponse.BodyHandlers.ofString());
		when(shakespeareResponse.statusCode()).thenReturn(500);
		when(shakespeareResponse.body()).thenReturn("Internal error");

		// when
		final var result = client.send(
			HttpRequest.newBuilder().uri(URI.create("http://localhost:5000/pokemon/charizard")).GET().build(),
			HttpResponse.BodyHandlers.ofString());

		// then
		verify(mockedHttpClient, times(3)).send(any(), any());
		assertEquals(500, result.statusCode());
		assertEquals("Translation service is not available", result.body());
	}

	private String fileContent(String pathString) throws Exception {
		Path path = Paths.get("src/test/resources/" + pathString);
		return String.join("", Files.readAllLines(path));
	}

}

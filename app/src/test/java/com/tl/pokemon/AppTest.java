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

import static com.tl.pokemon.App.POKEMON_RESOURCE;
import static com.tl.pokemon.App.SHAKESPEARE_TRANSLATION_RESOURCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

class AppTest {

	private static final String samplePokemonDescription = "Charizard flies around the sky in search of powerful opponents.\\nIt breathes fire of such great heat that it melts anything.\\nHowever, it never turns its fiery breath on any opponent\\nweaker than itself.";

	private static final String sampleShakespeareTranslation = "Charizard flies 'round the sky in search of powerful opponents.\\nit breathes fire of such most wondrous heat yond 't melts aught.\\nhowever,  't nev'r turns its fiery breath on any opponent\\nweaker than itself.";

	private final HttpClient mockedHttpClient = Mockito.mock(HttpClient.class);

	private final App app = new App(mockedHttpClient);

	private final HttpClient client = HttpClient.newBuilder()
		.version(HttpClient.Version.HTTP_1_1)
		.followRedirects(HttpClient.Redirect.NORMAL)
		.connectTimeout(Duration.ofSeconds(20))
		.build();

	@BeforeEach
	void init() {
		app.registerResources();
	}

	@AfterEach
	void finish() {
		app.shutdown();
	}

	@Test
	void test() throws Exception {

		// given
		var pokemonResponse = Mockito.mock(HttpResponse.class);
		doReturn(pokemonResponse).when(mockedHttpClient).send(HttpRequest.newBuilder()
			.uri(URI.create(POKEMON_RESOURCE + "charizard"))
			.GET()
			.build(), HttpResponse.BodyHandlers.ofString());
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
		when(shakespeareResponse.body()).thenReturn(fileContent("test_response_pokemon_shakespeare_translation.json"));

		// when
		final var result = client.send(
			HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/pokemon/charizard")).GET().build(),
			HttpResponse.BodyHandlers.ofString());

		// then
		assertEquals(200, result.statusCode());
		assertEquals(sampleShakespeareTranslation, result.body());
	}

	private String fileContent(String pathString) throws Exception {
		Path path = Paths.get("src/test/resources/" + pathString);
		return String.join("", Files.readAllLines(path));
	}

}

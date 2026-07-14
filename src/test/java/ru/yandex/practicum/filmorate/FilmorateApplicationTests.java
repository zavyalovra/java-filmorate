package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import com.google.gson.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class FilmorateApplicationTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void createUser_withEmptyJson_shouldReturnBadRequest() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>("{}", headers);
		ResponseEntity<String> response = restTemplate.postForEntity("/users", request, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
		assertEquals("Ошибка валидации", json.get("error").getAsString());
	}


	@Test
	void createUser_withValidData_shouldReturnOkAndUser() {
		String payload = "{\n"
				+ "    \"email\": \"john@example.com\",\n"
				+ "    \"login\": \"john_doe\",\n"
				+ "    \"name\": \"John\",\n"
				+ "    \"birthday\": \"1990-01-01\"\n"
				+ "}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> request = new HttpEntity<>(payload, headers);
		ResponseEntity<User> response = restTemplate.postForEntity("/users", request, User.class);

		assertEquals(HttpStatus.OK, response.getStatusCode(), "POST /users должен вернуть 200");

		User responseUser = response.getBody();
		assertNotNull(responseUser, "Тело ответа не должно быть null");
		assertNotNull(responseUser.getId(), "id не должен быть null");
		assertEquals("john@example.com", responseUser.getEmail(), "email должен совпадать");
		assertEquals("john_doe", responseUser.getLogin(), "login должен совпадать");
	}

	@Test
	void createUser_withInvalidEmail_shouldReturnBadRequest() {
		String payload = "{\n"
				+ "    \"email\": \"invalid-email\",\n"
				+ "    \"login\": \"john_doe\",\n"
				+ "    \"name\": \"John\",\n"
				+ "    \"birthday\": \"1990-01-01\"\n"
				+ "}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(payload, headers);
		ResponseEntity<String> response = restTemplate.postForEntity("/users", request, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
		assertEquals("Ошибка валидации", json.get("error").getAsString());
	}

	@Test
	void createUser_withLoginContainingSpaces_shouldReturnBadRequest() {
		String payload = "{\n"
				+ "    \"email\": \"john@example.com\",\n"
				+ "    \"login\": \"john doe\",\n"
				+ "    \"name\": \"John\",\n"
				+ "    \"birthday\": \"1990-01-01\"\n"
				+ "}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(payload, headers);
		ResponseEntity<String> response = restTemplate.postForEntity("/users", request, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
		assertEquals("Ошибка валидации", json.get("error").getAsString());
	}

	@Test
	void createUser_withFutureBirthday_shouldReturnBadRequest() {
		LocalDate futureDate = LocalDate.now().plusDays(1);
		String payload = "{\n"
				+ "    \"email\": \"john@example.com\",\n"
				+ "    \"login\": \"john_doe\",\n"
				+ "    \"name\": \"John\",\n"
				+ "    \"birthday\": \"" + futureDate + "\"\n"
				+ "}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(payload, headers);
		ResponseEntity<String> response = restTemplate.postForEntity("/users", request, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
		assertEquals("Ошибка валидации", json.get("error").getAsString());
	}

	@Test
	void createUser_withBlankNameShouldUseLogin() {
		String payload = "{\n"
				+ "    \"email\": \"alex@example.com\",\n"
				+ "    \"login\": \"alex_clear\",\n"
				+ "    \"name\": \"\",\n"
				+ "    \"birthday\": \"1990-01-01\"\n"
				+ "}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(payload, headers);
		ResponseEntity<String> response = restTemplate.postForEntity("/users", request, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
		assertEquals("alex_clear", json.get("name").getAsString(), "Имя должно быть установлено на login");
	}

	@Test
	void createFilm_withEmptyJson_shouldReturnBadRequest() {
		String payload = "{}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(payload, headers);
		ResponseEntity<String> response = restTemplate.postForEntity("/films", request, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
		assertEquals("Ошибка валидации", json.get("error").getAsString());
	}

	@Test
	void createFilm_withValidData_shouldReturnOkAndFilm() {
		String payload = "{\n"
				+ "  \"name\": \"nisi eiusmod\",\n"
				+ "  \"description\": \"adipisicing\",\n"
				+ "  \"releaseDate\": \"1967-03-25\",\n"
				+ "  \"duration\": 100\n"
				+ "}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> request = new HttpEntity<>(payload, headers);
		ResponseEntity<Film> response = restTemplate.postForEntity("/films", request, Film.class);

		assertEquals(HttpStatus.OK, response.getStatusCode(), "POST /films должен вернуть 200");

		Film responseFilm = response.getBody();
		assertNotNull(responseFilm, "Тело ответа не должно быть null");
		assertNotNull(responseFilm.getId(), "id не должен быть null");
		assertEquals("nisi eiusmod", responseFilm.getName(), "name должен совпадать");
		assertEquals("adipisicing", responseFilm.getDescription(), "description должен совпадать");
		assertEquals(LocalDate.of(1967, 3, 25), responseFilm.getReleaseDate(),
				"releaseDate должен совпадать");
		assertEquals(100, responseFilm.getDuration(), "duration должен совпадать");
	}

	@Test
	void createFilm_withBlankNameAndNegativeDuration_shouldReturnBadRequest() {
		String payload = "{\n"
				+ "  \"name\": \"\",\n"
				+ "  \"description\": \"adipisicing\",\n"
				+ "  \"releaseDate\": \"1967-03-25\",\n"
				+ "  \"duration\": -100\n"
				+ "}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(payload, headers);
		ResponseEntity<String> response = restTemplate.postForEntity("/films", request, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
		assertEquals("Ошибка валидации", json.get("error").getAsString());
	}

	@Test
	void createFilm_withInvalidReleaseDate_shouldReturnBadRequest() {
		String payload = "{\n"
				+ "  \"name\": \"eiusmod\",\n"
				+ "  \"description\": \"adipisicing\",\n"
				+ "  \"releaseDate\": \"1894-01-01\",\n"
				+ "  \"duration\": 100\n"
				+ "}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(payload, headers);
		ResponseEntity<String> response = restTemplate.postForEntity("/films", request, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
		assertEquals("Ошибка валидации", json.get("error").getAsString());
	}

	@Test
	void createFilm_withBoundaryReleaseDate_shouldReturnOk() {
		String payload = "{\n"
				+ "  \"name\": \"Film with Boundary Date\",\n"
				+ "  \"description\": \"adipisicing\",\n"
				+ "  \"releaseDate\": \"1895-12-28\",\n"
				+ "  \"duration\": 100\n"
				+ "}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> request = new HttpEntity<>(payload, headers);
		ResponseEntity<Film> response = restTemplate.postForEntity("/films", request, Film.class);

		assertEquals(HttpStatus.OK, response.getStatusCode(), "POST /films должен вернуть 200");

		Film responseFilm = response.getBody();
		assertNotNull(responseFilm, "Тело ответа не должно быть null");
		assertNotNull(responseFilm.getId(), "id не должен быть null");
		assertEquals("Film with Boundary Date", responseFilm.getName(), "name должен совпадать");
		assertEquals("adipisicing", responseFilm.getDescription(), "description должен совпадать");
		assertEquals(LocalDate.of(1895, 12, 28), responseFilm.getReleaseDate(),
				"releaseDate должен совпадать");
		assertEquals(100, responseFilm.getDuration(), "duration должен совпадать");
	}

	@Test
	void createFilm_withDescriptionTooLong_shouldReturnBadRequest() {
		String payload = "{\n"
				+ "  \"name\": \"Film with long description\",\n"
				+ "  \"description\": \"" + "a".repeat(201) + "\",\n"
				+ "  \"releaseDate\": \"1895-12-28\",\n"
				+ "  \"duration\": 100\n"
				+ "}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(payload, headers);
		ResponseEntity<String> response = restTemplate.postForEntity("/films", request, String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
		assertEquals("Ошибка валидации", json.get("error").getAsString());
	}

	@Test
	void getFilms_shouldReturnFilmList() {
		String payload = "{\n"
				+ "  \"name\": \"New Film for list\",\n"
				+ "  \"description\": \"adipisicing\",\n"
				+ "  \"releaseDate\": \"1967-03-25\",\n"
				+ "  \"duration\": 100\n"
				+ "}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(payload, headers);
		ResponseEntity<String> addFilmResponse = restTemplate.postForEntity("/films", request, String.class);
		assertEquals(HttpStatus.OK, addFilmResponse.getStatusCode());

		ResponseEntity<String> getFilmsResponse = restTemplate.getForEntity("/films", String.class);
		assertEquals(HttpStatus.OK, getFilmsResponse.getStatusCode());
		assertNotNull(getFilmsResponse.getBody());

		JsonArray films = JsonParser.parseString(getFilmsResponse.getBody()).getAsJsonArray();
		assertFalse(films.isEmpty());
	}

	@Test
	void getUsers_shouldReturnUserList() {
		String payload = "{\n"
				+ "    \"email\": \"smith@example.com\",\n"
				+ "    \"login\": \"smith\",\n"
				+ "    \"name\": \"smith agent\",\n"
				+ "    \"birthday\": \"1990-01-01\"\n"
				+ "}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(payload, headers);
		ResponseEntity<String> addUserResponse = restTemplate.postForEntity("/users", request, String.class);
		assertEquals(HttpStatus.OK, addUserResponse.getStatusCode());

		ResponseEntity<String> getUsersResponse = restTemplate.getForEntity("/users", String.class);
		assertEquals(HttpStatus.OK, getUsersResponse.getStatusCode());
		assertNotNull(getUsersResponse.getBody());

		JsonArray users = JsonParser.parseString(getUsersResponse.getBody()).getAsJsonArray();
		assertFalse(users.isEmpty());
	}
}
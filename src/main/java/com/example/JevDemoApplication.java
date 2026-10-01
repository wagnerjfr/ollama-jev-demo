package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@SpringBootApplication
public class JevDemoApplication {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final HttpClient HTTP = HttpClient.newHttpClient();

	// The names we give our three questions
	private static final String Q_DEPARTMENT = "department";
	private static final String Q_FRUSTRATION = "frustration";
	private static final String Q_URGENT = "is_urgent";

	public static void main(String[] args) throws Exception {
		SpringApplication.run(JevDemoApplication.class, args);

		String message = "Hi, I've been trying to connect my Stripe account for 3 days "
				+ "and the integration keeps failing. I'm losing sales. Please help ASAP.";

		TicketAnalysis result = analyze(message);

		System.out.printf("Team: %s (confidence %.2f)%n",
				result.department(), result.departmentConfidence());
		System.out.println("Frustration: " + result.frustration());
		System.out.printf("Urgency: %.2f%n", result.urgency());

		String action = switch (result.handling()) {
			case HUMAN_REVIEW -> "Send to a person to check";
			case PRIORITY     -> "Fast lane for the " + result.department() + " team";
			case NORMAL       -> "Normal queue for the " + result.department() + " team";
		};
		System.out.println("Action: " + action);
	}

	static TicketAnalysis analyze(String message) throws Exception {
		JsonNode answers = send(buildRequest(message));

		JsonNode department = answers.get(Q_DEPARTMENT);
		JsonNode frustration = answers.get(Q_FRUSTRATION);
		JsonNode urgent = answers.get(Q_URGENT);

		return new TicketAnalysis(
				Department.fromApiName(department.get("choice").asText()),
				department.get("confidence").asDouble(),
				Frustration.fromScore(frustration.get("score").asDouble()),
				urgent.get("noul").asDouble());
	}

	static ObjectNode buildRequest(String message) {
		ObjectNode request = MAPPER.createObjectNode();
		request.put("state", message);
		request.put("model", "tev1:0.8b");
		ObjectNode questions = request.putObject("questions");

		// Question 1: pick one team. The options come straight from the enum.
		ObjectNode department = questions.putObject(Q_DEPARTMENT);
		department.put("type", "choice");
		department.put("instructions", "Which team should handle this?");
		ObjectNode options = department.putObject("criteria");
		for (Department d : Department.values()) {
			options.put(d.apiName(), d.description());
		}

		// Question 2: rate frustration. The levels also come from the enum.
		ObjectNode frustration = questions.putObject(Q_FRUSTRATION);
		frustration.put("type", "score");
		frustration.put("instructions", "How frustrated the customer appears");
		ArrayNode levels = frustration.putArray("criteria");
		for (Frustration f : Frustration.values()) {
			levels.add(f.description());
		}

		// Question 3: yes or no
		ObjectNode urgent = questions.putObject(Q_URGENT);
		urgent.put("type", "noul");
		urgent.put("instructions", "The message conveys urgency or time-sensitivity");

		return request;
	}

	static JsonNode send(ObjectNode request) throws Exception {
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:11434/v1/systemone"))
				.header("Content-Type", "application/json")
				.header("Accept", "application/json")
				.timeout(Duration.ofSeconds(30))
				.POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(request)))
				.build();

		HttpResponse<String> response = sendWithRetry(httpRequest);

		if (response.statusCode() != 200) {
			throw new RuntimeException("Error " + response.statusCode() + ": " + response.body());
		}
		return MAPPER.readTree(response.body()).get("answers");
	}

	// Tries again if the service is busy (see "When things go wrong" below)
	static HttpResponse<String> sendWithRetry(HttpRequest request) throws Exception {
		long waitMs = 500;
		for (int attempt = 1; attempt <= 5; attempt++) {
			HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
			int status = response.statusCode();
			if (status != 429 && status != 529) {
				return response;
			}
			Thread.sleep(waitMs);
			waitMs *= 2;   // wait twice as long each time
		}
		throw new RuntimeException("Service still busy after 5 tries");
	}
}

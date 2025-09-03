package integration.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.ComposeContainer;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BsvDriverIntegrationTest {

    private static final String driverUrl = "http://localhost:9116";

    private final static String VALID_DID = "did:bsv:adaf8c37db395b05bde08ddfb47eb898108dcdef8cf8dac3d9d1bc587d57828e";
    private final static String DEACTIVATED_DID = "did:bsv:9b7cfb6dd65dea457323579798c1479cbd9d00304c1384d590ff15559e6519cf";

    private static ComposeContainer driverContainer;

    private static final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();


    @BeforeAll
    public static void setUp() {

        driverContainer = new ComposeContainer(new File("docker-compose-integration-test.yml")).withExposedService("bsvdid-driver-test", 9115)
            .withLocalCompose(true); // service name & port from docker-compose.yml
        driverContainer.start();
    }

    @AfterAll
    public static void tearDown() {
        driverContainer.stop();
    }

    @Test
    void testValidDidResolution() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(driverUrl + "/1.0/identifiers/" + VALID_DID)).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"id\""));
        assertTrue(response.body().contains(VALID_DID));
    }

    @Test
    void testInvalidDidFormat() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(driverUrl + "/1.0/identifiers/did:bsv:invalid!")).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("INVALID_DID"));
    }


    @Test
    void testDeactivatedDid() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(driverUrl + "/1.0/identifiers/" + DEACTIVATED_DID)).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(410, response.statusCode());
        assertTrue(response.body().contains("\"error\":\"deactivated\""));
    }
}

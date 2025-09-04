package uni.resolver.driver.did.bsv;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class BsvHttpClient {

    private final HttpClient client;
    private final String resolverBaseUrl;
    private final Integer connectionTimeOut;

    @Inject
    public BsvHttpClient(
        @ConfigProperty(name = "bsv.resolver.url") String resolverBaseUrl,
        @ConfigProperty(name = "quarkus.rest-client.bsv-resolver-api.connect-timeout",
            defaultValue = "10") String connectTimeout
    ) {
        this.resolverBaseUrl = resolverBaseUrl.endsWith("/") ?
            resolverBaseUrl : resolverBaseUrl + "/";

        this.client = HttpClient.newBuilder().build();
        this.connectionTimeOut = Integer.parseInt(connectTimeout);
    }

    public CompletableFuture<HttpResponse<String>> resolveDid(String didString) {
        if (!didString.matches("^did:bsv:[a-fA-F0-9]{64}$")) {
            throw new IllegalArgumentException("Invalid BSV DID format");
        }

        URI uri = URI.create(resolverBaseUrl)
            .resolve("/1.0/identifiers/" + URLEncoder.encode(didString, StandardCharsets.UTF_8));


        return client.sendAsync(HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/ld+json,application/did+ld+json,application/json")
                .timeout(Duration.ofSeconds(connectionTimeOut))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );
    }
}
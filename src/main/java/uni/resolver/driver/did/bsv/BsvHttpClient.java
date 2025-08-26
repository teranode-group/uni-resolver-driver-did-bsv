package uni.resolver.driver.did.bsv;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
        String url = resolverBaseUrl + "1.0/identifiers/" + didString;

        return client.sendAsync(HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/ld+json,application/did+ld+json,application/json")
                .timeout(Duration.ofSeconds(connectionTimeOut))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );
    }
}
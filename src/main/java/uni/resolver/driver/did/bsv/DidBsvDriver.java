package uni.resolver.driver.did.bsv;

import foundation.identity.did.DID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.result.ResolveResult;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletionException;

@ApplicationScoped
public class DidBsvDriver {
    private final Logger log = LoggerFactory.getLogger(DidBsvDriver.class);

    BsvHttpClient httpClient;

    DidBsvDriver(BsvHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public ResolveResponse resolve(DID did) throws ResolutionException {
        try {
            HttpResponse<String> response = httpClient.resolveDid(did.toString()).join();
            ResolveResult result = ResolveResult.fromJson(response.body());

            log.debug("Resolver response with status code: {} /n {}", response.statusCode(), response);

            return new ResolveResponse(result, Response.Status.fromStatusCode(response.statusCode()));

        } catch (CompletionException e) {
            throw new ResolutionException("Connection to resolver failed: " + e.getCause().getMessage());
        } catch (IOException | RuntimeException e) {
            throw new ResolutionException("Can not parse response from Resolver: " + e.getMessage());
        }
    }
}
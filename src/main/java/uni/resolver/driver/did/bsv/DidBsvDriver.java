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
import java.util.Map;
import java.util.concurrent.CompletionException;

@ApplicationScoped
public class DidBsvDriver {
    private final Logger log = LoggerFactory.getLogger(DidBsvDriver.class);

    BsvHttpClient httpClient;

    private static final String CONTENT_TYPE = "contentType";

    public DidBsvDriver(BsvHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public ResolveResponse resolve(DID did) throws ResolutionException {
        try {
            HttpResponse<String> response = httpClient.resolveDid(did.toString()).join();

            ResolveResult result = ResolveResult.fromJson(response.body());

            ResolveResult remappedResult = remapToUniversalResolverFormat(result);

            log.debug("Resolver response with status code: {} /n {}", response.statusCode(), response);

            return new ResolveResponse(remappedResult, Response.Status.fromStatusCode(response.statusCode()));

        } catch (CompletionException e) {
            throw new ResolutionException("Connection to resolver failed: " + e.getCause().getMessage());
        } catch (IOException | RuntimeException e) {
            throw new ResolutionException("Can not parse response from Resolver: " + e.getMessage());
        }
    }

    private ResolveResult remapToUniversalResolverFormat(ResolveResult originalResult) {
        // Create a new result based on the original
        ResolveResult remappedResult = ResolveResult.build();

        // Copy didDocument as-is
        remappedResult.setDidDocument(originalResult.getDidDocument());

        // Copy didDocumentMetadata as original (no changes)
        remappedResult.setDidDocumentMetadata(originalResult.getDidDocumentMetadata());

        // Handle didResolutionMetadata - fix contentType only
        if (originalResult.getDidResolutionMetadata() != null) {
            Map<String, Object> resolutionMetadata = originalResult.getDidResolutionMetadata();

            // Fix contentType field (convert object to string)
            if (resolutionMetadata.containsKey(CONTENT_TYPE)) {
                Object contentType = resolutionMetadata.get(CONTENT_TYPE);
                if (contentType instanceof Map) {
                    Map<String, Object> contentTypeMap = (Map<String, Object>) contentType;
                    resolutionMetadata.put(CONTENT_TYPE, contentTypeMap.getOrDefault("mimeType", "application/did+ld+json"));
                }
            }
            remappedResult.setDidResolutionMetadata(resolutionMetadata);
        }
        return remappedResult;
    }
}
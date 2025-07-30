package uni.resolver.driver.did.bsv;

import foundation.identity.did.DID;
import foundation.identity.did.DIDURL;
import jakarta.enterprise.context.ApplicationScoped;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletionException;

@ApplicationScoped
public class DidBsvDriver implements Driver {

    BsvHttpClient httpClient;

    DidBsvDriver(BsvHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public ResolveResult resolve(DID did, Map<String, Object> resolutionOptions) throws ResolutionException {

        try {
            HttpResponse<String> response = httpClient.resolveDid(did.toString()).join();
            ResolveResult result = ResolveResult.fromJson(response.body());

            if (response.statusCode() != 200) {
                Map<String, Object> metadata = result.getDidResolutionMetadata();
                metadata.put("properties", Map.of(
                    "x-httpStatus", response.statusCode()
                ));
            }

            log.debug("Resolver response: {}", result);

            return result;

        } catch (CompletionException e) {
            throw new ResolutionException("Connection to resolver failed: " + e.getCause().getMessage());
        } catch (Exception e) {
            throw new ResolutionException("Unexpected error: " + e.getMessage());
        }
    }


    @Override
    public DereferenceResult dereference(DIDURL didurl, Map<String, Object> map) {
        return null;
    }

    @Override
    public Map<String, Object> properties() throws ResolutionException {
        return Driver.super.properties();
    }
}
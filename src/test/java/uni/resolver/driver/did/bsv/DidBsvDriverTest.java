package uni.resolver.driver.did.bsv;

import foundation.identity.did.DID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uniresolver.ResolutionException;
import uniresolver.result.ResolveResult;

import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DidBsvDriverTest {

    @Mock
    private BsvHttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockHttpResponse;

    private DidBsvDriver driver;

    @BeforeEach
    void setUp() {
        driver = new DidBsvDriver(mockHttpClient);
    }

    @Test
    void resolve_shouldReturnResultForSuccessfulResolution() throws Exception {
        String testDid = "did:bsv:test123";
        String jsonResponse = "{\"didDocument\":{\"id\":\"did:bsv:test123\"}}";

        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.resolveDid(anyString())).thenReturn(completedFuture(mockHttpResponse));

        ResolveResult result = driver.resolve(DID.fromString(testDid), Map.of());

        assertNotNull(result);
        assertEquals(testDid, result.getDidDocument().getId().toString());
        verify(mockHttpClient).resolveDid(testDid);
    }

    @Test
    void resolve_shouldIncludeStatusCodeInMetadataForNon200Responses() throws Exception {
        String testDid = "did:bsv:test456";
        int expectedStatus = 404;
        String jsonResponse = "{\"didResolutionMetadata\":{}}";

        when(mockHttpResponse.statusCode()).thenReturn(expectedStatus);
        when(mockHttpResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.resolveDid(anyString())).thenReturn(completedFuture(mockHttpResponse));

        ResolveResult result = driver.resolve(DID.fromString(testDid), Map.of());

        assertNotNull(result.getDidResolutionMetadata());
        Map<?, ?> properties = (Map<?, ?>) result.getDidResolutionMetadata().get("properties");
        assertEquals(expectedStatus, properties.get("x-httpStatus"));
    }

    @Test
    void resolve_shouldThrowResolutionExceptionForHttpClientFailure() {
        String testDid = "did:bsv:test789";
        when(mockHttpClient.resolveDid(anyString()))
            .thenReturn(failedFuture(new CompletionException(new RuntimeException("Connection failed"))));

        ResolutionException exception = assertThrows(ResolutionException.class, () -> {
            driver.resolve(DID.fromString(testDid), Map.of());
        });
        assertTrue(exception.getMessage().contains("Connection to resolver failed"));
    }

    @Test
    void resolve_shouldThrowResolutionExceptionForInvalidJsonResponse() {
        String testDid = "did:bsv:testInvalid";
        when(mockHttpResponse.body()).thenReturn("invalid json");
        when(mockHttpClient.resolveDid(anyString())).thenReturn(completedFuture(mockHttpResponse));

        ResolutionException exception = assertThrows(ResolutionException.class, () -> {
            driver.resolve(DID.fromString(testDid), Map.of());
        });
        assertTrue(exception.getMessage().contains("Unexpected error"));
    }

    private static <T> java.util.concurrent.CompletableFuture<T> completedFuture(T value) {
        return java.util.concurrent.CompletableFuture.completedFuture(value);
    }

    private static <T> java.util.concurrent.CompletableFuture<T> failedFuture(Throwable ex) {
        java.util.concurrent.CompletableFuture<T> future = new java.util.concurrent.CompletableFuture<>();
        future.completeExceptionally(ex);
        return future;
    }
}
package unit.test;

import foundation.identity.did.DID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uni.resolver.driver.did.bsv.BsvHttpClient;
import uni.resolver.driver.did.bsv.DidBsvDriver;
import uni.resolver.driver.did.bsv.ResolveResponse;
import uniresolver.ResolutionException;
import uniresolver.result.ResolveResult;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        ResolveResponse result = driver.resolve(DID.fromString(testDid));

        assertNotNull(result);
        assertEquals(testDid, result.resolveResult().getDidDocument().getId().toString());
        verify(mockHttpClient).resolveDid(testDid);
    }

    @Test
    void resolve_shouldThrowResolutionExceptionForHttpClientFailure() {
        String testDid = "did:bsv:test789";
        when(mockHttpClient.resolveDid(anyString()))
            .thenReturn(failedFuture(new CompletionException(new RuntimeException("Connection failed"))));

        ResolutionException exception = assertThrows(ResolutionException.class, () -> driver.resolve(DID.fromString(testDid)));
        assertTrue(exception.getMessage().contains("Connection to resolver failed"));
    }

    @Test
    void resolve_shouldThrowResolutionExceptionForInvalidJsonResponse() {
        String testDid = "did:bsv:testInvalid";

        when(mockHttpResponse.body()).thenReturn("bad json but status 200");
        when(mockHttpClient.resolveDid(anyString()))
            .thenReturn(CompletableFuture.completedFuture(mockHttpResponse));

        try (MockedStatic<ResolveResult> mockedStatic = mockStatic(ResolveResult.class)) {
            mockedStatic.when(() -> ResolveResult.fromJson(anyString()))
                .thenThrow(new IOException("Invalid JSON"));

            ResolutionException exception = assertThrows(ResolutionException.class,
                () -> driver.resolve(DID.fromString(testDid)));

            assertEquals("Can not parse response from Resolver: Invalid JSON",
                exception.getMessage());
        }
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
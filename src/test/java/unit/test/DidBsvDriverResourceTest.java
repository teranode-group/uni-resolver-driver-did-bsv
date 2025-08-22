package unit.test;

import foundation.identity.did.DID;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uni.resolver.driver.did.bsv.DidBsvDriver;
import uni.resolver.driver.did.bsv.ResolveResponse;
import uni.resolver.driver.did.bsv.rest.DidBsvDriverResource;
import uniresolver.result.ResolveResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DidBsvDriverResourceTest {

    @Mock
    private DidBsvDriver mockDriver;

    private DidBsvDriverResource resource;

    @BeforeEach
    void setUp() {
        resource = new DidBsvDriverResource(mockDriver);
    }

    @Test
    void resolve_shouldReturn200WithValidDid() throws Exception {
        String testDid = "did:bsv:test123";

        ResolveResult resolveResult = ResolveResult.build();
        ResolveResponse mockResult = new ResolveResponse(resolveResult, Response.Status.OK);

        when(mockDriver.resolve(any(DID.class))).thenReturn(mockResult);

        try (Response response = resource.resolve(testDid)) {
            assertEquals(200, response.getStatus());
            assertNotNull(response.getEntity());
        }
    }

    @Test
    void resolve_shouldReturn500WhenDriverThrowsException() throws Exception {
        String testDid = "did:bsv:test789";
        when(mockDriver.resolve(any(DID.class))).thenThrow(new RuntimeException("Test exception"));

        try (Response response = resource.resolve(testDid)) {
            assertEquals(500, response.getStatus());
        }
    }

    @Test
    void resolve_shouldHandleInvalidDidFormat() {
        String invalidDid = "invalid-did-format";

        try (Response response = resource.resolve(invalidDid)) {
            assertEquals(400, response.getStatus());
        }
    }
}
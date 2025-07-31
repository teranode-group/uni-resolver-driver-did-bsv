package uni.resolver.driver.did.bsv;

import foundation.identity.did.DID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uni.resolver.driver.did.bsv.rest.DidBsvDriverResource;
import uniresolver.result.ResolveResult;

import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
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
        ResolveResult mockResult = ResolveResult.build();
        when(mockDriver.resolve(any(DID.class), anyMap())).thenReturn(mockResult);

        Response response = resource.resolve(testDid);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void resolve_shouldReturnCustomHttpStatusFromMetadata() throws Exception {
        String testDid = "did:bsv:test456";
        int expectedStatus = 404;

        Map<String, Object> properties = new HashMap<>();
        properties.put("x-httpStatus", expectedStatus);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("properties", properties);

        ResolveResult mockResult = ResolveResult.build();
        mockResult.setDidResolutionMetadata(metadata);

        when(mockDriver.resolve(any(DID.class), anyMap())).thenReturn(mockResult);

        Response response = resource.resolve(testDid);

        assertEquals(expectedStatus, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void resolve_shouldReturn500WhenDriverThrowsException() throws Exception {
        String testDid = "did:bsv:test789";
        when(mockDriver.resolve(any(DID.class), anyMap())).thenThrow(new RuntimeException("Test exception"));

        Response response = resource.resolve(testDid);

        assertEquals(500, response.getStatus());
    }

    @Test
    void resolve_shouldHandleInvalidDidFormat() {
        String invalidDid = "invalid-did-format";

        Response response = resource.resolve(invalidDid);

        assertEquals(500, response.getStatus());
    }
}
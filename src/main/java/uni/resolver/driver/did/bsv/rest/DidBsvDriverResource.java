package uni.resolver.driver.did.bsv.rest;

import foundation.identity.did.DID;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uni.resolver.driver.did.bsv.DidBsvDriver;
import uniresolver.result.ResolveResult;

import java.util.Map;

@Path("/1.0/identifiers")
public class DidBsvDriverResource {

    private final DidBsvDriver driver;

    public DidBsvDriverResource(DidBsvDriver driver) {
        this.driver = driver;
    }

    @GET
    @Path("/{did}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response resolve(@PathParam("did") String didString) {
        try {
            ResolveResult result = driver.resolve(DID.fromString(didString), Map.of());

            int status = 200; // Default
            if (result.getDidResolutionMetadata() != null
                && result.getDidResolutionMetadata().get("properties") instanceof Map<?, ?> customProperty
                && (customProperty.get("x-httpStatus") instanceof Integer)) {
                status = (Integer) customProperty.get("x-httpStatus");
            }

            return Response.status(status)
                .entity(result.toJson())
                .build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }
}
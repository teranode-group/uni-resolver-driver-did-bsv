package uni.resolver.driver.did.bsv.rest;

import foundation.identity.did.DID;
import foundation.identity.did.parser.ParserException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uni.resolver.driver.did.bsv.DidBsvDriver;
import uni.resolver.driver.did.bsv.ResolveResponse;
import uniresolver.ResolutionException;

@Path("/1.0/identifiers")
public class DidBsvDriverResource {

    private static final Logger log = LoggerFactory.getLogger(DidBsvDriverResource.class);
    private final DidBsvDriver driver;

    public DidBsvDriverResource(DidBsvDriver driver) {
        this.driver = driver;
    }

    @GET
    @Path("/{did}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response resolve(@PathParam("did") String didString) {
        try {
            log.info("Resolve endpoint method is called");
            ResolveResponse result = driver.resolve(DID.fromString(didString));
            return Response.status(result.httpStatusCode())
                .entity(result.resolveResult().toJson())
                .build();
        } catch (ResolutionException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .build();
        } catch (ParserException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ResolutionException.ERROR_INVALIDDID)
                .build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Unexpected error: " + e.getMessage())
                .build();
        }
    }
}
package uni.resolver.driver.did.bsv;

import jakarta.ws.rs.core.Response;
import uniresolver.result.ResolveResult;

public record ResolveResponse(ResolveResult resolveResult, Response.Status httpStatusCode) {
}

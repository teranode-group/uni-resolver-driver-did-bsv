package uni.resolver.driver.did.bsv;

import foundation.identity.did.DID;
import foundation.identity.did.DIDURL;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

import java.util.Map;

public class DidBsvDriver implements Driver {
    @Override
    public ResolveResult resolve(DID did, Map<String, Object> map) throws ResolutionException {
        return null;
    }

    @Override
    public DereferenceResult dereference(DIDURL didurl, Map<String, Object> map) throws DereferencingException, ResolutionException {
        return null;
    }
}

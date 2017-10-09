package io.nuls.rpcserver.resources;

import io.nuls.global.NulsContext;
import io.nuls.rpcserver.entity.RpcResult;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Niels on 2017/9/30.
 * nuls.io
 */
@Path("/transaction")
public class TransactionResourceImpl implements TransactionResource {
    private NulsContext nulsContext = NulsContext.getInstance();
    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult query() {
        return RpcResult.getSuccess();
    }
}

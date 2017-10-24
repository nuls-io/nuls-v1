package io.nuls.rpc.resources.impl;


import io.nuls.core.context.NulsContext;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.resources.CreditResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Niels on 2017/9/30.
 * nuls.io
 */
@Path("/credit")
public class CreditResourceImpl implements CreditResource {

    private NulsContext context = NulsContext.getInstance();

    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult query() {
        return RpcResult.getSuccess();
    }
}

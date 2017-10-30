package io.nuls.rpc.resources.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.resources.ConsensusResource;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Niels on 2017/9/30.
 * nuls.io
 */
@Path("/consensus")
public class ConsensusResourceImpl implements ConsensusResource {
    private NulsContext context = NulsContext.getInstance();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getInfo() {
        return RpcResult.getSuccess();
    }

    @GET
    @Path("/condition")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getCondition() {
        return RpcResult.getSuccess();
    }

    @POST
    @Path("/in")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult in(String address, String password) {
        return RpcResult.getSuccess();
    }

    @POST
    @Path("/out")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult out(String address, String password) {
        return RpcResult.getSuccess();
    }

}

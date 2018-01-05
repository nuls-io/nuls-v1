package io.nuls.rpc.resources.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.resources.NodesResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Niels
 * @date 2017/9/30
 *
 */
@Path("/nodes")
public class NodesResourceImpl implements NodesResource {
    private NulsContext context = NulsContext.getInstance();

    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getList() {
        return RpcResult.getSuccess();
    }

    @Override
    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getCount() {
        return RpcResult.getSuccess();
    }

    @Override
    @GET
    @Path("/consensus")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getConsensusNodes() {
        return RpcResult.getSuccess();
    }


    @Override
    @GET
    @Path("/count/consensus")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getConsensusCount() {
        return RpcResult.getSuccess();
    }

    @Override
    @GET
    @Path("/groups")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getGroups() {
        return RpcResult.getSuccess();
    }
}

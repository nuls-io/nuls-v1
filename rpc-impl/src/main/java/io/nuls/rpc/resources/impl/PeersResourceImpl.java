package io.nuls.rpc.resources.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.resources.PeersResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Niels on 2017/9/30.
 *
 */
@Path("/peers")
public class PeersResourceImpl implements PeersResource {
    private NulsContext context = NulsContext.getInstance();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getList() {
        return RpcResult.getSuccess();
    }

    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getCount() {
        return RpcResult.getSuccess();
    }

    @GET
    @Path("/consensus")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getConsensusPeers() {
        return RpcResult.getSuccess();
    }


    @GET
    @Path("/count/consensus")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getConsensusCount() {
        return RpcResult.getSuccess();
    }

    @GET
    @Path("/groups")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getGroups() {
        return RpcResult.getSuccess();
    }
}

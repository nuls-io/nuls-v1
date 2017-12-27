package io.nuls.rpc.resources.impl;

import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.resources.BlockResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Niels
 * @date 2017/9/30
 *
 */
@Path("/block")
public class BlockResourceImpl implements BlockResource {
    @GET
    @Path("/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult loadBlock(@PathParam("hash") String hash) {
        return null;
    }
    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult getBlockCount() {
        return null;
    }
    @GET
    @Path("/bestheight")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult getBestBlockHeiht() {
        return null;
    }
    @GET
    @Path("/besthash")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult getBestBlockHash() {
        return null;
    }
    @GET
    @Path("/height/{height}/hash")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult getHashByHeight(@PathParam("height")Integer height) {
        return null;
    }
    @GET
    @Path("/height/{height}/header")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult getHeaderByHeight(@PathParam("height")Integer height) {
        return null;
    }
    @GET
    @Path("/{hash}/header")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult getHeader(@PathParam("hash")String hash) {
        return null;
    }
    @GET
    @Path("/height/{height}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult getBlock(@PathParam("height")Integer height) {
        return null;
    }
}

package io.nuls.rpc.resources.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.resources.AccountResource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by Niels on 2017/9/30.
 * nuls.io
 */
@Path("/account")
public class AccountResourceImpl implements AccountResource {

    private NulsContext context = NulsContext.getInstance();
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult create(@QueryParam("count") Integer count ) {
        return null;
    }
    @GET
    @Path("/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult load(@PathParam("address") String address) {
        return RpcResult.getSuccess();
    }

    @GET
    @Path("/{address}/balance")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult getBalance(@PathParam("address") String address) {
        return null;
    }

    @GET
    @Path("/{address}/credit")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult getCredit(@PathParam("address") String address) {
        return null;
    }

    @GET
    @Path("/{address}/prikey")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult getPrikey(@PathParam("address") String address, @QueryParam("password") String password) {
        return null;
    }

    @GET
    @Path("/address")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult getAddress(@QueryParam("publicKey") String publicKey, @QueryParam("subChainId") String subChainId) {
        return null;
    }


    @POST
    @Path("/lock")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult lock(@QueryParam("address") String address, @QueryParam("password") String password,
                          @QueryParam("amount") Double amount, @QueryParam("remark") String remark,
                          @QueryParam("unlockTime") String unlockTime) {
        return null;
    }

    @GET
    @Path("/address/{address}/validate")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult getAddress(@QueryParam("address") String address) {
        return null;
    }

}

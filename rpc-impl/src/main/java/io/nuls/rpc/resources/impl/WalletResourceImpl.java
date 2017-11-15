package io.nuls.rpc.resources.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.resources.WalletResouce;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;

/**
 * Created by Niels on 2017/9/30.
 *
 */
@Path("/wallet")
public class WalletResourceImpl implements WalletResouce {
    private NulsContext context = NulsContext.getInstance();

    @POST
    @Path("/lock")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult lock() {
        return RpcResult.getSuccess();
    }

    @POST
    @Path("/unlock")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult unlock(@FormParam("password") String password, @FormParam("unlockTime") String unlockTime) {
        return RpcResult.getSuccess();
    }

    @POST
    @Path("/password")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult password(@FormParam("password") String password) {
        return RpcResult.getSuccess();
    }

    @PUT
    @Path("/password")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult password(@FormParam("password") String password, @FormParam("newPassword") String newPassword) {
        return RpcResult.getSuccess();
    }

    @GET
    @Path("/account/list")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult accountList() {
        return RpcResult.getSuccess();
    }

    @POST
    @Path("/account/{address}/transfer")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult transfer(@FormParam("address") String address, @FormParam("password") String password, @FormParam("toAddress") String toAddress, @FormParam("amount") String amount, @FormParam("remark") String remark) {
        return RpcResult.getSuccess();
    }

    @POST
    @Path("/backup")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult backup(@FormParam("address") String address, @FormParam("password") String password) {
        return RpcResult.getSuccess();
    }

    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult importWallet(File file) {
        return RpcResult.getSuccess();
    }

}

package io.nuls.rpc.resources.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.resources.TransactionResource;
import io.nuls.rpc.resources.form.TxForm;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Niels
 * @date 2017/9/30
 *
 */
@Path("/tx")
public class TransactionResourceImpl implements TransactionResource {
    private NulsContext nulsContext = NulsContext.getInstance();

    @Override
    @GET
    @Path("/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult load(@PathParam("hash")String hash) {
        return RpcResult.getSuccess();
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult list(@QueryParam("accountAddress") String accountAddress,@QueryParam("type")  String type) {
        return null;
    }
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult create(TxForm form) {
        return null;
    }
}

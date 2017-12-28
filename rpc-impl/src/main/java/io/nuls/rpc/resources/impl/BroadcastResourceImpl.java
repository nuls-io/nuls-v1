package io.nuls.rpc.resources.impl;

import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.resources.BroadcastResource;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Niels
 * @date 2017/10/24
 *
 */
@Path("/broadcast")
public class BroadcastResourceImpl implements BroadcastResource {

    @Override
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult broadcast(String message) {
        return RpcResult.getSuccess();
    }
}

/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.rpc.resources.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.resources.WalletResouce;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;

/**
 *
 * @author Niels
 * @date 2017/9/30
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

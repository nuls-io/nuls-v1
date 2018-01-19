/**
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
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
import io.nuls.rpc.resources.AccountResource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Niels
 * @date 2017/9/30
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
    @Override
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

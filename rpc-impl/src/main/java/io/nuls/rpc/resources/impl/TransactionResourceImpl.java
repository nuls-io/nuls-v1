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

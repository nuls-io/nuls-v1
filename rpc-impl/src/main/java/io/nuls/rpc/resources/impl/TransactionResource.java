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

import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.rpc.entity.RpcResult;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author Niels
 * @date 2017/9/30
 */
@Path("/tx")
public class TransactionResource {
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);


    @GET
    @Path("/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult load(@PathParam("hash") String hash) {
        RpcResult result = RpcResult.getSuccess();
        result.setData(ledgerService.getTx(NulsDigestData.fromDigestHex(hash)));
        return result;
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)

    public RpcResult list(@QueryParam("address") String address, @QueryParam("type") int type
            , @QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        RpcResult result = RpcResult.getSuccess();
        List<Transaction> txList = null;
        try {
            txList = ledgerService.getTxList(address, type, pageNumber, pageSize);
            result.setData(txList);
        } catch (Exception e) {
            Log.error(e);
            return RpcResult.getFailed(e.getMessage());
        }
        return result;
    }

}

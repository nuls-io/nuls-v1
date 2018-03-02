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

import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.entity.TransactionDto;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
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
        RpcResult result = null;
        if (StringUtils.isBlank(hash)) {
            return RpcResult.getFailed(ErrorCode.NULL_PARAMETER);
        }
        try {
            Transaction tx = ledgerService.getTx(NulsDigestData.fromDigestHex(hash));
            if (tx == null) {
                result = RpcResult.getFailed("not found");
            } else {
                result = RpcResult.getSuccess();
                result.setData(new TransactionDto(tx));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)

    public RpcResult list(@QueryParam("address") String address, @QueryParam("type") int type
            , @QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        RpcResult result;
        System.out.println(address.length());
        if (StringUtils.isBlank(address) || address.length() > 35 || pageNumber < 0 || pageSize < 0) {
            result = RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
            return result;
        }
        if(pageNumber == 0) {
            pageNumber = 1;
        }
        if(pageSize == 0) {
            pageSize = 10;
        }

        try {
            List<Transaction> txList = ledgerService.getTxList(address, type, pageNumber, pageSize);
            if (txList == null || txList.isEmpty()) {
                return RpcResult.getSuccess();
            }
            List<TransactionDto> dtoList = new ArrayList<>();
            for (Transaction tx : txList) {
                dtoList.add(new TransactionDto(tx, address));
            }
            result = RpcResult.getSuccess();
            result.setData(dtoList);
        } catch (Exception e) {
            Log.error(e);
            return RpcResult.getFailed(e.getMessage());
        }
        return result;
    }

}

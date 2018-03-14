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

import io.nuls.account.service.intf.AccountService;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.UtxoOutputDataService;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.rpc.entity.OutputDto;
import io.nuls.rpc.entity.PageDto;
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
    private AccountService accountService = NulsContext.getServiceBean(AccountService.class);
    private UtxoOutputDataService outputDataService = NulsContext.getServiceBean(UtxoOutputDataService.class);

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

    public RpcResult list(@QueryParam("address") String address, @QueryParam("type") int type,
                          @QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        if (!StringUtils.isBlank(address) && !StringUtils.validAddress(address)) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }

        if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        if (pageNumber == 0) {
            pageNumber = 1;
        }
        if (pageSize == 0) {
            pageSize = 10;
        }

        RpcResult result;
        try {
            PageDto pageDto = new PageDto();
            pageDto.setPageNumber(pageNumber);
            pageDto.setPageSize(pageSize);

            long count = ledgerService.getTxCount(address, type);
            pageDto.setTotal(count);

            List<Transaction> txList = ledgerService.getTxList(address, type, pageNumber, pageSize);
            if (txList == null || txList.isEmpty()) {
                return RpcResult.getSuccess().setData(pageDto);
            }

            List<TransactionDto> dtoList = new ArrayList<>();
            for (Transaction tx : txList) {
                dtoList.add(new TransactionDto(tx, address));
            }
            pageDto.setList(dtoList);

            result = RpcResult.getSuccess();
            result.setData(dtoList);
        } catch (Exception e) {
            Log.error(e);
            return RpcResult.getFailed(e.getMessage());
        }
        return result;
    }

    @GET
    @Path("/block/list")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult list(@QueryParam("height") long height) {
        if(height < 0) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
     //   List<Transaction> txList = ledgerService.getTxList(height);
        return null;
    }

    @GET
    @Path("/locked")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult list(@QueryParam("address") String address, @QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        if (address != null && !StringUtils.validAddress(address)) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        if (pageNumber < 0 || pageSize < 0) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        if (StringUtils.isBlank(address)) {
            address = accountService.getDefaultAccount().getAddress().getBase58();
        }
        if (pageNumber == 0) {
            pageNumber = 1;
        }
        if (pageSize == 0) {
            pageSize = 1;
        }
        List<UtxoOutputPo> poList = outputDataService.getLockUtxo(address, TimeService.currentTimeMillis(), pageNumber, pageSize);
        List<OutputDto> dtoList = new ArrayList<>();
        for (UtxoOutputPo po : poList) {
            dtoList.add(new OutputDto(po));
        }
        RpcResult result = RpcResult.getSuccess();
        result.setData(dtoList);
        return result;
    }


}

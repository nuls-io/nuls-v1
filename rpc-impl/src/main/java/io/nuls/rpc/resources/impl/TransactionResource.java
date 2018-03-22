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
import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.UtxoOutputDataService;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.event.TransactionEvent;
import io.nuls.ledger.service.impl.LedgerCacheService;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.rpc.entity.OutputDto;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.entity.TransactionDto;
import io.nuls.rpc.resources.form.TxForm;

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

    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult forwardTransaction(TxForm form) {
        Transaction tx = null;
        try {
            tx = form.getTx();
        } catch (Exception e) {
            Log.error(e);
        }
        if (tx == null) {
            throw new NulsRuntimeException(ErrorCode.NULL_PARAMETER);
        }
        ValidateResult result = tx.verify();
        if (result.isFailed() && ErrorCode.ORPHAN_TX != result.getErrorCode()) {
            return RpcResult.getFailed(ErrorCode.DATA_ERROR);
        }
        TransactionEvent event = new TransactionEvent();
        event.setEventBody(tx);
        List<String> list = eventBroadcaster.broadcastAndCache(event, true);
        return RpcResult.getSuccess().setData(list);

    }

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
        } catch (NulsRuntimeException re) {
            Log.error(re);
            result = new RpcResult(false, re.getCode(), re.getMessage());
        } catch (Exception e) {
            Log.error(e);
            result = RpcResult.getFailed(ErrorCode.SYS_UNKOWN_EXCEPTION);
        }

        return result;
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)

    public RpcResult list(@QueryParam("address") String address, @QueryParam("type") int type,
                          @QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        if (type < 0 || pageNumber < 0 || pageSize < 0 || pageSize > 100) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        if (pageNumber == 0) {
            pageNumber = 1;
        }
        if (pageSize == 0) {
            pageSize = 10;
        }

        try {
            RpcResult result = RpcResult.getSuccess();

            Page<Transaction> pages = new Page<>();
            if (StringUtils.isBlank(address)) {
                pages = ledgerService.getTxList(-1, type, pageNumber, pageSize);
            } else if (StringUtils.validAddress(address)) {
                pages.setPageNumber(pageNumber);
                pages.setPageSize(pageSize);
                long count = ledgerService.getTxCount(address, type);
                pages.setTotal(count);
                if (count == 0) {
                    return RpcResult.getSuccess().setData(pages);
                }
                List<Transaction> txList = ledgerService.getTxList(address, type, pageNumber, pageSize);
                pages.setList(txList);
            }
            Page<TransactionDto> pageDto = new Page<>(pages);
            List<TransactionDto> dtoList = new ArrayList<>();
            for (Transaction tx : pages.getList()) {
                dtoList.add(new TransactionDto(tx, address));
            }
            pageDto.setList(dtoList);
            result.setData(pageDto);
            return result;
        } catch (Exception e) {
            Log.error(e);
            return RpcResult.getFailed(e.getMessage());
        }

    }

    @GET
    @Path("/block/list")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult list(@QueryParam("height") long height,
                          @QueryParam("pageNumber") int pageNumber,
                          @QueryParam("pageSize") int pageSize) {
        if (height < 0 || pageNumber < 0 || pageSize < 0) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        if (pageNumber == 0) {
            pageNumber = 1;
        }
        if (pageSize == 0) {
            pageSize = 20000;
        }

        Page<TransactionDto> dtoPage = null;
        try {
            Page<Transaction> txPage = ledgerService.getTxList(height, 0, pageNumber, pageSize);
            List<TransactionDto> dtoList = new ArrayList<>();
            dtoPage = new Page<>(txPage);
            for (Transaction tx : txPage.getList()) {
                dtoList.add(new TransactionDto(tx));
            }
            dtoPage.setList(dtoList);
        } catch (Exception e) {
            Log.error(e);
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }

        RpcResult result = RpcResult.getSuccess();
        result.setData(dtoPage);
        return result;
    }

    @GET
    @Path("/locked")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult list(@QueryParam("address") String address,
                          @QueryParam("pageNumber") int pageNumber,
                          @QueryParam("pageSize") int pageSize) {
        if (!StringUtils.validAddress(address)) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        if (pageNumber < 0 || pageSize < 0) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        if (pageNumber == 0) {
            pageNumber = 1;
        }
        if (pageSize == 0) {
            pageSize = 10;
        }
        //todo        ledgerService.getLockUtxo
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

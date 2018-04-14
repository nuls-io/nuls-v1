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

import io.nuls.account.entity.Address;
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
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.UtxoInput;
import io.nuls.ledger.entity.UtxoOutput;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.ledger.event.TransactionEvent;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.rpc.entity.OutputDto;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.entity.TransactionDto;
import io.nuls.rpc.resources.form.TxForm;
import io.swagger.annotations.Api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/9/30
 */
@Path("/tx")
@Api(value = "/browse", description = "Transaction")
public class TransactionResource {
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
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
            Transaction tx = ledgerService.getCacheTx(hash);
            if (tx == null) {
                tx = ledgerService.getTx(NulsDigestData.fromDigestHex(hash));
            }
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
    @Path("/bySpent/{fromHash}/{fromIndex}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult load(@PathParam("fromHash") String fromHash, @PathParam("fromIndex") int fromIndex) {
        RpcResult result = null;
        if (StringUtils.isBlank(fromHash) || fromIndex < 0 || fromIndex > 500) {
            return RpcResult.getFailed(ErrorCode.NULL_PARAMETER);
        }
        Transaction transaction = null;
        List<Transaction> txList = ledgerService.getCacheTxList(0);
        for (Transaction tx : txList) {
            if (tx instanceof AbstractCoinTransaction) {
                AbstractCoinTransaction atx = (AbstractCoinTransaction) tx;
                UtxoData coinData = (UtxoData) atx.getCoinData();
                for (UtxoInput input : coinData.getInputs()) {
                    if (fromHash.equals(input.getFromHash().getDigestHex()) && fromIndex == input.getFromIndex()) {
                        transaction = tx;
                        break;
                    }
                }
            }
            if (transaction != null) {
                break;
            }
        }
        if (transaction == null) {
            transaction = ledgerService.getTx(fromHash, fromIndex);
        }
        if (transaction == null) {
            return RpcResult.getFailed(ErrorCode.DATA_NOT_FOUND);
        }
        TransactionDto dto = new TransactionDto(transaction);
        return RpcResult.getSuccess().setData(dto);
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult list(@QueryParam("blockHeight") Long blockHeight,
                          @QueryParam("address") String address, @QueryParam("type") int type,
                          @QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        if (blockHeight == null && StringUtils.isBlank(address) && type == 0 && pageNumber == 0 && pageSize == 0) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        if ((blockHeight != null && blockHeight < 0) || type < 0 || pageNumber < 0 || pageSize < 0) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        if ((pageNumber == 0 && pageSize > 0) || (pageNumber > 0 && pageSize == 0)) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }

        try {
            RpcResult result = RpcResult.getSuccess();

            Page<Transaction> pages = new Page<>();
            if (StringUtils.isBlank(address)) {
                pages = ledgerService.getTxList(blockHeight, type, pageNumber, pageSize);
            } else if (Address.validAddress(address)) {

                long count = ledgerService.getTxCount(blockHeight, address, type);
                if (count < (pageNumber - 1) * pageSize) {
                    Page page = new Page(pageNumber, pageSize);
                    return result.setData(page);
                }

                if (pageSize > 0) {
                    pages.setPageNumber(pageNumber);
                    pages.setPageSize(pageSize);
                } else {
                    pages.setPageNumber(pageNumber);
                    pages.setPages((int) count);
                }

                pages.setTotal(count);
                if (count == 0) {
                    return RpcResult.getSuccess().setData(pages);
                }
                List<Transaction> txList = ledgerService.getTxList(blockHeight, address, type, pageNumber, pageSize);
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
    @Path("/locked")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult list(@QueryParam("address") String address,
                          @QueryParam("pageNumber") int pageNumber,
                          @QueryParam("pageSize") int pageSize) {
        if (!Address.validAddress(address)) {
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
        Page<UtxoOutput> page = ledgerService.getLockUtxo(address, pageNumber, pageSize);

        List<OutputDto> dtoList = new ArrayList<>();
        for (UtxoOutput po : page.getList()) {
            dtoList.add(new OutputDto(po));
        }
        RpcResult result = RpcResult.getSuccess();

        Page dtoPage = new Page(page);
        dtoPage.setList(dtoList);
        result.setData(dtoPage);
        return result;
    }


}

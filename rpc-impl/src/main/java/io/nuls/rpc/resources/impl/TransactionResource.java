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

import io.nuls.account.entity.Address;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.UtxoInput;
import io.nuls.ledger.entity.UtxoOutput;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.ledger.util.UtxoTransactionTool;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.TransactionEvent;
import io.nuls.protocol.model.NulsDigestData;
import io.nuls.protocol.model.Transaction;
import io.nuls.rpc.entity.OutputDto;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.entity.TransactionDto;
import io.nuls.rpc.resources.form.TxForm;
import io.swagger.annotations.*;

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
    @Path("/transaction")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "发送交易数据包 [3.5.4]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = Boolean.class)
    })
    public RpcResult forwardTransaction(@ApiParam(name = "form", value = "交易数据", required = true)  TxForm form) {
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
        boolean b = eventBroadcaster.publishToLocal(event);
        return RpcResult.getSuccess().setData(b);

    }

    @GET
    @Path("/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据hash查询交易 [3.5.1]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = TransactionDto.class)
    })
    public RpcResult load(@ApiParam(name="hash", value="交易hash", required = true)
                              @PathParam("hash") String hash) {
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
    @Path("/bySpent/{fromHash}/{fromIndex}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据已花费UTXO获取交易 [3.5.5]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = TransactionDto.class)
    })
    public RpcResult load(@ApiParam(name="fromHash", value="交易hash", required = true)
                              @PathParam("fromHash") String fromHash,
                          @ApiParam(name="fromIndex", value="索引", required = true)
                                @PathParam("fromIndex") int fromIndex) {
        RpcResult result = null;
        if (StringUtils.isBlank(fromHash) || fromIndex < 0 || fromIndex > 500) {
            return RpcResult.getFailed(ErrorCode.NULL_PARAMETER);
        }
        Transaction transaction = ledgerService.getTx(fromHash, fromIndex);
        if (transaction == null) {
            return RpcResult.getFailed(ErrorCode.DATA_NOT_FOUND);
        }
        TransactionDto dto = new TransactionDto(transaction);
        return RpcResult.getSuccess().setData(dto);
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询单个账户交易列表 [3.5.2]",
            notes = "result.data.page.list: List<TransactionDto>, pageNumber和pageSize要么同时缺省，" +
                    "要么都必须大于0，同时缺省时则返回查询到的所有结果, blockHeight, address, type至少有一个必填")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = TransactionDto.class)
    })
    public RpcResult list(@ApiParam(name="blockHeight", value="交易所在的区块高度，缺省时为所有区块")
                              @QueryParam("blockHeight") Long blockHeight,
                          @ApiParam(name="address", value="账户地址，缺省时为所有账户")
                                @QueryParam("address") String address,
                          @ApiParam(name="type", value="交易类型，缺省时默认为所有类型")
                              @QueryParam("type") int type,
                          @ApiParam(name="pageNumber", value="页码")
                              @QueryParam("pageNumber") int pageNumber,
                          @ApiParam(name="pageSize", value="每页条数")
                              @QueryParam("pageSize") int pageSize) {
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
    @ApiOperation(value = "查询我的锁定金额列表 [3.5.3]",
            notes = "result.data.page.list: List<OutputDto>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = OutputDto.class)
    })
    public RpcResult list(@ApiParam(name="address", value="账户地址")
                              @QueryParam("address") String address,
                          @ApiParam(name="pageNumber", value="页码")
                                @QueryParam("pageNumber") int pageNumber,
                          @ApiParam(name="pageSize", value="每页条数")
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

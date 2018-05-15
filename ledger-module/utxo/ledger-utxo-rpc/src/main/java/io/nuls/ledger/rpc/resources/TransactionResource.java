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
package io.nuls.ledger.rpc.resources;

import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.*;
import io.nuls.kernel.utils.VarInt;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.rpc.model.OutputDto;
import io.nuls.ledger.rpc.model.TransactionDto;
import io.nuls.ledger.service.LedgerService;
import io.nuls.ledger.storage.service.UtxoLedgerUtxoStorageService;
import io.nuls.ledger.utils.LedgerUtil;
import io.swagger.annotations.*;
import org.spongycastle.util.Arrays;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/5/14
 */
@Path("/tx")
@Api(value = "/transaction", description = "transaction")
@Component
public class TransactionResource {

    @Autowired
    private LedgerService ledgerService;
    @Autowired
    private UtxoLedgerUtxoStorageService utxoLedgerUtxoStorageService;

    @GET
    @Path("/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据hash查询交易")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = TransactionDto.class)
    })
    public Result<TransactionDto> getTxByHash(@ApiParam(name="hash", value="交易hash", required = true)
                          @PathParam("hash") String hash) {
        if (StringUtils.isBlank(hash)) {
            return Result.getFailed(LedgerErrorCode.NULL_PARAMETER);
        }
        Result result = null;
        try {
            Transaction tx = ledgerService.getTx(NulsDigestData.fromDigestHex(hash));
            if (tx == null) {
                result = Result.getFailed(LedgerErrorCode.DATA_NOT_FOUND);
            } else {
                TransactionDto txDto = null;
                CoinData coinData = tx.getCoinData();
                if(coinData != null) {
                    // 组装from数据
                    List<Coin> froms = coinData.getFrom();
                    if(froms != null && froms.size() > 0) {
                        byte[] fromHash, owner;
                        int fromIndex;
                        NulsDigestData fromHashObj;
                        Transaction fromTx;
                        Coin fromUtxo;
                        for(Coin from : froms) {
                            owner = from.getOwner();
                            // owner拆分出txHash和index
                            fromHash = LedgerUtil.getTxHashBytes(owner);
                            fromIndex = LedgerUtil.getIndex(owner);
                            // 查询from UTXO
                            fromHashObj = new NulsDigestData();
                            fromHashObj.parse(fromHash);
                            fromTx = ledgerService.getTx(fromHashObj);
                            fromUtxo = fromTx.getCoinData().getTo().get(fromIndex);
                            from.setFrom(fromUtxo);
                        }
                    }
                    txDto = new TransactionDto(tx);
                    List<OutputDto> outputDtoList = new ArrayList<>();
                    // 组装to数据
                    List<Coin> tos = coinData.getTo();
                    if(tos != null && tos.size() > 0) {
                        byte[] txHashBytes = tx.getHash().serialize();
                        String txHash = tx.getHash().getDigestHex();
                        OutputDto outputDto = null;
                        Coin to, temp;
                        for(int i = 0, length = tos.size(); i < length; i++) {
                            to = tos.get(i);
                            outputDto = new OutputDto(to);
                            outputDto.setTxHash(txHash);
                            outputDto.setIndex(i);
                            temp = utxoLedgerUtxoStorageService.getUtxo(Arrays.concatenate(txHashBytes, new VarInt(i).encode()));
                            if(temp == null) {
                                // 已花费
                                outputDto.setStatus(3);
                            } else {
                                if(temp.getLockTime() == -1) {
                                    // 参与共识锁定
                                    outputDto.setStatus(2);
                                } else if(TimeService.currentTimeMillis() < temp.getLockTime()) {
                                    // 高度锁定
                                    outputDto.setStatus(1);
                                } else {
                                    // 未花费
                                    outputDto.setStatus(0);
                                }
                            }
                            outputDtoList.add(outputDto);
                        }
                    }
                    txDto.setOutputs(outputDtoList);
                }
                result = Result.getSuccess();
                result.setData(txDto);
            }
        } catch (NulsRuntimeException re) {
            Log.error(re);
            result = new Result(false, re.getCode(), re.getMessage());
        } catch (Exception e) {
            Log.error(e);
            result = Result.getFailed(LedgerErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return result;
    }

}

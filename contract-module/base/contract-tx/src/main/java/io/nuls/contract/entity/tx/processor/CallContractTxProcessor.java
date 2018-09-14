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
package io.nuls.contract.entity.tx.processor;

import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.dto.ContractResult;
import io.nuls.contract.dto.ContractTokenTransferInfoPo;
import io.nuls.contract.entity.tx.CallContractTransaction;
import io.nuls.contract.entity.tx.ContractTransferTransaction;
import io.nuls.contract.entity.txdata.CallContractData;
import io.nuls.contract.helper.VMHelper;
import io.nuls.contract.ledger.service.ContractUtxoService;
import io.nuls.contract.service.ContractService;
import io.nuls.contract.storage.po.ContractAddressInfoPo;
import io.nuls.contract.storage.service.ContractAddressStorageService;
import io.nuls.contract.storage.service.ContractTokenTransferStorageService;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.TransactionProcessor;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.VarInt;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.service.LedgerService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/8
 */
@Component
public class CallContractTxProcessor implements TransactionProcessor<CallContractTransaction> {

    @Autowired
    private VMHelper vmHelper;

    @Autowired
    private ContractAddressStorageService contractAddressStorageService;

    @Autowired
    private ContractTokenTransferStorageService contractTokenTransferStorageService;

    @Autowired
    private ContractService contractService;

    @Autowired
    private ContractUtxoService contractUtxoService;

    @Autowired
    private LedgerService ledgerService;

    @Override
    public Result onRollback(CallContractTransaction tx, Object secondaryData) {
        try {
            // 回滚代币转账交易
            byte[] txHashBytes = null;
            try {
                txHashBytes = tx.getHash().serialize();
            } catch (IOException e) {
                Log.error(e);
            }
            CallContractData txData = tx.getTxData();
            byte[] sender = txData.getSender();
            byte[] contractAddress = txData.getContractAddress();
            Result<ContractAddressInfoPo> contractAddressInfo = contractAddressStorageService.getContractAddressInfo(contractAddress);
            ContractAddressInfoPo po = contractAddressInfo.getData();
            if(po != null && po.isNrc20()) {
                ContractResult contractResult = tx.getContractResult();
                if(contractResult == null) {
                    contractResult = contractService.getContractExecuteResult(tx.getHash());
                }
                if(contractResult != null) {
                    // 处理合约执行失败 - 没有transferEvent的情况, 直接从数据库中删除
                    if(!contractResult.isSuccess()) {
                        if(ContractUtil.isTransferMethod(txData.getMethodName())) {
                            contractTokenTransferStorageService.deleteTokenTransferInfo(ArraysTool.concatenate(txData.getSender(), txHashBytes, new VarInt(0).encode()));
                        }
                    }
                    List<String> events = contractResult.getEvents();
                    int size = events.size();
                    // 目前只处理Transfer事件
                    String event;
                    if(events != null && size > 0) {
                        for(int i = 0; i < size; i++) {
                            event = events.get(i);
                            // 按照NRC20标准，TransferEvent事件中第一个参数是转出地址-from，第二个参数是转入地址-to, 第三个参数是金额
                            ContractTokenTransferInfoPo tokenTransferInfoPo = ContractUtil.convertJsonToTokenTransferInfoPo(event);
                            if(tokenTransferInfoPo == null) {
                                continue;
                            }
                            byte[] from = tokenTransferInfoPo.getFrom();
                            byte[] to = tokenTransferInfoPo.getTo();
                            contractTokenTransferStorageService.deleteTokenTransferInfo(ArraysTool.concatenate(from, txHashBytes, new VarInt(i).encode()));
                            contractTokenTransferStorageService.deleteTokenTransferInfo(ArraysTool.concatenate(to, txHashBytes, new VarInt(i).encode()));
                        }
                    }
                }
            }

            // 删除子交易从全网账本
            // 删除子交易从合约账本
            Collection<ContractTransferTransaction> contractTransferTxs = tx.getContractTransferTxs();
            if(contractTransferTxs != null && contractTransferTxs.size() > 0) {
                List<ContractTransferTransaction> contractTransferTxList = new ArrayList<>(contractTransferTxs);
                Collections.reverse(contractTransferTxList);
                for(ContractTransferTransaction transferTx : contractTransferTxList) {
                    try {
                        Result result = ledgerService.rollbackTx(transferTx);
                        if(result.isFailed()) {
                            Log.error("rollback contract transfer tx to ledger error. msg: {}", result.getMsg());
                            return result;
                        }
                        result = contractService.rollbackContractTransferTx(transferTx);
                        if(result.isFailed()) {
                            Log.error("rollback contract transfer tx to contract ledger error. msg: {}", result.getMsg());
                            return Result.getFailed();
                        }

                    } catch (Exception e) {
                        Log.error("rollback contract transfer tx error. msg: {}", e.getMessage());
                        return Result.getFailed();
                    }
                }
            }

            // 删除合约调用交易的UTXO
            contractUtxoService.deleteUtxoOfTransaction(tx);

            // 删除合约执行结果
            contractService.deleteContractExecuteResult(tx.getHash());
        } catch (Exception e) {
            Log.error("rollback call contract tx error.", e);
            return Result.getFailed();
        }
        return Result.getSuccess();
    }

    @Override
    public Result onCommit(CallContractTransaction tx, Object secondaryData) {
        try {
            // 保存合约执行结果
            ContractResult contractResult = tx.getContractResult();
            contractService.saveContractExecuteResult(tx.getHash(), contractResult);

            // 保存调用合约交易的UTXO
            Result utxoResult = contractUtxoService.saveUtxoForContractAddress(tx);
            if (utxoResult.isFailed()) {
                Log.error("save confirmed contract utxo error, reason is {}.", utxoResult.getMsg());
                return utxoResult;
            }

            // 保存子交易到全网账本
            // 保存子交易合约账本
            Collection<ContractTransferTransaction> contractTransferTxs = tx.getContractTransferTxs();
            if(contractTransferTxs != null && contractTransferTxs.size() > 0) {

                for(ContractTransferTransaction transferTx : contractTransferTxs) {
                    try {
                        Result result = ledgerService.saveTx(transferTx);
                        if(result.isFailed()) {
                            Log.error("save contract transfer tx to ledger error. msg: {}", result.getMsg());
                            return result;
                        }
                        result = contractService.saveContractTransferTx(transferTx);
                        if(result.isFailed()) {
                            Log.error("save contract transfer tx to contract ledger error. msg: {}", result.getMsg());
                            return Result.getFailed();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.error("save contract transfer tx error. msg: {}", e.getMessage());
                        return Result.getFailed();
                    }
                }


            }

            // 保存代币交易
            CallContractData callContractData = tx.getTxData();
            byte[] contractAddress = callContractData.getContractAddress();
            byte[] sender = callContractData.getSender();

            Result<ContractAddressInfoPo> contractAddressInfoPoResult = contractAddressStorageService.getContractAddressInfo(contractAddress);
            if(contractAddressInfoPoResult.isFailed()) {
                return contractAddressInfoPoResult;
            }
            ContractAddressInfoPo contractAddressInfoPo = contractAddressInfoPoResult.getData();
            if(contractAddressInfoPo == null) {
                return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST);
            }
            contractResult.setNrc20(contractAddressInfoPo.isNrc20());

            BlockHeader blockHeader = tx.getBlockHeader();
            byte[] newestStateRoot = blockHeader.getStateRoot();

            // 处理合约执行失败 - 没有transferEvent的情况, 直接从数据库中获取, 若是本地创建的交易，获取到修改为失败交易
            if(!contractResult.isSuccess()) {
                if(contractAddressInfoPo != null && contractAddressInfoPo.isNrc20() && ContractUtil.isTransferMethod(callContractData.getMethodName())) {
                    byte[] txHashBytes = tx.getHash().serialize();
                    byte[] infoKey = ArraysTool.concatenate(callContractData.getSender(), txHashBytes, new VarInt(0).encode());
                    Result<ContractTokenTransferInfoPo> infoResult = contractTokenTransferStorageService.getTokenTransferInfo(infoKey);
                    ContractTokenTransferInfoPo po = infoResult.getData();
                    if(po != null) {
                        po.setStatus((byte) 2);
                        contractTokenTransferStorageService.saveTokenTransferInfo(infoKey, po);

                        // 刷新token余额
                        String contractAddressStr = AddressTool.getStringAddressByBytes(contractAddress);
                        if(po.getFrom() != null) {
                            vmHelper.refreshTokenBalance(newestStateRoot, contractAddressInfoPo, AddressTool.getStringAddressByBytes(po.getFrom()), contractAddressStr);
                        }
                        if(po.getTo() != null) {
                            vmHelper.refreshTokenBalance(newestStateRoot, contractAddressInfoPo, AddressTool.getStringAddressByBytes(po.getTo()), contractAddressStr);
                        }
                    }
                }
            }

            // 处理合约事件

            vmHelper.dealEvents(newestStateRoot, tx, contractResult, contractAddressInfoPo);

        } catch (Exception e) {
            Log.error("save call contract tx error.", e);
            return Result.getFailed();
        }
        return Result.getSuccess();
    }

    @Override
    public ValidateResult conflictDetect(List<Transaction> txList) {
        return ValidateResult.getSuccessResult();
    }
}

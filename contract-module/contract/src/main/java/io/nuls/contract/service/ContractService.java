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
package io.nuls.contract.service;

import io.nuls.contract.dto.ContractResult;
import io.nuls.contract.dto.ContractTokenInfo;
import io.nuls.contract.dto.ContractTokenTransferInfoPo;
import io.nuls.contract.dto.ContractTransfer;
import io.nuls.contract.entity.tx.ContractTransferTransaction;
import io.nuls.kernel.model.*;
import io.nuls.kernel.validate.ValidateResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ContractService {


    /**
     * 是否为合约地址
     *
     * @param addressBytes
     * @return
     */
    boolean isContractAddress(byte[] addressBytes);


    /**
     * 保存 txInfo : key -> contractAddress + txHash, status is confirmed
     * 保存 UTXO : key -> txHash + index
     *
     * @param txs
     * @return
     */
    Result<Integer> saveConfirmedTransactionList(List<Transaction> txs);

    /**
     * 回滚合约交易
     *
     * @param txs
     * @return
     */
    Result<Integer> rollbackTransactionList(List<Transaction> txs);


    /**
     * 保存合约执行结果
     *
     * @param hash
     * @param contractResult
     * @return
     */
    Result saveContractExecuteResult(NulsDigestData hash, ContractResult contractResult);

    /**
     * 删除合约执行结果
     *
     * @param hash
     * @return
     */
    Result deleteContractExecuteResult(NulsDigestData hash);

    /**
     * 获取合约执行结果
     *
     * @param hash
     * @return
     */
    ContractResult getContractExecuteResult(NulsDigestData hash);


    /**
     * 执行合约
     *
     * @param tx
     * @param height
     * @param stateRoot
     * @return
     */
    Result<ContractResult> invokeContract(Transaction tx, long height, byte[] stateRoot);

    /**
     * 打包或者验证区块时，创建合约临时余额区
     *
     */
    void createContractTempBalance();

    /**
     * 移除合约临时余额区
     *
     */
    void removeContractTempBalance();

    /**
     * 获取账户下指定合约的token余额
     *
     * @param address
     * @param contractAddress
     * @return
     */
    Result<ContractTokenInfo> getContractTokenViaVm(String address, String contractAddress);

    /**
     * 初始化账户下所有合约token
     *
     * @param address
     * @param contractAddress
     * @return
     */
    Result initAllTokensByAccount(String address);

    /**
     * 获取账户下代币资产列表
     *
     * @param address
     * @return
     */
    Result<List<ContractTokenInfo>> getAllTokensByAccount(String address);

    /**
     * 是否为Token合约地址
     *
     * @param address
     * @return
     */
    boolean isTokenContractAddress(String address);

    /**
     * 获取账户下Token转账列表
     *
     * @param address
     * @return
     */
    Result<List<ContractTokenTransferInfoPo>> getTokenTransferInfoList(String address);

    /**
     * 获取账户指定交易下Token转账列表
     *
     * @param address
     * @param hash
     * @return
     */
    Result<List<ContractTokenTransferInfoPo>> getTokenTransferInfoList(String address, NulsDigestData hash);

    Result<byte[]> handleContractResult(Transaction tx, ContractResult contractResult, byte[] stateRoot, long time, Map<String,Coin> toMaps, Map<String, Coin> contractUsedCoinMap);

    Result saveContractTransferTx(ContractTransferTransaction transferTx);
    Result rollbackContractTransferTx(ContractTransferTransaction transferTx);

    Result<byte[]> verifyContractResult(Transaction tx, ContractResult contractResult, byte[] stateRoot, long time, Map<String,Coin> toMaps, Map<String,Coin> contractUsedCoinMap);
    Result<byte[]> verifyContractResult(Transaction tx, ContractResult contractResult, byte[] stateRoot, long time, Map<String,Coin> toMaps, Map<String,Coin> contractUsedCoinMap, Long blockHeight);

    Result<byte[]> processTxs(List<Transaction> txs, long bestHeight, Block block, byte[] stateRoot, Map<String,Coin> toMaps, Map<String,Coin> contractUsedCoinMap);
}

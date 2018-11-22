/*
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
 *
 */
package io.nuls.contract.service.impl;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.ledger.model.CoinDataResult;
import io.nuls.account.ledger.model.TransactionInfo;
import io.nuls.account.service.AccountService;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.dto.ContractResult;
import io.nuls.contract.dto.ContractTokenInfo;
import io.nuls.contract.dto.ContractTokenTransferInfoPo;
import io.nuls.contract.dto.ContractTransfer;
import io.nuls.contract.entity.tx.*;
import io.nuls.contract.entity.txdata.*;
import io.nuls.contract.helper.VMHelper;
import io.nuls.contract.ledger.manager.ContractBalanceManager;
import io.nuls.contract.ledger.service.ContractTransactionInfoService;
import io.nuls.contract.ledger.service.ContractUtxoService;
import io.nuls.contract.ledger.util.ContractLedgerUtil;
import io.nuls.contract.service.ContractService;
import io.nuls.contract.storage.po.ContractAddressInfoPo;
import io.nuls.contract.storage.po.TransactionInfoPo;
import io.nuls.contract.storage.service.ContractAddressStorageService;
import io.nuls.contract.storage.service.ContractExecuteResultStorageService;
import io.nuls.contract.storage.service.ContractTokenTransferStorageService;
import io.nuls.contract.storage.service.ContractTransferTransactionStorageService;
import io.nuls.contract.util.ContractCoinComparator;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.program.*;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.map.MapUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.SignatureUtil;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.VarInt;
import io.nuls.ledger.util.LedgerUtil;
import io.nuls.protocol.constant.ProtocolConstant;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.nuls.ledger.util.LedgerUtil.asBytes;
import static io.nuls.ledger.util.LedgerUtil.asString;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date:
 */
@Service
public class ContractServiceImpl implements ContractService, InitializingBean {

    @Autowired
    private ContractTransactionInfoService contractTransactionInfoService;

    @Autowired
    private ContractUtxoService contractUtxoService;

    @Autowired
    private ContractTransferTransactionStorageService contractTransferTransactionStorageService;

    @Autowired
    private ContractAddressStorageService contractAddressStorageService;

    @Autowired
    private ContractExecuteResultStorageService contractExecuteResultStorageService;

    @Autowired
    private ContractBalanceManager contractBalanceManager;

    @Autowired
    private ContractTokenTransferStorageService contractTokenTransferStorageService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private VMHelper vmHelper;

    @Autowired
    private VMContext vmContext;

    private ProgramExecutor programExecutor;

    private Lock lock = new ReentrantLock();

    private ThreadLocal<ProgramExecutor> localProgramExecutor = new ThreadLocal<>();

    @Override
    public void afterPropertiesSet() throws NulsException {
        programExecutor = vmHelper.getProgramExecutor();
    }

    /**
     *
     * @param executor
     * @param number        当前块编号
     * @param prevStateRoot 上一区块状态根
     * @param create        创建智能合约的参数
     * @return
     */
    private Result<ContractResult> createContract(ProgramExecutor executor, long number, byte[] prevStateRoot, CreateContractData create) {
        if(number < 0) {
            return Result.getFailed(ContractErrorCode.PARAMETER_ERROR);
        }

        // 不能同时为空，批量提交方式stateRoot为空
        if(executor == null && prevStateRoot == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        try {
            byte[] contractAddress = create.getContractAddress();
            byte[] sender = create.getSender();
            long price = create.getPrice();
            ProgramCreate programCreate = new ProgramCreate();
            programCreate.setContractAddress(contractAddress);
            programCreate.setSender(sender);
            programCreate.setValue(BigInteger.ZERO);
            programCreate.setPrice(price);
            programCreate.setGasLimit(create.getGasLimit());
            programCreate.setNumber(number);
            programCreate.setContractCode(create.getCode());
            programCreate.setArgs(create.getArgs());

            ProgramExecutor track;
            if(executor == null) {
                track = programExecutor.begin(prevStateRoot);
            } else {
                track = executor.startTracking();
            }
            ProgramResult programResult = track.create(programCreate);
            // 批量提交方式，交易track放置到外部处理合约执行结果的方法里去提交
            if(executor == null) {
                track.commit();
            }

            // current state root
            byte[] stateRoot = executor == null ? track.getRoot() : null;
            ContractResult contractResult = new ContractResult();
            contractResult.setNonce(programResult.getNonce());
            contractResult.setGasUsed(programResult.getGasUsed());
            contractResult.setPrice(price);
            contractResult.setStateRoot(stateRoot);
            contractResult.setBalance(programResult.getBalance());
            contractResult.setContractAddress(contractAddress);
            contractResult.setSender(sender);
            contractResult.setRemark(ContractConstant.CREATE);
            // 批量提交方式，交易track放置到外部处理合约执行结果的方法里去提交
            contractResult.setTxTrack(track);

            if(!programResult.isSuccess()) {
                Result<ContractResult> result = Result.getFailed(ContractErrorCode.CONTRACT_EXECUTE_ERROR);
                contractResult.setError(programResult.isError());
                contractResult.setRevert(programResult.isRevert());
                contractResult.setErrorMessage(programResult.getErrorMessage());
                contractResult.setStackTrace(programResult.getStackTrace());
                result.setMsg(programResult.getErrorMessage());
                result.setData(contractResult);
                return result;
            }

            // 返回已使用gas、状态根、消息事件、合约转账(从合约转出)
            contractResult.setError(false);
            contractResult.setRevert(false);
            contractResult.setEvents(programResult.getEvents());
            contractResult.setTransfers(generateContractTransfer(programResult.getTransfers()));

            Result<ContractResult> result = Result.getSuccess();
            result.setData(contractResult);
            return result;
        } catch (Exception e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.CONTRACT_EXECUTE_ERROR);
            result.setMsg(e.getMessage());
            return result;
        }
    }

    private List<ContractTransfer> generateContractTransfer(List<ProgramTransfer> transfers) {
        if(transfers == null || transfers.size() == 0) {
            return new ArrayList<>(0);
        }
        List<ContractTransfer> resultList = new ArrayList<>(transfers.size());
        ContractTransfer contractTransfer;
        for(ProgramTransfer transfer : transfers) {
            contractTransfer = new ContractTransfer();
            contractTransfer.setFrom(transfer.getFrom());
            contractTransfer.setTo(transfer.getTo());
            contractTransfer.setValue(Na.valueOf(transfer.getValue().longValue()));
            contractTransfer.setFee(Na.ZERO);
            resultList.add(contractTransfer);
        }
        return resultList;
    }

    /**
     *
     * @param executor
     * @param number        当前块编号
     * @param prevStateRoot 上一区块状态根
     * @param call          调用智能合约的参数
     * @return
     */
    private Result<ContractResult> callContract(ProgramExecutor executor, long number, byte[] prevStateRoot, CallContractData call) {
        if(number < 0) {
            return Result.getFailed(ContractErrorCode.PARAMETER_ERROR);
        }
        if(executor == null && prevStateRoot == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        try {
            byte[] contractAddress = call.getContractAddress();
            byte[] sender = call.getSender();
            long price = call.getPrice();
            ProgramCall programCall = new ProgramCall();
            programCall.setContractAddress(contractAddress);
            programCall.setSender(sender);
            programCall.setValue(BigInteger.valueOf(call.getValue()));
            programCall.setPrice(price);
            programCall.setGasLimit(call.getGasLimit());
            programCall.setNumber(number);
            programCall.setMethodName(call.getMethodName());
            programCall.setMethodDesc(call.getMethodDesc());
            programCall.setArgs(call.getArgs());

            ProgramExecutor track;
            if(executor == null) {
                track = programExecutor.begin(prevStateRoot);
            } else {
                track = executor.startTracking();
            }

            ProgramResult programResult = track.call(programCall);

            // 批量提交方式，交易track放置到外部处理合约执行结果的方法里去提交
            if(executor == null) {
                track.commit();
            }

            // current state root
            byte[] stateRoot = executor == null ? track.getRoot() : null;
            ContractResult contractResult = new ContractResult();

            contractResult.setNonce(programResult.getNonce());
            contractResult.setGasUsed(programResult.getGasUsed());
            contractResult.setPrice(price);
            contractResult.setStateRoot(stateRoot);
            contractResult.setBalance(programResult.getBalance());
            contractResult.setContractAddress(contractAddress);
            contractResult.setSender(sender);
            contractResult.setValue(programCall.getValue().longValue());
            contractResult.setRemark(ContractConstant.CALL);
            // 批量提交方式，交易track放置到外部处理合约执行结果的方法里去提交
            contractResult.setTxTrack(track);

            if(!programResult.isSuccess()) {
                Result<ContractResult> result = Result.getFailed(ContractErrorCode.CONTRACT_EXECUTE_ERROR);
                contractResult.setError(programResult.isError());
                contractResult.setRevert(programResult.isRevert());
                contractResult.setErrorMessage(programResult.getErrorMessage());
                contractResult.setStackTrace(programResult.getStackTrace());
                result.setMsg(programResult.getErrorMessage());
                result.setData(contractResult);
                return result;
            }

            // 返回调用结果、已使用Gas、状态根、消息事件、合约转账(从合约转出)等
            contractResult.setError(false);
            contractResult.setRevert(false);
            contractResult.setResult(programResult.getResult());
            contractResult.setEvents(programResult.getEvents());
            contractResult.setTransfers(generateContractTransfer(programResult.getTransfers()));

            Result<ContractResult> result = Result.getSuccess();
            result.setData(contractResult);
            return result;
        } catch (Exception e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.CONTRACT_EXECUTE_ERROR);
            result.setMsg(e.getMessage());
            return result;
        }
    }

    /**
     *
     * @param executor
     * @param number        当前块编号
     * @param prevStateRoot 上一区块状态根
     * @param delete        删除智能合约的参数
     * @return
     */
    private Result<ContractResult> deleteContract(ProgramExecutor executor, long number, byte[] prevStateRoot, DeleteContractData delete) {
        if(number < 0) {
            return Result.getFailed(ContractErrorCode.PARAMETER_ERROR);
        }
        if(executor == null && prevStateRoot == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        try {
            byte[] contractAddress = delete.getContractAddress();
            byte[] sender = delete.getSender();
            ProgramExecutor track;

            if(executor == null) {
                track = programExecutor.begin(prevStateRoot);
            } else {
                track = executor.startTracking();
            }
            ProgramResult programResult = track.stop(contractAddress, sender);
            // 批量提交方式，交易track放置到外部处理合约执行结果的方法里去提交
            if(executor == null) {
                track.commit();
            }

            // current state root
            byte[] stateRoot = executor == null ? track.getRoot() : null;
            ContractResult contractResult = new ContractResult();
            contractResult.setNonce(programResult.getNonce());
            contractResult.setGasUsed(programResult.getGasUsed());
            contractResult.setStateRoot(stateRoot);
            contractResult.setBalance(programResult.getBalance());
            contractResult.setContractAddress(contractAddress);
            contractResult.setSender(sender);
            contractResult.setRemark(ContractConstant.DELETE);
            // 批量提交方式，交易track放置到外部处理合约执行结果的方法里去提交
            contractResult.setTxTrack(track);

            if(!programResult.isSuccess()) {
                Result<ContractResult> result = Result.getFailed(ContractErrorCode.CONTRACT_EXECUTE_ERROR);
                contractResult.setError(programResult.isError());
                contractResult.setRevert(programResult.isRevert());
                contractResult.setErrorMessage(programResult.getErrorMessage());
                contractResult.setStackTrace(programResult.getStackTrace());
                result.setMsg(programResult.getErrorMessage());
                result.setData(contractResult);
                return result;
            }

            // 返回状态根
            contractResult.setError(false);
            contractResult.setRevert(false);

            Result<ContractResult> result = Result.getSuccess();
            result.setData(contractResult);
            return result;
        } catch (Exception e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.CONTRACT_EXECUTE_ERROR);
            result.setMsg(e.getMessage());
            return result;
        }
    }

    @Override
    public boolean isContractAddress(byte[] addressBytes) {
        return ContractLedgerUtil.isExistContractAddress(addressBytes);
    }

    private Result<Integer> saveConfirmedTransaction(Transaction tx) {
        if (tx == null) {
            Log.error("save confirmed contract tx error, tx is null.");
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }

        // 获取tx中是智能合约地址列表
        List<byte[]> addresses = ContractLedgerUtil.getRelatedAddresses(tx);

        // 合约账本不处理非合约相关交易
        if (addresses == null || addresses.size() == 0) {
            return Result.getSuccess().setData(new Integer(0));
        }

        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        txInfoPo.setStatus(TransactionInfo.CONFIRMED);

        Result result = contractTransactionInfoService.saveTransactionInfo(txInfoPo, addresses);
        if (result.isFailed()) {
            Log.error("save confirmed contract transactionInfo error, reason is {}.", result.getMsg());
            return result;
        }

        // 保存非合约交易转入合约地址的utxo
        if(tx.getType() != ContractConstant.TX_TYPE_CALL_CONTRACT) {
            result = contractUtxoService.saveUtxoForContractAddress(tx);
            if (result.isFailed()) {
                Log.error("save confirmed non-call-contract transfer utxo error, reason is {}.", result.getMsg());
                return result;
            }
        }

        result.setData(new Integer(1));
        return result;
    }

    @Override
    public Result saveContractTransferTx(ContractTransferTransaction tx) {
        Result result = contractUtxoService.saveUtxoForContractAddress(tx);
        if (result.isFailed()) {
            Log.error("save contract transfer utxo error, reason is {}.", result.getMsg());
            return result;
        }

        result = contractTransferTransactionStorageService.saveContractTransferTx(tx.getHash(), tx);
        if (result.isFailed()) {
            Log.error("save contract transfer tx error, reason is {}.", result.getMsg());
            contractUtxoService.deleteUtxoOfTransaction(tx);
            return result;
        }
        return result;
    }

    @Override
    public Result rollbackContractTransferTx(ContractTransferTransaction tx) {
        Result result = contractTransferTransactionStorageService.deleteContractTransferTx(tx.getHash());
        if (result.isFailed()) {
            Log.error("rollback contract transfer tx error, reason is {}.", result.getMsg());
            return result;
        }
        result = contractUtxoService.deleteUtxoOfTransaction(tx);
        if (result.isFailed()) {
            contractTransferTransactionStorageService.saveContractTransferTx(tx.getHash(), tx);
            Log.error("rollback contract transfer utxo error, reason is {}.", result.getMsg());
            return result;
        }
        return result;
    }

    @Override
    public Result<Integer> saveConfirmedTransactionList(List<Transaction> txs) {
        List<Transaction> savedTxList = new ArrayList<>();
        Result result;
        for (int i = 0; i < txs.size(); i++) {
            result = saveConfirmedTransaction(txs.get(i));
            if (result.isSuccess()) {
                if(result.getData() != null && (int) result.getData() == 1) {
                    savedTxList.add(txs.get(i));
                }
            } else {
                rollbackTransactionList(savedTxList);
                return result;
            }
        }
        return Result.getSuccess().setData(savedTxList.size());
    }

    @Override
    public Result<Integer> rollbackTransactionList(List<Transaction> txs) {
        // 回滚确认交易
        for (int i = txs.size() - 1; i >= 0; i--) {
            rollbackTransaction(txs.get(i));
        }
        return Result.getSuccess().setData(new Integer(txs.size()));
    }

    private Result<Integer> rollbackTransaction(Transaction tx) {

        // 获取tx中是智能合约地址的地址列表
        List<byte[]> addresses = ContractLedgerUtil.getRelatedAddresses(tx);

        if (addresses == null || addresses.size() == 0) {
            return Result.getSuccess().setData(new Integer(0));
        }

        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        // 删除合约地址相关交易 - TransactionInfo
        Result result = contractTransactionInfoService.deleteTransactionInfo(txInfoPo, addresses);

        if (result.isFailed()) {
            return result;
        }

        // 删除非合约交易转入合约地址的utxo
        if(tx.getType() != ContractConstant.TX_TYPE_CALL_CONTRACT) {
            result = contractUtxoService.deleteUtxoOfTransaction(tx);
            if (result.isFailed()) {
                Log.error("rollback non-call-contract transfer utxo error, reason is {}.", result.getMsg());
                return result;
            }
        }

        return result;
    }

    @Override
    public Result saveContractExecuteResult(NulsDigestData hash, ContractResult result) {
        if (hash == null || result == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        vmHelper.updateLastedPriceForAccount(result.getSender(), result.getPrice());
        return contractExecuteResultStorageService.saveContractExecuteResult(hash, result);
    }

    @Override
    public Result deleteContractExecuteResult(NulsDigestData hash) {
        if (hash == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        return contractExecuteResultStorageService.deleteContractExecuteResult(hash);
    }

    @Override
    public ContractResult getContractExecuteResult(NulsDigestData hash) {
        if (hash == null) {
            return null;
        }
        return contractExecuteResultStorageService.getContractExecuteResult(hash);
    }

    /**
     * @param from
     * @param to
     * @param values
     * @param fee
     * @param isSendBack
     * @param orginHash
     * @param blockTime
     * @param toMaps
     * @param contractUsedCoinMap 组装时不操作toMaps, 用 contractUsedCoinMap 来检查UTXO是否已经被使用，如果已使用则跳过
     *                            (合约转账(从合约转出)不同于普通转账，普通转账有本地账本来维护UTXO，并且在打包前已经组装好UTXO，
     *                              而合约转账(从合约转出)是在打包时才组装，此时从合约账本[DB]及打包交易[toMaps]中拿到所有的utxo来组装，
     *                              用 contractUsedCoinMap 来代表已使用的UTXO)
     * @param bestHeight
     * @return
     */
    private Result<ContractTransferTransaction> transfer(byte[] from, byte[] to, Na values, Na fee, boolean isSendBack, NulsDigestData orginHash, long blockTime,
                                                         Map<String, Coin> toMaps,
                                                         Map<String, Coin> contractUsedCoinMap, Long bestHeight) {
        try {
            if(!ContractLedgerUtil.isExistContractAddress(from)) {
                return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST);
            }

            ContractTransferTransaction tx = new ContractTransferTransaction();
            tx.setTime(blockTime);

            CoinData coinData = new CoinData();
            List<Coin> tos = coinData.getTo();
            Coin toCoin = new Coin(to, values.subtract(fee));
            tos.add(toCoin);

            // 加入toMaps、contractUsedCoinMap组装UTXO
            // 组装时不操作toMaps, 用 contractUsedCoinMap 来检查UTXO是否已经被使用，如果已使用则跳过
            CoinDataResult coinDataResult = getContractSpecialTransferCoinData(from, values, toMaps, contractUsedCoinMap, bestHeight);
            if (!coinDataResult.isEnough()) {
                return Result.getFailed(TransactionErrorCode.INSUFFICIENT_BALANCE);
            }
            coinData.setFrom(coinDataResult.getCoinList());
            if (coinDataResult.getChange() != null) {
                tos.add(coinDataResult.getChange());
            }

            tx.setCoinData(coinData);
            byte successByte;
            byte[] remark = null;
            if(isSendBack) {
                successByte = 0;
                remark = ContractConstant.SEND_BACK_REMARK.getBytes(NulsConfig.DEFAULT_ENCODING);
            } else {
                successByte = 1;
            }
            tx.setRemark(remark);
            tx.setTxData(new ContractTransferData(orginHash, from, successByte));
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));

            // 维护合约地址连续转出交易的未花费UTXO
            byte[] txBytes = tx.getHash().serialize();
            for (int i = 0, size = tos.size(); i < size; i++) {
                if (toMaps != null) {
                    toMaps.put(LedgerUtil.asString(ArraysTool.concatenate(txBytes, new VarInt(i).encode())), tos.get(i));
                }
            }

            // 合约转账(从合约转出)交易不需要签名
            return Result.getSuccess().setData(tx);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @param address
     * @param amount
     * @param bestHeight
     * @return
     * @throws NulsException
     */
    public CoinDataResult getContractSpecialTransferCoinData(byte[] address, Na amount, Map<String, Coin> toMaps, Map<String, Coin> contractUsedCoinMap, Long bestHeight) {
        lock.lock();
        try {
            CoinDataResult coinDataResult = new CoinDataResult();
            List<Coin> coinList = contractBalanceManager.getCoinListByAddress(address);
            //pierre add contract coin key
            Set<Map.Entry<String, Coin>> toMapsEntries = toMaps.entrySet();
            Coin toCoin;
            Coin cloneCoin;
            String key;
            for(Map.Entry<String, Coin> toMapsEntry : toMapsEntries) {
                key = toMapsEntry.getKey();
                toCoin = toMapsEntry.getValue();
                if (Arrays.equals(toCoin.getAddress(), address) && !contractUsedCoinMap.containsKey(key)) {
                    cloneCoin = new Coin(asBytes(key), toCoin.getNa(), toCoin.getLockTime());
                    cloneCoin.setFrom(toCoin);
                    cloneCoin.setKey(key);
                    coinList.add(cloneCoin);
                }
            }

            if (coinList.isEmpty()) {
                coinDataResult.setEnough(false);
                return coinDataResult;
            }

            // 将所有余额从小到大排序
            Collections.sort(coinList, ContractCoinComparator.getInstance());

            boolean enough = false;
            List<Coin> coins = new ArrayList<>();
            Na values = Na.ZERO;
            Coin coin;
            // 将所有余额从小到大排序后，累计未花费的余额
            for (int i = 0, length = coinList.size(); i < length; i++) {
                coin = coinList.get(i);
                if(bestHeight != null) {
                    if (!coin.usable(bestHeight)) {
                        continue;
                    }
                } else {
                    if (!coin.usable()) {
                        continue;
                    }
                }
                if (coin.getNa().equals(Na.ZERO)) {
                    continue;
                }
                // for contract, 使用过的UTXO不能再使用
                if(StringUtils.isNotBlank(coin.getKey())) {
                    if(contractUsedCoinMap.containsKey(coin.getKey())) {
                        continue;
                    }
                    contractUsedCoinMap.put(coin.getKey(), coin);
                }
                coins.add(coin);

                // 每次累加一条未花费余额
                values = values.add(coin.getNa());
                if (values.isGreaterOrEquals(amount)) {
                    // 余额足够后，需要判断是否找零
                    Na change = values.subtract(amount);
                    if (change.isGreaterThan(Na.ZERO)) {
                        Coin changeCoin = new Coin();
                        changeCoin.setOwner(address);
                        changeCoin.setNa(change);
                        coinDataResult.setChange(changeCoin);
                    }
                    enough = true;
                    coinDataResult.setEnough(true);
                    coinDataResult.setFee(Na.ZERO);
                    coinDataResult.setCoinList(coins);
                    break;
                }
            }
            if (!enough) {
                coinDataResult.setEnough(false);
                return coinDataResult;
            }
            return coinDataResult;
        } finally {
            lock.unlock();
        }
    }

    private Result<ContractResult> invokeContract(ProgramExecutor track, Transaction tx, long height, byte[] stateRoot, boolean isForkChain) {
        if(tx == null || height < 0) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }
        int txType = tx.getType();

        // 分叉链切换主链时，重新执行智能合约
        if(!isForkChain) {
            ContractTransaction contractTx = (ContractTransaction) tx;
            // 打包、验证区块，合约只执行一次
            ContractResult contractExecutedResult;
            contractExecutedResult = contractTx.getContractResult();
            if(contractExecutedResult == null) {
                contractExecutedResult = getContractExecuteResult(tx.getHash());
                if(contractExecutedResult != null) {
                    if(Log.isDebugEnabled()) {
                        Log.debug("===get ContractResult from db.");
                    }
                }
            } else {
                if(Log.isDebugEnabled()) {
                    Log.debug("===get ContractResult from tx object.");
                }
            }
            if(contractExecutedResult != null) {
                contractExecutedResult.setTxTrack(track);
                if(contractExecutedResult.isSuccess()) {
                    // 刷新临时余额
                    if(contractExecutedResult.isSuccess()) {
                        if(tx instanceof CallContractTransaction) {
                            this.refreshTempBalance((CallContractTransaction) tx, contractExecutedResult, height);
                        }
                    } else {
                        // 合约调用失败，把需要退还的UTXO记录到结果对象中
                        contractExecutedResult.setValue(((ContractData) tx.getTxData()).getValue());
                    }
                    return Result.getSuccess().setData(contractExecutedResult);
                } else {
                    if(Log.isDebugEnabled()) {
                        Log.debug("contractExecutedResult failed. {}", contractExecutedResult.toString());
                    }
                    return Result.getFailed().setData(contractExecutedResult);
                }
            }
        }

        if (txType == ContractConstant.TX_TYPE_CREATE_CONTRACT) {
            CreateContractTransaction createContractTransaction = (CreateContractTransaction) tx;
            CreateContractData createContractData = createContractTransaction.getTxData();
            if(!ContractUtil.checkPrice(createContractData.getPrice())) {
                return Result.getFailed(ContractErrorCode.CONTRACT_MINIMUM_PRICE);
            }
            Result<ContractResult> result = createContract(track, height, stateRoot, createContractData);
            ContractResult contractResult = result.getData();
            if (contractResult != null && contractResult.isSuccess()) {
                Result nrc20Result = vmHelper.validateNrc20Contract((ProgramExecutor) contractResult.getTxTrack(), createContractTransaction, contractResult);
                if(nrc20Result.isFailed()) {
                    contractResult.setError(true);
                    if(ContractErrorCode.CONTRACT_NRC20_SYMBOL_FORMAT_INCORRECT.equals(nrc20Result.getErrorCode())) {
                        contractResult.setErrorMessage("The format of the symbol is incorrect.");
                    } else if(ContractErrorCode.CONTRACT_NAME_FORMAT_INCORRECT.equals(nrc20Result.getErrorCode())) {
                        contractResult.setErrorMessage("The format of the name is incorrect.");
                    } else if(ContractErrorCode.CONTRACT_NRC20_MAXIMUM_DECIMALS.equals(nrc20Result.getErrorCode())) {
                        contractResult.setErrorMessage("The value of decimals ranges from 0 to 18.");
                    } else if(ContractErrorCode.CONTRACT_NRC20_MAXIMUM_TOTAL_SUPPLY.equals(nrc20Result.getErrorCode())) {
                        contractResult.setErrorMessage("The value of totalSupply ranges from 1 to 2^256 - 1.");
                    } else {
                        contractResult.setErrorMessage("Unkown error.");
                    }
                    result.setData(contractResult);
                }
            }
            return result;
        } else if(txType == ContractConstant.TX_TYPE_CALL_CONTRACT) {
            CallContractTransaction callContractTransaction = (CallContractTransaction) tx;
            CallContractData callContractData = callContractTransaction.getTxData();
            if(!ContractUtil.checkPrice(callContractData.getPrice())) {
                return Result.getFailed(ContractErrorCode.CONTRACT_MINIMUM_PRICE);
            }
            Result<ContractResult> result = callContract(track, height, stateRoot, callContractData);
            byte[] contractAddress = callContractData.getContractAddress();
            BigInteger preBalance = vmContext.getBalance(contractAddress, height);
            ContractResult contractResult = result.getData();
            if(!contractResult.isSuccess()) {
                if(Log.isDebugEnabled()) {
                    Log.debug("contractResult failed. {}", contractResult.toString());
                }
            }
            contractResult.setPreBalance(preBalance);
            // 刷新临时余额
            if(result.isSuccess()) {
                this.refreshTempBalance(callContractTransaction, contractResult, height);
            } else {
                // 合约调用失败，把需要退还的UTXO记录到结果对象中
                contractResult.setValue(callContractData.getValue());
            }
            return result;
        } else if(txType == ContractConstant.TX_TYPE_DELETE_CONTRACT) {
            DeleteContractTransaction deleteContractTransaction = (DeleteContractTransaction) tx;
            DeleteContractData deleteContractData = deleteContractTransaction.getTxData();
            Result<ContractResult> result = deleteContract(track, height, stateRoot, deleteContractData);
            return result;
        } else {
            return Result.getSuccess();
        }
    }

    private void refreshTempBalance(CallContractTransaction callContractTransaction, ContractResult contractExecutedResult, Long height) {
        CallContractData callContractData = callContractTransaction.getTxData();
        byte[] contractAddress = callContractData.getContractAddress();
        BigInteger preBalance = vmContext.getBalance(contractAddress, height);
        contractExecutedResult.setPreBalance(preBalance);
        // 增加转入
        long value = callContractData.getValue();
        if(value > 0) {
            contractBalanceManager.addTempBalance(contractAddress, Na.valueOf(value));
        }
        // 增加转入, 扣除转出
        List<ContractTransfer> transfers = contractExecutedResult.getTransfers();
        if(transfers != null && transfers.size() > 0) {
            LinkedHashMap<String, Na>[] contracts = this.filterContractNa(transfers);
            LinkedHashMap<String, Na> contractOutNa = contracts[0];
            LinkedHashMap<String, Na> contractInNa = contracts[1];
            byte[] contractBytes;
            Set<Map.Entry<String, Na>> ins = contractInNa.entrySet();
            for(Map.Entry<String, Na> in : ins) {
                contractBytes = asBytes(in.getKey());
                vmContext.getBalance(contractBytes, height);
                contractBalanceManager.addTempBalance(contractBytes, in.getValue());
            }
            Set<Map.Entry<String, Na>> outs = contractOutNa.entrySet();
            for(Map.Entry<String, Na> out : outs) {
                contractBytes = asBytes(out.getKey());
                vmContext.getBalance(contractBytes, height);
                contractBalanceManager.minusTempBalance(contractBytes, out.getValue());
            }
        }
    }

    private LinkedHashMap<String, Na>[] filterContractNa(List<ContractTransfer> transfers) {
        LinkedHashMap<String, Na> contractOutNa = MapUtil.createLinkedHashMap(4);
        LinkedHashMap<String, Na> contractInNa = MapUtil.createLinkedHashMap(4);
        LinkedHashMap<String, Na>[] contracts = new LinkedHashMap[2];
        contracts[0] = contractOutNa;
        contracts[1] = contractInNa;

        byte[] from,to;
        Na transferValue;
        for(ContractTransfer transfer : transfers) {
            from = transfer.getFrom();
            to = transfer.getTo();
            transferValue = transfer.getValue();
            if(ContractUtil.isLegalContractAddress(from)) {
                String contract = asString(from);
                Na na = contractOutNa.get(contract);
                if(na == null) {
                    contractOutNa.put(contract, transferValue);
                } else {
                    contractOutNa.put(contract, na.add(transferValue));
                }
            }
            if(ContractUtil.isLegalContractAddress(to)) {
                String contract = asString(to);
                Na na = contractInNa.get(contract);
                if(na == null) {
                    contractInNa.put(contract, transferValue);
                } else {
                    contractInNa.put(contract, na.add(transferValue));
                }
            }
        }
        return contracts;
    }



    private void rollbackContractTempBalance(Transaction tx, ContractResult contractResult) {
        if(tx != null && tx.getType() == ContractConstant.TX_TYPE_CALL_CONTRACT) {
            CallContractTransaction callContractTransaction = (CallContractTransaction) tx;
            CallContractData callContractData = callContractTransaction.getTxData();
            byte[] contractAddress = callContractData.getContractAddress();
            // 增加转出, 扣除转入
            List<ContractTransfer> transfers = contractResult.getTransfers();
            if(transfers != null && transfers.size() > 0) {
                LinkedHashMap<String, Na>[] contracts = this.filterContractNa(transfers);
                LinkedHashMap<String, Na> contractOutNa = contracts[0];
                LinkedHashMap<String, Na> contractInNa = contracts[1];
                byte[] contractBytes;
                Set<Map.Entry<String, Na>> outs = contractOutNa.entrySet();
                for(Map.Entry<String, Na> out : outs) {
                    contractBytes = asBytes(out.getKey());
                    contractBalanceManager.addTempBalance(contractBytes, out.getValue());
                }
                Set<Map.Entry<String, Na>> ins = contractInNa.entrySet();
                for(Map.Entry<String, Na> in : ins) {
                    contractBytes = asBytes(in.getKey());
                    contractBalanceManager.minusTempBalance(contractBytes, in.getValue());
                }
            }
            // 扣除转入
            long value = callContractData.getValue();
            if(value > 0) {
                contractBalanceManager.minusTempBalance(contractAddress, Na.valueOf(value));
            }
        }
    }

    @Override
    public void createContractTempBalance() {
        contractBalanceManager.createTempBalanceMap();
    }

    @Override
    public void removeContractTempBalance() {
        contractBalanceManager.removeTempBalanceMap();
    }

    private void rollbackContractTransferTxs(Map<String, ContractTransferTransaction> successContractTransferTxs, Map<String, Coin> toMaps, Map<String, Coin> contractUsedCoinMap) {
        if(successContractTransferTxs != null && successContractTransferTxs.size() > 0) {
            Collection<ContractTransferTransaction> values = successContractTransferTxs.values();
            for(Transaction tx : values) {
                rollbackToMapAndContractUsedCoinMap(tx, toMaps, contractUsedCoinMap);
            }
        }
    }

    private void rollbackToMapAndContractUsedCoinMap(Transaction tx, Map<String, Coin> toMaps, Map<String,Coin> contractUsedCoinMap) {
        if (tx == null || tx.getCoinData() == null || contractUsedCoinMap == null) {
            return;
        }
        CoinData coinData = tx.getCoinData();
        List<Coin> froms = coinData.getFrom();
        List<Coin> tos = coinData.getTo();
        String key;
        for (Coin from : froms) {
            key = from.getKey();
            if(key != null) {
                contractUsedCoinMap.remove(from.getKey());
            }
        }
        try {
            byte[] txHashBytes = tx.getHash().serialize();
            for (int i = 0, size = tos.size(); i < size; i++) {
                toMaps.remove(LedgerUtil.asString(ArraysTool.concatenate(txHashBytes, new VarInt(i).encode())));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Result<ContractTransferTransaction> createContractTransferTx(ContractTransfer transfer, long blockTime, Map<String, Coin> toMaps, Map<String, Coin> contractUsedCoinMap, Long bestHeight) {
        Result<ContractTransferTransaction> result;
        result = transfer(transfer.getFrom(), transfer.getTo(), transfer.getValue(), transfer.getFee(), transfer.isSendBack(), transfer.getOrginHash(), blockTime, toMaps, contractUsedCoinMap, bestHeight);
        if(result.isSuccess()) {
            result.getData().setTransfer(transfer);
        } else {
            Log.error("contract transfer failed. Reason: " + result);
        }
        return result;
    }

    private List<ContractTransfer> createReturnFundsContractTransfer(Transaction tx, Na sendBack) {
        // 合约执行失败而退回的转入金额暂时不收取手续费
        Na transferFee = Na.ZERO;

        if(sendBack.compareTo(transferFee) <= 0) {
            transferFee = sendBack;
        }

        List<ContractTransfer> contractTransferList = new ArrayList<>();
        try {
            Set<String> addressFromTX = SignatureUtil.getAddressFromTX(tx);
            if(addressFromTX == null || addressFromTX.size() == 0) {
                return contractTransferList;
            }
            Object[] array = addressFromTX.toArray();
            String fromAddress = (String) array[0];
            byte[] fromAddressBytes = AddressTool.getAddress(fromAddress);
            CoinData coinData = tx.getCoinData();
            List<Coin> toList = coinData.getTo();
            HashMap<String, Na> sendBackMap = MapUtil.createHashMap(toList.size());
            String ownerStr;
            for(Coin coin : toList) {
                if(!ArraysTool.arrayEquals(fromAddressBytes, coin.getOwner())) {
                    ownerStr = AddressTool.getStringAddressByBytes(coin.getOwner());
                    Na addressNa = sendBackMap.get(ownerStr);
                    if(addressNa == null) {
                        sendBackMap.put(ownerStr, coin.getNa());
                    } else {
                        sendBackMap.put(ownerStr, addressNa.add(coin.getNa()));
                    }
                }
            }
            if(sendBackMap.size() > 0) {
                for(Map.Entry<String, Na> entry : sendBackMap.entrySet()) {
                    ContractTransfer transfer = new ContractTransfer(AddressTool.getAddress(entry.getKey()), fromAddressBytes, entry.getValue(), transferFee, true);
                    contractTransferList.add(transfer);
                }
            }

        } catch (NulsException e) {
            Log.error(e);
        }
        return contractTransferList;
    }

    @Override
    public Result<ContractTokenInfo> getContractTokenViaVm(String address, String contractAddress) {
        try {
            if (StringUtils.isBlank(contractAddress) || StringUtils.isBlank(address)) {
                return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
            }

            if (!AddressTool.validAddress(contractAddress) || !AddressTool.validAddress(address)) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
            }

            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            Result<ContractAddressInfoPo> contractAddressInfoResult = contractAddressStorageService.getContractAddressInfo(contractAddressBytes);
            ContractAddressInfoPo po = contractAddressInfoResult.getData();
            if(po == null) {
                return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST);
            }
            if(!po.isNrc20()) {
                return Result.getFailed(ContractErrorCode.CONTRACT_NOT_NRC20);
            }

            ProgramResult programResult = vmHelper.invokeViewMethod(contractAddressBytes, "balanceOf", null, address);
            Result<ContractTokenInfo> result;
            if(!programResult.isSuccess()) {
                result = Result.getFailed(ContractErrorCode.DATA_ERROR);
                result.setMsg(ContractUtil.simplifyErrorMsg(programResult.getErrorMessage()));
            } else {
                result = Result.getSuccess();
                ContractTokenInfo tokenInfo = new ContractTokenInfo(contractAddress, po.getNrc20TokenName(), po.getDecimals(), new BigInteger(programResult.getResult()), po.getNrc20TokenSymbol(), po.getBlockHeight());
                byte[] prevStateRoot = ContractUtil.getStateRoot(NulsContext.getInstance().getBestBlock().getHeader());
                ProgramExecutor track = programExecutor.begin(prevStateRoot);
                tokenInfo.setStatus(track.status(AddressTool.getAddress(tokenInfo.getContractAddress())).ordinal());
                result.setData(tokenInfo);
            }
            return result;
        } catch (Exception e) {
            Log.error("get contract token via VM error.", e);
            return Result.getFailed(ContractErrorCode.SYS_UNKOWN_EXCEPTION);
        }

    }

    @Override
    public Result initAllTokensByAccount(String address) {
        try {
            if (StringUtils.isBlank(address)) {
                return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
            }

            if (!AddressTool.validAddress(address)) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
            }
            contractBalanceManager.initAllTokensByAccount(address);
            return Result.getSuccess();
        } catch (Exception e) {
            Log.error("initial all tokens of the account error.", e);
            return Result.getFailed(ContractErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @Override
    public Result<List<ContractTokenInfo>> getAllTokensByAccount(String address) {
        try {
            if (StringUtils.isBlank(address)) {
                return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
            }

            if (!AddressTool.validAddress(address)) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
            }

            Result<List<ContractTokenInfo>> tokenListResult = contractBalanceManager.getAllTokensByAccount(address);
            List<ContractTokenInfo> list = tokenListResult.getData();
            if(list != null && list.size() > 0) {
                byte[] prevStateRoot = ContractUtil.getStateRoot(NulsContext.getInstance().getBestBlock().getHeader());
                ProgramExecutor track = programExecutor.begin(prevStateRoot);
                for(ContractTokenInfo tokenInfo : list) {
                    tokenInfo.setStatus(track.status(AddressTool.getAddress(tokenInfo.getContractAddress())).ordinal());
                }
            }
            return tokenListResult;
        } catch (Exception e) {
            Log.error("initial all tokens of the account error.", e);
            return Result.getFailed(ContractErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @Override
    public boolean isTokenContractAddress(String contractAddress) {
        try {
            if (StringUtils.isBlank(contractAddress)) {
                return false;
            }

            if (!AddressTool.validAddress(contractAddress)) {
                return false;
            }

            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            Result<ContractAddressInfoPo> contractAddressInfoResult = contractAddressStorageService.getContractAddressInfo(contractAddressBytes);
            ContractAddressInfoPo po = contractAddressInfoResult.getData();
            if(po == null) {
                return false;
            }
            return po.isNrc20();
        } catch (Exception e) {
            Log.error("check if it is a token address error.", e);
            return false;
        }
    }

    @Override
    public Result<List<ContractTokenTransferInfoPo>> getTokenTransferInfoList(String address) {
        try {
            Result accountResult = accountService.getAccount(address);
            if (accountResult.isFailed()) {
                return accountResult;
            }
            byte[] addressBytes = AddressTool.getAddress(address);

            List<ContractTokenTransferInfoPo> tokenTransferInfoListByAddress = contractTokenTransferStorageService.getTokenTransferInfoListByAddress(addressBytes);
            return Result.getSuccess().setData(tokenTransferInfoListByAddress);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed();
        }
    }

    @Override
    public Result<List<ContractTokenTransferInfoPo>> getTokenTransferInfoList(String address, NulsDigestData hash) {
        try {
            if(hash == null) {
                return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
            }
            Result accountResult = accountService.getAccount(address);
            if (accountResult.isFailed()) {
                return accountResult;
            }
            byte[] addressBytes = AddressTool.getAddress(address);

            List<ContractTokenTransferInfoPo> tokenTransferInfoListByAddress = contractTokenTransferStorageService.getTokenTransferInfoListByAddress(addressBytes, hash.serialize());
            return Result.getSuccess().setData(tokenTransferInfoListByAddress);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed();
        }
    }

    @Override
    public Result<byte[]> handleContractResult(Transaction tx, ContractResult contractResult, byte[] stateRoot, long time, Map<String, Coin> toMaps, Map<String, Coin> contractUsedCoinMap) {
        if (contractResult == null) {
            return Result.getSuccess().setData(stateRoot);
        }
        List<ContractTransfer> transfers = contractResult.getTransfers();
        byte[] preStateRoot = stateRoot;
        stateRoot = contractResult.getStateRoot();
        if(tx instanceof CallContractTransaction) {
            // 合约调用失败，且调用者存在资金转入合约地址，创建一笔合约转账(从合约转出)，退回这笔资金
            if (!contractResult.isSuccess() && contractResult.getValue() > 0) {
                Na sendBack = Na.valueOf(contractResult.getValue());
                List<ContractTransfer> transfer = this.createReturnFundsContractTransfer(tx, sendBack);
                transfers.addAll(transfer);
            }

            // 处理合约转账(从合约转出)交易，若处理成功，则提交这笔交易(前提是合约执行成功)
            stateRoot = this.handleContractTransferTxs((CallContractTransaction) tx, contractResult, stateRoot, preStateRoot,
                    transfers, time, toMaps, contractUsedCoinMap, null);

        } else {
            // 提交这笔交易
            Object txTrackObj = contractResult.getTxTrack();
            if(contractResult.isSuccess() && txTrackObj != null && txTrackObj instanceof ProgramExecutor) {
                ProgramExecutor txTrack = (ProgramExecutor) txTrackObj;
                if(Log.isDebugEnabled()) {
                    Log.debug("===tx track commit.");
                }
                txTrack.commit();
            }
        }

        // 这笔交易的合约执行结果保存在DB中, 另外保存在交易对象中，用于计算退还剩余的Gas，以CoinBase交易的方式退还 --> method: addConsensusTx
        ContractTransaction contractTx = (ContractTransaction) tx;
        contractTx.setContractResult(contractResult);
        return Result.getSuccess().setData(stateRoot);
    }

    private Result verifyTransfer(List<ContractTransfer> transfers) {
        if(transfers == null || transfers.size() == 0) {
            return Result.getSuccess();
        }
        for(ContractTransfer transfer : transfers) {
            if (transfer.getValue().isLessThan(ProtocolConstant.MININUM_TRANSFER_AMOUNT)) {
                return Result.getFailed(TransactionErrorCode.TOO_SMALL_AMOUNT);
            }
        }
        return Result.getSuccess();
    }

    private byte[] handleContractTransferTxs(CallContractTransaction tx, ContractResult contractResult,
                                             byte[] stateRoot, byte[] preStateRoot,
                                             List<ContractTransfer> transfers, long time,
                                             Map<String,Coin> toMaps, Map<String,Coin> contractUsedCoinMap, Long blockHeight) {
        boolean isCorrectContractTransfer = true;
        // 创建合约转账(从合约转出)交易
        if (transfers != null && transfers.size() > 0) {

            // 用于保存本次交易产生的合约转账(从合约转出)交易
            Map<String, ContractTransferTransaction> successContractTransferTxs = new LinkedHashMap<>();
            Result<ContractTransferTransaction> contractTransferResult;
            do {
                // 验证合约转账(从合约转出)交易的最小转移金额
                Result result = this.verifyTransfer(transfers);
                if(result.isFailed()) {
                    isCorrectContractTransfer = false;
                    contractResult.setError(true);
                    String errorMsg = contractResult.getErrorMessage();
                    errorMsg = errorMsg == null ? result.getErrorCode().getEnMsg() : (errorMsg + "," + result.getErrorCode().getEnMsg());
                    contractResult.setErrorMessage(errorMsg);
                    break;
                }

                // 合约转账(从合约转出)使用的交易时间为区块时间
                ContractTransferTransaction contractTransferTx;
                for (ContractTransfer transfer : transfers) {
                    // 保存外部合约交易hash
                    transfer.setOrginHash(tx.getHash());
                    contractTransferResult = this.createContractTransferTx(transfer, time, toMaps, contractUsedCoinMap, blockHeight);
                    if (contractTransferResult.isFailed()) {
                        this.rollbackContractTransferTxs(successContractTransferTxs, toMaps, contractUsedCoinMap);
                        isCorrectContractTransfer = false;
                        contractResult.setError(true);
                        String errorMsg = contractResult.getErrorMessage();
                        errorMsg = errorMsg == null ? contractTransferResult.getMsg() : (errorMsg + "," + contractTransferResult.getMsg());
                        contractResult.setErrorMessage(errorMsg);
                        break;
                    }
                    contractTransferTx = contractTransferResult.getData();
                    // 保存内部转账交易hash
                    transfer.setHash(contractTransferTx.getHash());
                    successContractTransferTxs.put(contractTransferTx.getHash().getDigestHex(), contractTransferTx);
                }
            } while (false);


            // 如果合约转账(从合约转出)出现错误，整笔合约交易视作合约执行失败
            if (!isCorrectContractTransfer) {
                Log.error("contract transfer execution failed, reason: {}", contractResult.getErrorMessage());
                // 执行合约产生的状态根回滚到上一个世界状态
                stateRoot = preStateRoot;
                contractResult.setStateRoot(stateRoot);
                // 余额还原到上一次的余额
                contractResult.setBalance(contractResult.getPreBalance());
                // 清空成功转账列表
                successContractTransferTxs.clear();
                // 回滚临时余额
                this.rollbackContractTempBalance(tx, contractResult);
                // 清空内部转账列表
                transfers.clear();

                // 合约转账(从合约转出)交易失败，且调用者存在资金转入合约地址，创建一笔合约转账(从合约转出)，退回这笔资金
                if (contractResult.getValue() > 0) {

                    Na sendBack = Na.valueOf(contractResult.getValue());
                    List<ContractTransfer> transferList = this.createReturnFundsContractTransfer(tx, sendBack);
                    for(ContractTransfer transfer : transferList) {
                        transfer.setOrginHash(tx.getHash());
                        contractTransferResult = this.createContractTransferTx(transfer, time, toMaps, contractUsedCoinMap, blockHeight);

                        if (contractTransferResult.isFailed()) {
                            successContractTransferTxs.clear();
                            contractResult.setErrorMessage(contractResult.getErrorMessage() + ", " + contractTransferResult.getMsg());
                            break;
                        } else {
                            ContractTransferTransaction _contractTransferTx = contractTransferResult.getData();
                            // 保存内部转账交易hash和外部合约交易hash
                            transfer.setHash(_contractTransferTx.getHash());
                            transfers.add(transfer);
                            successContractTransferTxs.put(_contractTransferTx.getHash().getDigestHex(), _contractTransferTx);
                        }
                    }
                }
            }
            // 保存内部转账子交易到父交易对象中
            tx.setContractTransferTxs(successContractTransferTxs.values());
        }

        // 合约执行成功并且合约转账(从合约转出)执行成功，提交这笔交易
        if(contractResult.isSuccess() && isCorrectContractTransfer) {
            Object txTrackObj = contractResult.getTxTrack();
            if(txTrackObj != null && txTrackObj instanceof ProgramExecutor) {
                ProgramExecutor txTrack = (ProgramExecutor) txTrackObj;
                if(Log.isDebugEnabled()) {
                    Log.debug("===tx track commit.");
                }
                txTrack.commit();
            }
        }
        return stateRoot;
    }

    @Override
    public Result<byte[]> verifyContractResult(Transaction tx, ContractResult contractResult, byte[] stateRoot, long time, Map<String, Coin> toMaps, Map<String, Coin> contractUsedCoinMap) {
        return this.verifyContractResult(tx, contractResult, stateRoot, time, toMaps, contractUsedCoinMap, null);
    }

    @Override
    public Result<byte[]> verifyContractResult(Transaction tx, ContractResult contractResult, byte[] stateRoot, long time, Map<String, Coin> toMaps, Map<String, Coin> contractUsedCoinMap, Long blockHeight) {
        if (contractResult == null) {
            return Result.getSuccess().setData(stateRoot);
        }
        List<ContractTransfer> transfers = contractResult.getTransfers();
        byte[] preStateRoot = stateRoot;
        stateRoot = contractResult.getStateRoot();
        do {
            if(tx instanceof CallContractTransaction) {
                // 合约调用失败，且调用者存在资金转入合约地址，创建一笔合约转账(从合约转出)，退回这笔资金
                if (!contractResult.isSuccess() && contractResult.getValue() > 0) {
                    // 智能合约在打包节点上只执行一次(打包区块和验证区块), 打包节点在打包时已处理过contractResult, 不重复处理
                    if (transfers.size() == 0) {
                        Na sendBack = Na.valueOf(contractResult.getValue());
                        List<ContractTransfer> transfer = this.createReturnFundsContractTransfer(tx, sendBack);
                        transfers.addAll(transfer);
                    }
                }
                // 处理合约转账(从合约转出)交易，若处理成功，则提交这笔交易(前提是合约执行成功)
                stateRoot = this.handleContractTransferTxs((CallContractTransaction) tx, contractResult, stateRoot, preStateRoot,
                        transfers, time, toMaps, contractUsedCoinMap, blockHeight);
            } else {
                // 提交这笔交易
                Object txTrackObj = contractResult.getTxTrack();
                if(contractResult.isSuccess() && txTrackObj != null && txTrackObj instanceof ProgramExecutor) {
                    ProgramExecutor txTrack = (ProgramExecutor) txTrackObj;
                    if(Log.isDebugEnabled()) {
                        Log.debug("===tx track commit.");
                    }
                    txTrack.commit();
                }
            }
        } while (false);

        // 这笔交易的合约执行结果保存在DB中, 另外保存在交易对象中，用于计算退还剩余的Gas，以CoinBase交易的方式退还 --> method: addConsensusTx
        ContractTransaction contractTx = (ContractTransaction) tx;
        contractTx.setContractResult(contractResult);
        return Result.getSuccess().setData(stateRoot);
    }

    @Override
    public Result<ContractResult> batchPackageTx(Transaction tx, long bestHeight, Block block, byte[] stateRoot, Map<String, Coin> toMaps, Map<String, Coin> contractUsedCoinMap) {
        if(stateRoot == null) {
            return Result.getFailed();
        }

        BlockHeader blockHeader = block.getHeader();
        long blockTime = blockHeader.getTime();
        ProgramExecutor executor = localProgramExecutor.get();
        if(executor == null) {
            return Result.getFailed();
        }

        ContractTransaction contractTx = (ContractTransaction) tx;
        contractTx.setBlockHeader(blockHeader);
        // 验证区块时发现智能合约交易就调用智能合约
        Result<ContractResult> invokeContractResult = this.invokeContract(executor, tx, bestHeight, null, false);
        ContractResult contractResult = invokeContractResult.getData();
        if (contractResult != null) {
            this.handleContractResult(tx, contractResult, stateRoot, blockTime, toMaps, contractUsedCoinMap);
        }

        return Result.getSuccess().setData(contractResult);
    }

    @Override
    public Result<ContractResult> batchProcessTx(Transaction tx, long bestHeight, Block block, byte[] stateRoot, Map<String, Coin> toMaps, Map<String, Coin> contractUsedCoinMap, boolean isForkChain) {
        if(stateRoot == null) {
            return Result.getFailed();
        }

        BlockHeader blockHeader = block.getHeader();
        long blockTime = blockHeader.getTime();
        ProgramExecutor executor = localProgramExecutor.get();
        if(executor == null) {
            return Result.getFailed();
        }

        ContractTransaction contractTx = (ContractTransaction) tx;
        contractTx.setBlockHeader(blockHeader);
        // 验证区块时发现智能合约交易就调用智能合约
        Result<ContractResult> invokeContractResult = this.invokeContract(executor, tx, bestHeight, null, isForkChain);
        ContractResult contractResult = invokeContractResult.getData();
        if (contractResult != null) {
            Result<byte[]> handleContractResult;
            if(isForkChain) {
                handleContractResult = this.verifyContractResult(tx, contractResult, stateRoot, blockTime, toMaps, contractUsedCoinMap, bestHeight);
            } else {
                handleContractResult = this.verifyContractResult(tx, contractResult, stateRoot, blockTime, toMaps, contractUsedCoinMap);
            }
        }

        return Result.getSuccess().setData(contractResult);
    }

    @Override
    public Result<List<ContractTransferTransaction>> loadAllContractTransferTxList() {
        try {
            List<ContractTransferTransaction> list = contractTransferTransactionStorageService.loadAllContractTransferTxList();
            return Result.getSuccess().setData(list);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed();
        }
    }

    @Override
    public void createBatchExecute(byte[] stateRoot) {
        localProgramExecutor.remove();
        if(stateRoot == null) {
            return;
        }
        ProgramExecutor executor = programExecutor.begin(stateRoot);
        localProgramExecutor.set(executor);
    }

    @Override
    public Result<byte[]> commitBatchExecute() {
        ProgramExecutor executor = localProgramExecutor.get();
        if(executor == null) {
            return Result.getSuccess();
        }
        executor.commit();
        byte[] stateRoot = executor.getRoot();
        return Result.getSuccess().setData(stateRoot);
    }

    @Override
    public void removeBatchExecute() {
        localProgramExecutor.remove();
    }

    @Override
    public void createCurrentBlockHeader(BlockHeader tempHeader) {
        vmContext.createCurrentBlockHeader(tempHeader);
    }

    @Override
    public void removeCurrentBlockHeader() {
        vmContext.removeCurrentBlockHeader();
    }
}

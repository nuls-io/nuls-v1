/**
 * MIT License
 * *
 * Copyright (c) 2017-2018 nuls.io
 * *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.ledger.service.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.spring.lite.annotation.Autowired;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.TxAccountRelationDataService;
import io.nuls.db.dao.UtxoInputDataService;
import io.nuls.db.dao.UtxoOutputDataService;
import io.nuls.db.entity.TxAccountRelationPo;
import io.nuls.db.entity.UtxoInputPo;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.ledger.entity.*;
import io.nuls.ledger.entity.params.Coin;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.ledger.entity.tx.LockNulsTransaction;
import io.nuls.ledger.entity.tx.UnlockNulsTransaction;
import io.nuls.ledger.service.intf.CoinDataProvider;
import io.nuls.ledger.util.UtxoTransactionTool;
import io.nuls.ledger.util.UtxoTransferTool;
import io.nuls.protocol.constant.TransactionConstant;
import io.nuls.protocol.constant.TxStatusEnum;
import io.nuls.protocol.model.Na;
import io.nuls.protocol.model.NulsDigestData;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.script.P2PKHScript;
import io.nuls.protocol.script.P2PKHScriptSig;
import io.nuls.protocol.utils.io.NulsByteBuffer;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Niels
 * @date 2017/12/21
 */
public class UtxoCoinDataProvider implements CoinDataProvider {

    //    private LedgerCacheService ledgerCacheService = LedgerCacheService.getInstance();
    @Autowired
    private UtxoOutputDataService outputDataService;
    @Autowired
    private UtxoInputDataService inputDataService;
    @Autowired
    private TxAccountRelationDataService relationDataService;
    @Autowired
    private AccountService accountService;

    private UtxoCoinManager coinManager = UtxoCoinManager.getInstance();

    private LedgerCacheService ledgerCacheService = LedgerCacheService.getInstance();

    private static Lock lock = new ReentrantLock();

    @Override
    public CoinData parse(NulsByteBuffer byteBuffer) throws NulsException {
        CoinData coinData = byteBuffer.readNulsData(new UtxoData());
        return coinData;
    }

//    @Override
//    public void approve(CoinData coinData, Transaction tx) throws NulsException {
//        //spent the transaction specified output in the cache when the newly received transaction is approved.
//        UtxoData utxoData = (UtxoData) coinData;
//        for (UtxoInput input : utxoData.getInputs()) {
//            input.setTxHash(tx.getHash());
//        }
//        for (UtxoOutput output : utxoData.getOutputs()) {
//            output.setTxHash(tx.getHash());
//        }
//
//        List<UtxoOutput> unSpends = new ArrayList<>();
//        Set<String> addressSet = new HashSet<>();
//        try {
//            lock.lock();
//            //update inputs referenced utxo status
//            for (int i = 0; i < utxoData.getInputs().size(); i++) {
//                UtxoInput input = utxoData.getInputs().get(i);
//                UtxoOutput unSpend = ledgerCacheService.getUtxo(input.getKey());
//                if (null == unSpend) {
//                    throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "the output is not exist!");
//                }
//                if (tx.getType() != TransactionConstant.TX_TYPE_STOP_AGENT) {
//                    if (!unSpend.isUsable()) {
//                        throw new NulsRuntimeException(ErrorCode.UTXO_UNUSABLE);
//                    }
//                    if (OutPutStatusEnum.UTXO_CONFIRMED_UNSPENT == unSpend.getStatus()) {
//                        unSpend.setStatus(OutPutStatusEnum.UTXO_CONFIRMED_SPENT);
//                    }
//                } else {
//                    if (unSpend.getStatus() == OutPutStatusEnum.UTXO_CONFIRMED_CONSENSUS_LOCK) {
//                        unSpend.setStatus(OutPutStatusEnum.UTXO_CONFIRMED_SPENT);
//                    } else {
//                        throw new NulsRuntimeException(ErrorCode.UTXO_UNUSABLE);
//                    }
//                }
//                BlockLog.debug("use utxo:txHash:" + tx.getHash() + ", utxoKey:" + unSpend.getKey());
//                unSpends.add(unSpend);
//                addressSet.add(unSpend.getAddress());
//            }
//
//            //cache new utxo ,it is unConfirm
//            approveProcessOutput(utxoData.getOutputs(), tx, addressSet);
//        } catch (Exception e) {
//            //rollback
//            for (UtxoOutput output : unSpends) {
//                if (tx.getType() != TransactionConstant.TX_TYPE_STOP_AGENT) {
//                    if (OutPutStatusEnum.UTXO_CONFIRMED_SPENT.equals(output.getStatus())) {
//                        ledgerCacheService.updateUtxoStatus(output.getKey(), OutPutStatusEnum.UTXO_CONFIRMED_UNSPENT, OutPutStatusEnum.UTXO_CONFIRMED_SPENT);
//                    }
//                } else {
//                    if (output.getStatus() == OutPutStatusEnum.UTXO_CONFIRMED_SPENT) {
//                        output.setStatus(OutPutStatusEnum.UTXO_CONFIRMED_CONSENSUS_LOCK);
//                    }
//                }
//            }
//            // remove cache new utxo
//            for (int i = 0; i < utxoData.getOutputs().size(); i++) {
//                UtxoOutput output = utxoData.getOutputs().get(i);
//                ledgerCacheService.removeUtxo(output.getKey());
//            }
//            throw e;
//        } finally {
//            lock.unlock();
//            //calc balance
//            for (String address : addressSet) {
//                UtxoTransactionTool.getInstance().calcBalance(address, false);
//            }
//        }
//    }

//    private void approveProcessOutput(List<UtxoOutput> outputs, Transaction tx, Set<String> addressSet) {
//        for (int i = 0; i < outputs.size(); i++) {
//            UtxoOutput output = outputs.get(i);
//            if (tx instanceof LockNulsTransaction && i == 0 && output.getLockTime() == 0) {
//                output.setStatus(OutPutStatusEnum.UTXO_CONFIRMED_CONSENSUS_LOCK);
//            } else if (output.getLockTime() > 0) {
//                output.setStatus(OutPutStatusEnum.UTXO_CONFIRMED_TIME_LOCK);
//            } else {
//                output.setStatus(OutPutStatusEnum.UTXO_CONFIRMED_UNSPENT);
//            }
//            ledgerCacheService.putUtxo(output.getKey(), output);
//            addressSet.add(output.getAddress());
//        }
//    }

    /**
     * 1. change spending output status  (cache and database)
     * 2. save new input
     * 3. save new unSpend output (cache and database)
     * 4. finally, calc balance
     */
    @Override
    @DbSession
    public void save(CoinData coinData, Transaction tx) throws NulsException {
        UtxoTransactionTool.getInstance().setTxhashToUtxo(tx);

        UtxoData utxoData = (UtxoData) coinData;
        List<UtxoInputPo> inputPoList = new ArrayList<>();
        List<UtxoOutputPo> spendPoList = new ArrayList<>();
        List<TxAccountRelationPo> txRelations = new ArrayList<>();
        List<UtxoOutput> spends = new ArrayList<>();
        Set<String> addressSet = new HashSet<>();
        lock.lock();
        try {
            processDataInput(utxoData, spends, inputPoList, spendPoList, addressSet, tx);

            refreshCache(spends, utxoData.getOutputs());

            List<UtxoOutputPo> outputPoList = new ArrayList<>();
            for (int i = 0; i < utxoData.getOutputs().size(); i++) {
                UtxoOutput output = utxoData.getOutputs().get(i);
                output.setTxHash(tx.getHash());
                UtxoOutputPo outputPo = UtxoTransferTool.toOutputPojo(output);
                outputPoList.add(outputPo);
                addressSet.add(Address.fromHashs(output.getAddress()).getBase58());
            }

            for (String address : addressSet) {
                TxAccountRelationPo relationPo = new TxAccountRelationPo(tx.getHash().getDigestHex(), address);
                if (relationDataService.getRelationCount(relationPo.getTxHash(), address) == 0) {
                    relationDataService.save(relationPo);
                }
//                txRelations.add(relationPo);
            }

            outputDataService.updateStatus(spendPoList);
            inputDataService.save(inputPoList);
            outputDataService.save(outputPoList);

//            relationDataService.save(txRelations);
//
//            for (String address : addressSet) {
//                UtxoTransactionTool.getInstance().calcBalance(address, true);
//            }
        } catch (Exception e) {
//          rollback
            Log.warn(e.getMessage(), e);
            for (UtxoOutput output : utxoData.getOutputs()) {
                ledgerCacheService.removeUtxo(output.getKey());
            }
            for (UtxoOutput spend : spends) {
                if (tx.getType() == TransactionConstant.TX_TYPE_STOP_AGENT) {
                    spend.setLockTime(0L);
                    spend.setStatus(OutPutStatusEnum.UTXO_CONSENSUS_LOCK);
                } else {
                    spend.setStatus(OutPutStatusEnum.UTXO_UNSPENT);
                }
                ledgerCacheService.putUtxo(spend.getKey(), spend, true);
            }
            throw e;
        } finally {
            lock.unlock();
        }
    }

    //Check if the input referenced output has been spent
    private void processDataInput(UtxoData utxoData, List<UtxoOutput> spends, List<UtxoInputPo> inputPoList,
                                  List<UtxoOutputPo> spendPoList, Set<String> addressSet, Transaction tx) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < utxoData.getInputs().size(); i++) {
            UtxoInput input = utxoData.getInputs().get(i);

            UtxoOutput output = ledgerCacheService.getUtxo(input.getKey());
            if (output == null) {
                throw new NulsRuntimeException(ErrorCode.UTXO_NOT_FOUND);
            }
//                map.put("txHash", input.getFromHash().getDigestHex());
//                map.put("outIndex", input.getFromIndex());
//                UtxoOutputPo outputPo = outputDataService.get(map);
//                if (outputPo == null) {
//                    throw new NulsRuntimeException(ErrorCode.UTXO_NOT_FOUND);
//                }
//                output = UtxoTransferTool.toOutput(outputPo);
            if (tx.getType() == TransactionConstant.TX_TYPE_STOP_AGENT) {
                if (output.getStatus() != OutPutStatusEnum.UTXO_CONSENSUS_LOCK) {
                    throw new NulsRuntimeException(ErrorCode.UTXO_STATUS_CHANGE);
                }
            } else {
                if (output.getStatus() != OutPutStatusEnum.UTXO_UNSPENT) {
                    throw new NulsRuntimeException(ErrorCode.UTXO_STATUS_CHANGE);
                }
            }
            output.setStatus(OutPutStatusEnum.UTXO_SPENT);
            spends.add(output);
            spendPoList.add(UtxoTransferTool.toOutputPojo(output));
            inputPoList.add(UtxoTransferTool.toInputPojo(input));
            addressSet.add(output.getAddress());
        }
    }

    private void refreshCache(List<UtxoOutput> spends, List<UtxoOutput> outputList) {
        for (int i = 0; i < spends.size(); i++) {
            ledgerCacheService.removeUtxo(spends.get(i).getKey());
        }
        for (UtxoOutput output : outputList) {
            ledgerCacheService.putUtxo(output.getKey(), output, true);
        }
    }

    @Override
    @DbSession
    public void rollback(CoinData coinData, Transaction tx) {
        UtxoData utxoData = (UtxoData) coinData;
        if (utxoData == null) {
            return;
        }
        Map<String, Object> keyMap = new HashMap<>();
        for (int i = utxoData.getOutputs().size() - 1; i >= 0; i--) {
            UtxoOutput output = utxoData.getOutputs().get(i);
            keyMap.put("txHash", output.getTxHash().getDigestHex());
            keyMap.put("outIndex", output.getIndex());
            int count = outputDataService.delete(keyMap);
            if (count != 1) {
                Log.info("delete " + output.getKey() + " fail");
                throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "delete output failed!");
            } else {
                Log.info("delete " + output.getKey() + " success");
            }
            ledgerCacheService.removeUtxo(output.getKey());
        }

        for (int i = utxoData.getInputs().size() - 1; i >= 0; i--) {
            UtxoInput input = utxoData.getInputs().get(i);
            keyMap.put("txHash", input.getFromHash().getDigestHex());
            keyMap.put("outIndex", input.getFromIndex());
            UtxoOutputPo po = outputDataService.get(keyMap);
            if (tx.getType() == TransactionConstant.TX_TYPE_STOP_AGENT) {
                po.setLockTime(0L);
                po.setStatus(UtxoOutputPo.LOCKED);
            } else {
                po.setStatus(UtxoOutputPo.USABLE);
            }
            outputDataService.updateStatus(po);
            UtxoOutput output = UtxoTransferTool.toOutput(po);
            ledgerCacheService.putUtxo(output.getKey(), output, true);
        }

        String txHash = tx.getHash().getDigestHex();
        inputDataService.deleteByHash(txHash);
        relationDataService.deleteRelation(txHash);
    }

    @Override
    public CoinData createByTransferData(Transaction tx, CoinTransferData coinParam, String password) throws NulsException {
        lock.lock();
        try {
            if (coinParam.getSpecialData() != null) {
                return createBySpecialData(tx, coinParam, password);
            }
            UtxoData utxoData = new UtxoData();
            List<UtxoInput> inputs = new ArrayList<>();
            List<UtxoOutput> outputs = new ArrayList<>();
            // Na totalNa = Na.ZERO;

            if (coinParam.getTotalNa().equals(Na.ZERO)) {
                utxoData.setInputs(inputs);
                utxoData.setOutputs(outputs);
                return utxoData;
            }

            long inputValue = 0;
            if (!coinParam.getFrom().isEmpty()) {
                //find unSpends to create inputs for this tx
                Na totalFee = Na.ZERO;
                if (tx instanceof UnlockNulsTransaction) {
                    totalFee = coinParam.getFee();
                } else {
                    totalFee = coinParam.getTotalNa().add(coinParam.getFee());
                }

                List<UtxoOutput> unSpends = coinManager.getAccountsUnSpend(coinParam.getFrom(), totalFee);
                if (unSpends.isEmpty()) {
                    throw new NulsException(ErrorCode.BALANCE_NOT_ENOUGH);
                }

                for (int i = 0; i < unSpends.size(); i++) {
                    UtxoOutput output = unSpends.get(i);
                    UtxoInput input = new UtxoInput();
                    input.setFrom(output);
                    input.setFromHash(output.getTxHash());
                    input.setFromIndex(output.getIndex());
                    input.setTxHash(tx.getHash());
                    input.setIndex(i);
                    inputValue += output.getValue();

                    inputs.add(input);
                }
            }

            //get EcKey for output's script
            Account account = null;
            byte[] priKey = null;
            if (coinParam.getPriKey() != null) {
                priKey = coinParam.getPriKey();
            } else if (!coinParam.getFrom().isEmpty()) {
                account = accountService.getAccount(coinParam.getFrom().get(0));
                if (account == null) {
                    throw new NulsException(ErrorCode.ACCOUNT_NOT_EXIST);
                }
                if (account.isEncrypted() && account.isLocked()) {
                    if (!account.unlock(password)) {
                        throw new NulsException(ErrorCode.PASSWORD_IS_WRONG);
                    }
                    priKey = account.getPriKey();
                    account.lock();
                } else {
                    priKey = account.getPriKey();
                }
            }

            //create outputs
            long outputValue = 0;
            for (int index=0;index<coinParam.getToCoinList().size();index++) {
                Coin coin = coinParam.getToCoinList().get(index);
                UtxoOutput output = new UtxoOutput();
                output.setAddress(coin.getAddress());
                output.setValue(coin.getNa().getValue());
                if (output.getLockTime() > 0) {
                    output.setStatus(OutPutStatusEnum.UTXO_TIME_LOCK);
                } else if (tx instanceof LockNulsTransaction) {
                    output.setStatus(OutPutStatusEnum.UTXO_CONSENSUS_LOCK);
                } else {
                    output.setStatus(OutPutStatusEnum.UTXO_UNSPENT);
                }

                output.setIndex(index);
                P2PKHScript p2PKHScript = new P2PKHScript(new NulsDigestData(NulsDigestData.DIGEST_ALG_SHA160, new Address(coin.getAddress()).getHash160()));
                output.setP2PKHScript(p2PKHScript);
                if (coin.getUnlockHeight() > 0) {
                    output.setLockTime(coin.getUnlockHeight());
                } else if (coin.getUnlockTime() > 0) {
                    output.setLockTime(coin.getUnlockTime());
                } else {
                    output.setLockTime(0L);
                }
                output.setTxHash(tx.getHash());
                outputValue += output.getValue();
                outputs.add(output);
            }

            //the balance leave to myself
            long balance = 0;
            if (outputValue > 0) {
                balance = inputValue - outputValue - coinParam.getFee().getValue();
            } else {
                balance = inputValue - coinParam.getTotalNa().getValue() - coinParam.getFee().getValue();
            }
            if (balance > 0) {
                UtxoOutput output = new UtxoOutput();
                output.setAddress(inputs.get(0).getFrom().getAddress());
                output.setValue(balance);
                output.setIndex(outputs.size());
                output.setTxHash(tx.getHash());
                output.setStatus(OutPutStatusEnum.UTXO_UNSPENT);
                P2PKHScript p2PKHScript = new P2PKHScript(new NulsDigestData(NulsDigestData.DIGEST_ALG_SHA160, account.getHash160()));
                output.setP2PKHScript(p2PKHScript);
                outputs.add(output);
            }
            utxoData.setInputs(inputs);
            utxoData.setOutputs(outputs);
            return utxoData;
        } finally {
            lock.unlock();
        }
    }

    public CoinData createBySpecialData(Transaction tx, CoinTransferData coinParam, String password) throws NulsException {
        UtxoData utxoData = new UtxoData();
        List<UtxoInput> inputs = new ArrayList<>();
        List<UtxoOutput> outputs = new ArrayList<>();

        Map<String, Object> map = coinParam.getSpecialData();
        int type = (int) map.get("type");
        if (type == 1) {
            String lockTxHash = map.get("lockedTxHash").toString();
            Long lockTime = (Long) map.get("lockTime");
            Map<String, Object> keyMap = new HashMap<>();
            keyMap.put("txHash", lockTxHash);
            keyMap.put("outIndex", 0);
            UtxoOutputPo outputPo = outputDataService.get(keyMap);
            if (outputPo == null) {
                throw new NulsException(ErrorCode.UTXO_NOT_FOUND);
            }
            if (outputPo.getStatus() != UtxoOutputPo.LOCKED) {
                throw new NulsException(ErrorCode.UTXO_STATUS_CHANGE);
            }
            UtxoOutput output = UtxoTransferTool.toOutput(outputPo);

            UtxoInput input = new UtxoInput();
            input.setFrom(output);
            input.setFromHash(output.getTxHash());
            input.setFromIndex(output.getIndex());
            input.setIndex(0);
            inputs.add(input);

            UtxoOutput newOutput = new UtxoOutput();
            newOutput.setIndex(0);
            newOutput.setValue(output.getValue());
            newOutput.setAddress(output.getAddress());
            newOutput.setLockTime(tx.getTime() + lockTime);
            newOutput.setStatus(OutPutStatusEnum.UTXO_TIME_LOCK);
            newOutput.setCreateTime(TimeService.currentTimeMillis());
            P2PKHScript p2PKHScript = new P2PKHScript(new NulsDigestData(NulsDigestData.DIGEST_ALG_SHA160, new Address(output.getAddress()).getHash160()));
            newOutput.setP2PKHScript(p2PKHScript);
            outputs.add(newOutput);

            utxoData.setInputs(inputs);
            utxoData.setOutputs(outputs);
            utxoData.setTotalNa(Na.valueOf(output.getValue()));
        }
        return utxoData;
    }

    @Override
    public void afterParse(CoinData coinData, Transaction tx) {
        UtxoData utxoData = (UtxoData) coinData;
        if (null != utxoData.getInputs()) {
            for (UtxoInput input : utxoData.getInputs()) {
                input.setTxHash(tx.getHash());
            }
        }
        if (tx instanceof LockNulsTransaction) {
            utxoData.getOutputs().get(0).setStatus(OutPutStatusEnum.UTXO_CONSENSUS_LOCK);
        }
//        Na totalNa = Na.ZERO;
        if (null != utxoData.getOutputs()) {
            for (int i = 0; i < utxoData.getOutputs().size(); i++) {
                UtxoOutput output = utxoData.getOutputs().get(i);
                output.setTxHash(tx.getHash());
            }
        }
//        coinData.setTotalNa(totalNa);
    }

    @Override
    public ValidateResult conflictDetect(Transaction tx, List<Transaction> txList) {
        AbstractCoinTransaction coinTx = (AbstractCoinTransaction) tx;
        UtxoData txUtxoData = (UtxoData) coinTx.getCoinData();
        if (txUtxoData.getInputs() == null || txUtxoData.getInputs().isEmpty()) {
            return ValidateResult.getSuccessResult();
        }
        Set<String> outputSet = new HashSet<>();
        for (Transaction transaction : txList) {
            if (!(transaction instanceof AbstractCoinTransaction)) {
                continue;
            }
            AbstractCoinTransaction coinTransaction = (AbstractCoinTransaction) transaction;
            UtxoData utxoData = (UtxoData) coinTransaction.getCoinData();
            if (null == utxoData.getInputs() || utxoData.getInputs().isEmpty()) {
                continue;
            }
            for (UtxoInput input : utxoData.getInputs()) {
                outputSet.add(input.getKey());
            }
        }
        for (UtxoInput input : txUtxoData.getInputs()) {
            boolean result = outputSet.add(input.getKey());
            if (!result) {
                return ValidateResult.getFailedResult(ErrorCode.FAILED, "input conflict!");
            }
        }
        return ValidateResult.getSuccessResult();
    }

    @Override
    public ValidateResult verifyCoinData(AbstractCoinTransaction tx, List<Transaction> txList) {

        if (txList == null || txList.isEmpty()) {
            //It's all an orphan that can go here
            return ValidateResult.getFailedResult(ErrorCode.ORPHAN_TX);
        }

        UtxoData data = (UtxoData) tx.getCoinData();


        Map<String, UtxoOutput> outputMap = getAllOutputMap(txList);


        for (int i = 0; i < data.getInputs().size(); i++) {
            UtxoInput input = data.getInputs().get(i);
            UtxoOutput output = ledgerCacheService.getUtxo(input.getKey());

            if (output == null && tx.getStatus() == TxStatusEnum.UNCONFIRM) {
                output = outputMap.get(input.getKey());
                if (null == output) {
                    return ValidateResult.getFailedResult(ErrorCode.ORPHAN_TX);
                }
            } else if (output == null) {
                return ValidateResult.getFailedResult(ErrorCode.UTXO_NOT_FOUND);
            }

            if (tx.getStatus() == TxStatusEnum.UNCONFIRM) {
                if (tx.getType() == TransactionConstant.TX_TYPE_STOP_AGENT) {
                    if (output.getStatus() != OutPutStatusEnum.UTXO_CONSENSUS_LOCK) {
                        return ValidateResult.getFailedResult(ErrorCode.UTXO_STATUS_CHANGE);
                    }
                } else if (output.getStatus() != OutPutStatusEnum.UTXO_UNSPENT) {
                    return ValidateResult.getFailedResult(ErrorCode.UTXO_STATUS_CHANGE);
                }
            }
//            else if (tx.getStatus() == TxStatusEnum.AGREED) {
//                if (!output.isSpend()) {
//                    return ValidateResult.getFailedResult(ErrorCode.UTXO_STATUS_CHANGE);
//                }
//            }

            byte[] owner = output.getOwner();
            P2PKHScriptSig p2PKHScriptSig = null;
            try {
                p2PKHScriptSig = P2PKHScriptSig.createFromBytes(tx.getScriptSig());
            } catch (NulsException e) {
                return ValidateResult.getFailedResult(ErrorCode.DATA_ERROR);
            }
            byte[] user = p2PKHScriptSig.getSignerHash160();
            if (!Arrays.equals(owner, user)) {
                return ValidateResult.getFailedResult(ErrorCode.INVALID_INPUT);
            }

            return ValidateResult.getSuccessResult();
        }
        return ValidateResult.getSuccessResult();
    }

    private Map<String, UtxoOutput> getAllOutputMap(List<Transaction> txList) {
        Map<String, UtxoOutput> map = new HashMap<>();
        for (Transaction tx : txList) {
            if (!(tx instanceof AbstractCoinTransaction)) {
                continue;
            }
            List<UtxoOutput> outputs = ((UtxoData) ((AbstractCoinTransaction) tx).getCoinData()).getOutputs();
            if (null == outputs) {
                continue;
            }
            for (UtxoOutput output : outputs) {
                if (output.getTxHash() == null) {
                    output.setTxHash(tx.getHash());
                }
                map.put(output.getKey(), output);
            }
        }
        for (Transaction tx : txList) {
            if (!(tx instanceof AbstractCoinTransaction)) {
                continue;
            }
            List<UtxoInput> inputs = ((UtxoData) ((AbstractCoinTransaction) tx).getCoinData()).getInputs();
            if (null == inputs) {
                continue;
            }
            for (UtxoInput input : inputs) {
                map.remove(input.getKey());
            }
        }
        return map;
    }
}

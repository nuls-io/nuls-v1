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
package io.nuls.ledger.service.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.script.P2PKHScript;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.spring.lite.annotation.Autowired;
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
import io.nuls.ledger.entity.tx.LockNulsTransaction;
import io.nuls.ledger.entity.tx.UnlockNulsTransaction;
import io.nuls.ledger.service.intf.CoinDataProvider;
import io.nuls.ledger.util.UtxoTransactionTool;
import io.nuls.ledger.util.UtxoTransferTool;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Niels
 * @date 2017/12/21
 */
public class UtxoCoinDataProvider implements CoinDataProvider {

    private LedgerCacheService ledgerCacheService = LedgerCacheService.getInstance();
    @Autowired
    private UtxoOutputDataService outputDataService;
    @Autowired
    private UtxoInputDataService inputDataService;
    @Autowired
    private TxAccountRelationDataService relationDataService;
    @Autowired
    private AccountService accountService;

    private UtxoCoinManager coinManager = UtxoCoinManager.getInstance();

    private Lock lock = new ReentrantLock();

    @Override
    public CoinData parse(NulsByteBuffer byteBuffer) throws NulsException {
        CoinData coinData = byteBuffer.readNulsData(new UtxoData());
        return coinData;
    }

    @Override
    public void approve(CoinData coinData, Transaction tx) throws NulsException {
        //spent the transaction specified output in the cache when the newly received transaction is approved.
        UtxoData utxoData = (UtxoData) coinData;
        for (UtxoInput input : utxoData.getInputs()) {
            input.setTxHash(tx.getHash());
        }
        for (UtxoOutput output : utxoData.getOutputs()) {
            output.setTxHash(tx.getHash());
        }

        List<UtxoOutput> unSpends = new ArrayList<>();
        Set<String> addressSet = new HashSet<>();
        try {
            //update inputs referenced utxo status
            for (int i = 0; i < utxoData.getInputs().size(); i++) {
                UtxoInput input = utxoData.getInputs().get(i);
                UtxoOutput unSpend = ledgerCacheService.getUtxo(input.getKey());
                if (null == unSpend) {
                    throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "the output is not exist!");
                }
                if (!unSpend.isUsable()) {
                    throw new NulsRuntimeException(ErrorCode.UTXO_UNUSABLE);
                }
                if (OutPutStatusEnum.UTXO_CONFIRM_UNSPEND == unSpend.getStatus()) {
                    unSpend.setStatus(OutPutStatusEnum.UTXO_CONFIRM_SPEND);
                } else if (OutPutStatusEnum.UTXO_UNCONFIRM_UNSPEND == unSpend.getStatus()) {
                    unSpend.setStatus(OutPutStatusEnum.UTXO_UNCONFIRM_SPEND);
                }
                unSpends.add(unSpend);
                addressSet.add(unSpend.getAddress());
            }

            //cache new utxo ,it is unConfirm
            approveProcessOutput(utxoData.getOutputs(), tx, addressSet);
        } catch (Exception e) {
            //rollback
            for (UtxoOutput output : unSpends) {
                if (OutPutStatusEnum.UTXO_CONFIRM_SPEND.equals(output.getStatus())) {
                    ledgerCacheService.updateUtxoStatus(output.getKey(), OutPutStatusEnum.UTXO_CONFIRM_UNSPEND, OutPutStatusEnum.UTXO_CONFIRM_SPEND);
                } else if (OutPutStatusEnum.UTXO_UNCONFIRM_SPEND.equals(output.getStatus())) {
                    ledgerCacheService.updateUtxoStatus(output.getKey(), OutPutStatusEnum.UTXO_UNCONFIRM_UNSPEND, OutPutStatusEnum.UTXO_UNCONFIRM_SPEND);
                }
            }
            // remove cache new utxo
            for (int i = 0; i < utxoData.getOutputs().size(); i++) {
                UtxoOutput output = utxoData.getOutputs().get(i);
                ledgerCacheService.removeUtxo(output.getKey());
            }
            throw e;
        } finally {
            //calc balance
            for (String address : addressSet) {
                UtxoTransactionTool.getInstance().calcBalance(address, false);
            }
        }
    }

    private void approveProcessOutput(List<UtxoOutput> outputs, Transaction tx, Set<String> addressSet) {
        for (int i = 0; i < outputs.size(); i++) {
            UtxoOutput output = outputs.get(i);
            if (tx instanceof LockNulsTransaction && i == 0) {
                output.setStatus(OutPutStatusEnum.UTXO_UNCONFIRM_CONSENSUS_LOCK);
            } else if (output.getLockTime() > 0) {
                output.setStatus(OutPutStatusEnum.UTXO_UNCONFIRM_TIME_LOCK);
            } else {
                output.setStatus(OutPutStatusEnum.UTXO_UNCONFIRM_UNSPEND);
            }
            ledgerCacheService.putUtxo(output.getKey(), output);
            addressSet.add(output.getAddress());
        }
    }

    /**
     * 1. change spending output status  (cache and database)
     * 2. save new input
     * 3. save new unSpend output (cache and database)
     * 4. finally, calc balance
     */
    @Override
    @DbSession
    public void save(CoinData coinData, Transaction tx) throws NulsException {
        UtxoData utxoData = (UtxoData) coinData;

        List<UtxoInputPo> inputPoList = new ArrayList<>();
        List<UtxoOutput> spends = new ArrayList<>();
        List<UtxoOutputPo> spendPoList = new ArrayList<>();
        List<TxAccountRelationPo> txRelations = new ArrayList<>();
        Set<String> addressSet = new HashSet<>();

        try {
            processDataInput(utxoData, inputPoList, spends, spendPoList, addressSet);

            List<UtxoOutputPo> outputPoList = new ArrayList<>();
            for (int i = 0; i < utxoData.getOutputs().size(); i++) {
                UtxoOutput output = utxoData.getOutputs().get(i);
                output = ledgerCacheService.getUtxo(output.getKey());
                if (output == null) {
                    throw new NulsRuntimeException(ErrorCode.DATA_NOT_FOUND);
                }
                if (output.isConfirm() || OutPutStatusEnum.UTXO_SPENT == output.getStatus()) {
                    Log.error("-----------------------------------save() output status is" + output.getStatus().name());
                    throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "use a not legal utxo");
                }

                if (OutPutStatusEnum.UTXO_UNCONFIRM_CONSENSUS_LOCK == output.getStatus()) {
                    output.setStatus(OutPutStatusEnum.UTXO_CONFIRM_CONSENSUS_LOCK);
                } else if (OutPutStatusEnum.UTXO_UNCONFIRM_TIME_LOCK == output.getStatus()) {
                    output.setStatus(OutPutStatusEnum.UTXO_CONFIRM_TIME_LOCK);
                } else if (OutPutStatusEnum.UTXO_UNCONFIRM_UNSPEND == output.getStatus()) {
                    output.setStatus(OutPutStatusEnum.UTXO_CONFIRM_UNSPEND);
                } else if (OutPutStatusEnum.UTXO_UNCONFIRM_SPEND == output.getStatus()) {
                    output.setStatus(OutPutStatusEnum.UTXO_CONFIRM_SPEND);
                }

                UtxoOutputPo outputPo = UtxoTransferTool.toOutputPojo(output);
                outputPoList.add(outputPo);
                addressSet.add(Address.fromHashs(output.getAddress()).getBase58());
            }

            for (String address : addressSet) {
                TxAccountRelationPo relationPo = new TxAccountRelationPo(tx.getHash().getDigestHex(), address);
                txRelations.add(relationPo);
            }

            outputDataService.updateStatus(spendPoList);

            inputDataService.save(inputPoList);

            outputDataService.save(outputPoList);

            relationDataService.save(txRelations);

            afterSaveDatabase(spends, utxoData, tx);

            for (String address : addressSet) {
                UtxoTransactionTool.getInstance().calcBalance(address, true);
            }
        } catch (Exception e) {
            //rollback
//            Log.warn(e.getMessage(), e);
//            for (UtxoOutput output : utxoData.getOutputs()) {
//                ledgerCacheService.removeUtxo(output.getKey());
//            }
//            for (UtxoOutput spend : spends) {
//                ledgerCacheService.updateUtxoStatus(spend.getKey(), UtxoOutput.UTXO_CONFIRM_LOCK, UtxoOutput.UTXO_SPENT);
//            }
            throw e;
        }
    }

    //Check if the input referenced output has been spent
    private void processDataInput(UtxoData utxoData, List<UtxoInputPo> inputPoList,
                                  List<UtxoOutput> spends, List<UtxoOutputPo> spendPoList,
                                  Set<String> addressSet) {
        boolean update;
        for (int i = 0; i < utxoData.getInputs().size(); i++) {
            UtxoInput input = utxoData.getInputs().get(i);
            UtxoOutput spend = ledgerCacheService.getUtxo(input.getKey());
            if (spend == null) {
                throw new NulsRuntimeException(ErrorCode.DATA_NOT_FOUND, "the output is not exist!");
            }
            //change utxo status,
            update = ledgerCacheService.updateUtxoStatus(spend.getKey(), OutPutStatusEnum.UTXO_SPENT, OutPutStatusEnum.UTXO_CONFIRM_SPEND);
            if (!update) {
                Log.error("-----------------------------------save() input referenced status is" + spend.getStatus().name());
                throw new NulsRuntimeException(ErrorCode.UTXO_STATUS_CHANGE);
            }
            spends.add(spend);
            spendPoList.add(UtxoTransferTool.toOutputPojo(spend));
            inputPoList.add(UtxoTransferTool.toInputPojo(input));
            addressSet.add(spend.getAddress());
        }
    }

    private void afterSaveDatabase(List<UtxoOutput> spends, UtxoData utxoData, Transaction tx) {
        for (int i = 0; i < spends.size(); i++) {
            ledgerCacheService.removeUtxo(spends.get(i).getKey());
        }
    }

    @Override
    @DbSession
    public void rollback(CoinData coinData, Transaction tx) {
        UtxoData utxoData = (UtxoData) coinData;
        if (utxoData == null) {
            return;
        }

        Set<String> addressSet = new HashSet<>();
        if (TxStatusEnum.AGREED.equals(tx.getStatus())) {
            for (UtxoInput input : utxoData.getInputs()) {
                UtxoOutput from = ledgerCacheService.getUtxo(input.getKey());
                if (from != null) {
                    if (from.getStatus() == OutPutStatusEnum.UTXO_SPENT) {
                        from.setStatus(OutPutStatusEnum.UTXO_CONFIRM_UNSPEND);
                    } else if (from.getStatus() == OutPutStatusEnum.UTXO_CONFIRM_SPEND) {
                        from.setStatus(OutPutStatusEnum.UTXO_CONFIRM_UNSPEND);
                    } else if (from.getStatus() == OutPutStatusEnum.UTXO_UNCONFIRM_SPEND) {
                        from.setStatus(OutPutStatusEnum.UTXO_UNCONFIRM_UNSPEND);
                    }
                    addressSet.add(from.getAddress());
                }
            }
            for (int i = utxoData.getOutputs().size() - 1; i >= 0; i--) {
                UtxoOutput output = utxoData.getOutputs().get(i);
                ledgerCacheService.removeUtxo(output.getKey());
                addressSet.add(output.getAddress());
            }
        } else if (tx.getStatus().equals(TxStatusEnum.CONFIRMED)) {
            //process output
            outputDataService.deleteByHash(tx.getHash().getDigestHex());
            for (int i = utxoData.getOutputs().size() - 1; i >= 0; i--) {
                UtxoOutput output = utxoData.getOutputs().get(i);
                ledgerCacheService.removeUtxo(output.getKey());
                addressSet.add(output.getAddress());
            }

            //process input
            //1. delete input (database)
            //2. change input referenced output status (database)
            //3. cache and calc balance
            inputDataService.deleteByHash(tx.getHash().getDigestHex());
            Map<String, Object> keyMap = new HashMap<>();
            for (int i = utxoData.getInputs().size() - 1; i >= 0; i--) {
                UtxoInput input = utxoData.getInputs().get(i);
                keyMap.clear();
                keyMap.put("txHash", input.getFromHash().getDigestHex());
                keyMap.put("outIndex", input.getFromIndex());
                UtxoOutputPo outputPo = outputDataService.get(keyMap);
                outputPo.setStatus(UtxoOutputPo.USABLE);
                outputDataService.updateStatus(outputPo);
                addressSet.add(outputPo.getAddress());

                UtxoOutput output = UtxoTransferTool.toOutput(outputPo);
                ledgerCacheService.putUtxo(output.getKey(), output);
            }
            relationDataService.deleteRelation(tx.getHash().getDigestHex(), addressSet);
        }

        for (String address : addressSet) {
            UtxoTransactionTool.getInstance().calcBalance(address, false);
        }
    }

    @Override
    public CoinData createByTransferData(Transaction tx, CoinTransferData coinParam, String password) throws NulsException {
        lock.lock();
        try {
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
            int i = 0;
            long outputValue = 0;
            for (Map.Entry<String, List<Coin>> entry : coinParam.getToMap().entrySet()) {
                String address = entry.getKey();
                List<Coin> coinList = entry.getValue();
                for (Coin coin : coinList) {
                    UtxoOutput output = new UtxoOutput();
                    output.setAddress(address);
                    output.setValue(coin.getNa().getValue());
                    if (output.getLockTime() > 0) {
                        output.setStatus(OutPutStatusEnum.UTXO_UNCONFIRM_TIME_LOCK);
                    } else if (tx instanceof LockNulsTransaction) {
                        output.setStatus(OutPutStatusEnum.UTXO_UNCONFIRM_CONSENSUS_LOCK);
                    } else {
                        output.setStatus(OutPutStatusEnum.UTXO_UNCONFIRM_UNSPEND);
                    }

                    output.setIndex(i);
                    P2PKHScript p2PKHScript = new P2PKHScript(new NulsDigestData(NulsDigestData.DIGEST_ALG_SHA160, new Address(address).getHash160()));
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

                    i++;
                }
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
                output.setIndex(i);
                output.setTxHash(tx.getHash());
                output.setStatus(OutPutStatusEnum.UTXO_UNCONFIRM_UNSPEND);
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

    @Override
    public void afterParse(CoinData coinData, Transaction tx) {
        UtxoData utxoData = (UtxoData) coinData;
        if (null != utxoData.getInputs()) {
            for (UtxoInput input : utxoData.getInputs()) {
                input.setTxHash(tx.getHash());
            }
        }
        if (tx instanceof LockNulsTransaction) {
            utxoData.getOutputs().get(0).setStatus(OutPutStatusEnum.UTXO_UNCONFIRM_CONSENSUS_LOCK);
        }
//        Na totalNa = Na.ZERO;
//        if (null != utxoData.getOutputs()) {
//            for (int i = 0; i < utxoData.getOutputs().size(); i++) {
//                UtxoOutput output = utxoData.getOutputs().get(i);
//                output.setTxHash(tx.getHash());
//            }
//        }
//        coinData.setTotalNa(totalNa);
    }
}

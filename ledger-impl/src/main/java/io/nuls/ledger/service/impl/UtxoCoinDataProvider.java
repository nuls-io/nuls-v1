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
package io.nuls.ledger.service.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.service.intf.AccountService;
import io.nuls.core.chain.entity.*;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.crypto.Utils;
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
import io.nuls.ledger.script.P2PKHScript;
import io.nuls.ledger.script.P2PKHScriptSig;
import io.nuls.ledger.service.intf.CoinDataProvider;
import io.nuls.ledger.util.UtxoTransactionTool;
import io.nuls.ledger.util.UtxoTransferTool;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * @author Niels
 * @date 2017/12/21
 */
public class UtxoCoinDataProvider implements CoinDataProvider {

    private LedgerCacheService cacheService = LedgerCacheService.getInstance();
    @Autowired
    private UtxoOutputDataService outputDataService;
    @Autowired
    private UtxoInputDataService inputDataService;
    @Autowired
    private TxAccountRelationDataService relationDataService;
    @Autowired
    private AccountService accountService;

    private UtxoCoinManager coinManager = UtxoCoinManager.getInstance();

    @Override
    public CoinData parse(NulsByteBuffer byteBuffer) throws NulsException {
        CoinData coinData = byteBuffer.readNulsData(new UtxoData());
        return coinData;
    }

    @Override
    public CoinTransferData getTransferData(CoinData coinData) {
        UtxoData utxoData = (UtxoData) coinData;
        //todo
        return null;
    }

    @Override
    public void approve(CoinData coinData, Transaction tx) {
        //Lock the transaction specified output in the cache when the newly received transaction is approved.
        UtxoData utxoData = (UtxoData) coinData;
        for (UtxoInput input : utxoData.getInputs()) {
            input.setTxHash(tx.getHash());
        }
        for (UtxoOutput output : utxoData.getOutputs()) {
            output.setTxHash(tx.getHash());
        }

        List<UtxoOutput> outputs = new ArrayList<>();
        try {
            for (int i = 0; i < utxoData.getInputs().size(); i++) {
                UtxoInput input = utxoData.getInputs().get(i);
                input.setTxHash(tx.getHash());
                UtxoOutput unSpend = cacheService.getUtxo(input.getKey());
                if(null==unSpend){
                    throw new NulsRuntimeException(ErrorCode.DATA_ERROR,"the output is not exist!");
                }
                unSpend.setStatus(UtxoOutput.LOCKED);
                outputs.add(unSpend);
            }

        } catch (Exception e) {
            for (UtxoOutput output : outputs) {
                cacheService.updateUtxoStatus(output.getKey(), UtxoOutput.USEABLE, UtxoOutput.LOCKED);
            }
            throw e;
        } finally {
            for (UtxoOutput output : outputs) {
                UtxoTransactionTool.getInstance().calcBalance(Address.fromHashs(output.getAddress()));
            }
        }
    }

    @Override
    @DbSession
    /**
     * 1. change spending output status  (cache and database)
     * 2. save new input
     * 3. save new unSpend output (cache and database)
     * 4. finally, calc balance
     */
    public void save(CoinData coinData, Transaction tx) {
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
                outputPoList.add(UtxoTransferTool.toOutputPojo(output));
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

            processDataOutput(utxoData);

            for (UtxoOutput spend : spends) {
                cacheService.removeUtxo(spend.getKey());
                UtxoBalance balance = (UtxoBalance) cacheService.getBalance(Address.fromHashs(spend.getAddress()).getBase58());
                balance.getUnSpends().remove(spend);
            }
        } catch (Exception e) {
            //rollback
            Log.warn(e.getMessage(), e);
            for (UtxoOutput output : utxoData.getOutputs()) {
                cacheService.removeUtxo(output.getKey());
                UtxoBalance balance = (UtxoBalance) cacheService.getBalance(Address.fromHashs(output.getAddress()).getBase58());
                if (balance != null) {
                    balance.getUnSpends().remove(output);
                }
            }

            for (UtxoOutput spend : spends) {
                cacheService.putUtxo(spend.getKey(), spend);
//                cacheService.updateUtxoStatus(spend.getKey(), UtxoOutput.SPENT, UtxoOutput.LOCKED);
                spend.setStatus(UtxoOutput.LOCKED);
                UtxoBalance balance = (UtxoBalance) cacheService.getBalance(Address.fromHashs(spend.getAddress()).getBase58());
                if (balance != null && !balance.getUnSpends().contains(spend)) {
                    balance.getUnSpends().add(spend);
                }
            }
            throw e;
        } finally {
            for (UtxoOutput spend : spends) {
                UtxoTransactionTool.getInstance().calcBalance(Address.fromHashs(spend.getAddress()));
            }
            for (UtxoOutput output : utxoData.getOutputs()) {
                UtxoTransactionTool.getInstance().calcBalance(Address.fromHashs(output.getAddress()));
            }
        }
    }

    //Check if the input referenced output has been spent
    private void processDataInput(UtxoData utxoData, List<UtxoInputPo> inputPoList,
                                  List<UtxoOutput> spends, List<UtxoOutputPo> spendPoList, Set<String> addressSet) {
        boolean update;
        for (int i = 0; i < utxoData.getInputs().size(); i++) {
            UtxoInput input = utxoData.getInputs().get(i);
            inputPoList.add(UtxoTransferTool.toInputPojo(input));

            //change utxo status,
            UtxoOutput spend = cacheService.getUtxo(input.getKey());
            update = cacheService.updateUtxoStatus(input.getKey(), UtxoOutput.SPENT, UtxoOutput.LOCKED);
            if (!update) {
                throw new NulsRuntimeException(ErrorCode.UTXO_STATUS_CHANGE);
            }
            spends.add(spend);
            spendPoList.add(UtxoTransferTool.toOutputPojo(spend));
            addressSet.add(Address.fromHashs(spend.getAddress()).getBase58());
        }
    }

    // cache new unSpends
    private void processDataOutput(UtxoData utxoData) {
        for (int i = 0; i < utxoData.getOutputs().size(); i++) {
            UtxoOutput output = utxoData.getOutputs().get(i);

            cacheService.putUtxo(output.getKey(), output);
            String address = Address.fromHashs(output.getAddress()).getBase58();
            UtxoBalance balance = (UtxoBalance) cacheService.getBalance(address);
            if (balance == null) {
                balance = new UtxoBalance(Na.valueOf(output.getValue()), Na.ZERO);
                balance.addUnSpend(output);
                cacheService.putBalance(address, balance);
            } else {
                balance.addUnSpend(output);
            }
        }
    }

    @Override
    @DbSession
    public void rollback(CoinData coinData, Transaction tx) {
        UtxoData utxoData = (UtxoData) coinData;
        if (utxoData == null) {
            return;
        }
        if (TxStatusEnum.AGREED.equals(tx.getStatus())) {
            for (UtxoInput input : utxoData.getInputs()) {
                cacheService.updateUtxoStatus(input.getKey(), UtxoOutput.USEABLE, UtxoOutput.LOCKED);
            }
        } else if (tx.getStatus().equals(TxStatusEnum.CONFIRMED)) {
            Set<String> addressSet = new HashSet<>();
            //process output
            outputDataService.deleteByHash(tx.getHash().getDigestHex());

            for (UtxoOutput output : utxoData.getOutputs()) {
                // if utxo not spent,should calc balance and clear cache
                cacheService.removeUtxo(output.getKey());
                Address address = Address.fromHashs(output.getAddress());
                UtxoBalance balance = (UtxoBalance) cacheService.getBalance(address.getBase58());
                if (balance != null) {
                    balance.removeUnSpend(output.getKey());
                }
                addressSet.add(address.getBase58());
            }

            //process input
            //1. delete input (database)
            //2. change input referenced output status (database)
            //3. cache and calc balance
            inputDataService.deleteByHash(tx.getHash().getDigestHex());
            Map<String, Object> keyMap = new HashMap<>();
            for (UtxoInput input : utxoData.getInputs()) {
                keyMap.clear();
                keyMap.put("txHash", input.getFromHash().getDigestHex());
                keyMap.put("outIndex", input.getFromIndex());
                UtxoOutputPo outputPo = outputDataService.get(keyMap);
                outputPo.setStatus((byte) UtxoOutput.USEABLE);
                outputDataService.updateStatus(outputPo);
                addressSet.add(outputPo.getAddress());

                UtxoOutput output = UtxoTransferTool.toOutput(outputPo);
                cacheService.putUtxo(output.getKey(), output);

                UtxoBalance balance = (UtxoBalance) cacheService.getBalance(outputPo.getAddress());
                if (balance == null) {
                    balance = new UtxoBalance(Na.valueOf(output.getValue()), Na.ZERO);
                }
                balance.addUnSpend(output);
            }

            for (String address : addressSet) {
                UtxoTransactionTool.getInstance().calcBalance(address);
            }

            relationDataService.deleteRelation(tx.getHash().getDigestHex(), addressSet);
        }
    }

    @Override
    public CoinData createByTransferData(Transaction tx, CoinTransferData coinParam, String password) throws NulsException {
        UtxoData utxoData = new UtxoData();
        List<UtxoInput> inputs = new ArrayList<>();
        List<UtxoOutput> outputs = new ArrayList<>();

        if (coinParam.getTotalNa().equals(Na.ZERO)) {
            utxoData.setInputs(inputs);
            utxoData.setOutputs(outputs);
            return utxoData;
        }

        long inputValue = 0;
        if (!coinParam.getFrom().isEmpty()) {
            //find unSpends to create inputs for this tx
            List<UtxoOutput> unSpends = coinManager.getAccountsUnSpend(coinParam.getFrom(), coinParam.getTotalNa().add(coinParam.getFee()));

            for (int i = 0; i < unSpends.size(); i++) {
                UtxoOutput output = unSpends.get(i);
                UtxoInput input = new UtxoInput();
                input.setFrom(output);
                input.setFromHash(output.getTxHash());
                input.setFromIndex(output.getIndex());
                input.setParent(tx);
                input.setIndex(i);
                inputValue += output.getValue();

                inputs.add(input);
            }
        }

        //get EcKey for output's script
        byte[] priKey = null;
        if (coinParam.getPriKey() != null) {
            priKey = coinParam.getPriKey();
        } else if (!coinParam.getFrom().isEmpty()) {
            Account account = accountService.getAccount(coinParam.getFrom().get(0));
            if(account == null) {
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

        //create scriptSig foreach input
        for (int i = 0; i < inputs.size(); i++) {
            P2PKHScriptSig scriptSig = new P2PKHScriptSig();
            ECKey ecKey = ECKey.fromPrivate(new BigInteger(priKey));

            Address address = new Address(NulsContext.getInstance().getChainId(NulsContext.CHAIN_ID), Utils.sha256hash160(ecKey.getPubKey(false)));
            scriptSig.setPublicKey(ecKey.getPubKey(false));

            NulsSignData signData = new NulsSignData();
            signData.setSignAlgType((short) 1);
            signData.setSignBytes(ecKey.sign(Sha256Hash.twiceOf(address.getHash160())).encodeToDER());
            scriptSig.setSignData(signData);
            try {
                inputs.get(i).setScriptSig(scriptSig.serialize());
            } catch (IOException e) {
                e.printStackTrace();
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
                output.setAddress(new Address(address).getHash());
                output.setValue(coin.getNa().getValue());
                output.setStatus(UtxoOutput.USEABLE);
                output.setIndex(i);
                P2PKHScript p2PKHScript = new P2PKHScript(NulsDigestData.calcDigestData((new Address(address).getHash160()), NulsDigestData.DIGEST_ALG_SHA160));
                output.setScript(p2PKHScript);
                if (coin.getUnlockHeight() > 0) {
                    output.setLockTime(coin.getUnlockHeight());
                } else if (coin.getUnlockTime() > 0) {
                    output.setLockTime(coin.getUnlockTime());
                } else {
                    output.setLockTime(0L);
                }
                output.setParent(tx);
                outputValue += output.getValue();
                outputs.add(output);
                i++;
            }
        }

        //the balance leave to myself
        long balance = inputValue - outputValue - coinParam.getFee().getValue();
        if (balance > 0) {
            UtxoOutput output = new UtxoOutput();
            //todo script
            output.setAddress(inputs.get(0).getFrom().getAddress());
            output.setValue(balance);
            output.setIndex(i);
            output.setParent(tx);
            output.setStatus(UtxoOutput.USEABLE);
            P2PKHScript p2PKHScript = new P2PKHScript(NulsDigestData.calcDigestData(ECKey.fromPrivate(new BigInteger(priKey)).getPubKey(false), NulsDigestData.DIGEST_ALG_SHA160));
            output.setScript(p2PKHScript);
            outputs.add(output);
        }

        utxoData.setInputs(inputs);
        utxoData.setOutputs(outputs);
        return utxoData;
    }

    @Override
    public void afterParse(CoinData coinData, Transaction tx) {
        UtxoData utxoData = (UtxoData) coinData;
        for (UtxoInput input : utxoData.getInputs()) {
            input.setTxHash(tx.getHash());
        }
        for(UtxoOutput output:utxoData.getOutputs()){
            output.setTxHash(tx.getHash());
        }
    }
}

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

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.db.dao.UtxoInputDataService;
import io.nuls.db.dao.UtxoOutputDataService;
import io.nuls.db.entity.UtxoInputPo;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.db.transactional.annotation.TransactionalAnnotation;
import io.nuls.ledger.entity.*;
import io.nuls.ledger.entity.params.Coin;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.service.intf.CoinDataProvider;
import io.nuls.ledger.util.UtxoTransferTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/21
 */
public class UtxoCoinDataProvider implements CoinDataProvider {

    private static final UtxoCoinDataProvider INSTANCE = new UtxoCoinDataProvider();

    private LedgerCacheService cacheService = LedgerCacheService.getInstance();

    private UtxoOutputDataService outputDataService;

    private UtxoInputDataService inputDataService;

    private UtxoCoinDataProvider() {

    }

    private UtxoCoinManager coinManager = UtxoCoinManager.getInstance();

    public static UtxoCoinDataProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public CoinData parse(NulsByteBuffer byteBuffer) throws NulsException {

        return byteBuffer.readNulsData(new UtxoData());
    }

    @Override
    public CoinTransferData getTransferData(CoinData coinData) {
        UtxoData utxoData = (UtxoData) coinData;

        return null;
    }

    @Override
    public void approve(CoinData coinData, Transaction tx) {
        UtxoData utxoData = (UtxoData) coinData;
        List<UtxoOutput> outputs = new ArrayList<>();
        try {
            for (int i = 0; i < utxoData.getInputs().size(); i++) {
                UtxoInput input = utxoData.getInputs().get(i);
                UtxoOutput unSpend = cacheService.getUtxo(input.getKey());
                unSpend.setStatus(UtxoOutput.LOCKED);
                outputs.add(unSpend);
                //todo address format
                Balance balance = cacheService.getBalance(new Address((short) 0, unSpend.getAddress()).getBase58());
                balance.setLocked(balance.getLocked().add(Na.valueOf(unSpend.getValue())));
                balance.setUseable(balance.getUseable().subtract(Na.valueOf(unSpend.getValue())));
            }
        } catch (Exception e) {
            for (UtxoOutput output : outputs) {
                cacheService.updateUtxoStatus(output.getKey(), UtxoOutput.USEABLE, UtxoOutput.LOCKED);
            }
            throw e;
        }
    }

    @Override
    @TransactionalAnnotation
    public void save(CoinData coinData, Transaction tx) {
        UtxoData utxoData = (UtxoData) coinData;

        List<UtxoInputPo> inputPoList = new ArrayList<>();
        List<UtxoOutput> spends = new ArrayList<>();
        List<UtxoOutputPo> spendPoList = new ArrayList<>();

        int step = 0;
        try {
            processDataInput(utxoData, inputPoList, spends, spendPoList);
            step = 1;

            List<UtxoOutputPo> outputPoList = new ArrayList<>();
            for (int i = 0; i < utxoData.getOutputs().size(); i++) {
                UtxoOutput output = utxoData.getOutputs().get(i);
                outputPoList.add(UtxoTransferTool.toOutputPojo(output));
            }

            outputDataService.updateStatus(spendPoList);
            inputDataService.save(inputPoList);
            outputDataService.save(outputPoList);

            processDataOutput(utxoData);
            step = 2;

            for (UtxoOutput spend : spends) {
                UtxoBalance balance = (UtxoBalance) cacheService.getBalance(new Address(NulsContext.getInstance().getChainId(NulsContext.CHAIN_ID), spend.getAddress()).getBase58());
                System.out.println("------------balance contains output:" + balance.containsSpend(spend.getKey()));
                cacheService.removeUtxo(spend.getKey());
                System.out.println("------------balance contains output:" + balance.containsSpend(spend.getKey()));
                if (balance.containsSpend(spend.getKey())) {
                    System.out.println("---------------------------spend status:" + spend.getStatus());
                }
            }
        } catch (Exception e) {
            //rollback
            if (step >= 2) {
                rollBackProcessDataOutput(utxoData);
            }

            for (UtxoOutput spend : spends) {
                spend.setStatus(UtxoOutput.LOCKED);
                UtxoBalance balance = (UtxoBalance) cacheService.getBalance(new Address(NulsContext.getInstance().getChainId(NulsContext.CHAIN_ID), spend.getAddress()).getBase58());
                balance.setLocked(balance.getLocked().add(Na.valueOf(spend.getValue())));
                balance.setBalance(balance.getBalance().add(Na.valueOf(spend.getValue())));
            }
            throw e;
        }
    }


    private void processDataInput(UtxoData utxoData, List<UtxoInputPo> inputPoList, List<UtxoOutput> spends, List<UtxoOutputPo> spendPoList) {
        boolean update;
        for (int i = 0; i < utxoData.getInputs().size(); i++) {
            UtxoInput input = utxoData.getInputs().get(i);
            inputPoList.add(UtxoTransferTool.toInputPojo(input));

            //change utxo status
            UtxoOutput spend = cacheService.getUtxo(input.getKey());
            update = cacheService.updateUtxoStatus(input.getKey(), UtxoOutput.SPENT, UtxoOutput.LOCKED);
            if (!update) {
                throw new NulsRuntimeException(ErrorCode.UTXO_STATUS_CHANGE);
            }
            spends.add(spend);
            spendPoList.add(UtxoTransferTool.toOutputPojo(spend));
            //calc balance
            //todo address format
            UtxoBalance balance = (UtxoBalance) cacheService.getBalance(new Address(NulsContext.getInstance().getChainId(NulsContext.CHAIN_ID), spend.getAddress()).getBase58());

            balance.setLocked(balance.getLocked().subtract(Na.valueOf(spend.getValue())));
            balance.setBalance(balance.getBalance().subtract(Na.valueOf(spend.getValue())));


        }
    }

    private void processDataOutput(UtxoData utxoData) {
        // cache new unSpends and calc balance
        for (int i = 0; i < utxoData.getOutputs().size(); i++) {
            UtxoOutput output = utxoData.getOutputs().get(i);

            cacheService.putUtxo(output.getKey(), output);
            //todo address format
            String address = new Address(NulsContext.getInstance().getChainId(NulsContext.CHAIN_ID), output.getAddress()).getBase58();
            UtxoBalance balance = (UtxoBalance) cacheService.getBalance(address);
            if (balance == null) {
                balance = new UtxoBalance(Na.valueOf(output.getValue()), Na.ZERO);
                balance.addUnSpend(output);
                cacheService.putBalance(address, balance);
            } else {
                balance.setUseable(balance.getUseable().add(Na.valueOf(output.getValue())));
                balance.setBalance(balance.getBalance().add(Na.valueOf(output.getValue())));
                balance.addUnSpend(output);
            }
        }
    }

    private void rollBackProcessDataOutput(UtxoData utxoData) {
        for (int i = 0; i < utxoData.getOutputs().size(); i++) {
            UtxoOutput output = utxoData.getOutputs().get(i);
            cacheService.removeUtxo(output.getKey());

            //todo address format
            String address = new Address(NulsContext.getInstance().getChainId(NulsContext.CHAIN_ID), output.getAddress()).getBase58();
            UtxoBalance balance = (UtxoBalance) cacheService.getBalance(address);
            balance.setUseable(balance.getUseable().subtract(Na.valueOf(output.getValue())));
            balance.setBalance(balance.getBalance().subtract(Na.valueOf(output.getValue())));
            balance.addUnSpend(output);
        }
    }

    @Override
    @TransactionalAnnotation
    public void rollback(CoinData coinData, Transaction tx) {
        UtxoData utxoData = (UtxoData) coinData;
        if(utxoData==null){
            return;
        }
        if (TxStatusEnum.AGREED.equals(tx.getStatus())) {
            for (UtxoInput input : utxoData.getInputs()) {
                cacheService.updateUtxoStatus(input.getKey(), UtxoOutput.USEABLE, UtxoOutput.LOCKED);
            }
        } else if (tx.getStatus().equals(TxStatusEnum.CONFIRMED)) {
            String address;
            Map<String, Object> keyMap = new HashMap<>();
            //process output
            for (UtxoOutput output : utxoData.getOutputs()) {
                keyMap.clear();
                keyMap.put("txHash", output.getTxHash().getDigestHex());
                keyMap.put("outIndex", output.getIndex());
                UtxoOutputPo outputPo = outputDataService.get(keyMap);

                outputDataService.delete(keyMap);
                // if utxo not spent,should calc balance and clear cache
                if (outputPo.getStatus() != UtxoOutput.SPENT) {
                    cacheService.removeUtxo(output.getKey());

                    //todo address format
                    address = new Address((short) 1, output.getAddress()).getBase58();
                    UtxoBalance balance = (UtxoBalance) cacheService.getBalance(address);
                    if (outputPo.getStatus().equals(UtxoOutput.USEABLE)) {
                        balance.setBalance(balance.getBalance().subtract(Na.valueOf(outputPo.getValue())));
                        balance.setUseable(balance.getUseable().subtract(Na.valueOf(outputPo.getValue())));
                    } else {
                        balance.setBalance(balance.getBalance().subtract(Na.valueOf(outputPo.getValue())));
                        balance.setLocked(balance.getLocked().subtract(Na.valueOf(outputPo.getValue())));
                    }
                }
            }

            //process input
            for (UtxoInput input : utxoData.getInputs()) {
                keyMap.clear();
                keyMap.put("txHash", input.getTxHash().getDigestHex());
                keyMap.put("fromIndex", input.getFromIndex());
                keyMap.put("outIndex", input.getFromIndex());
                inputDataService.delete(keyMap);

                UtxoOutputPo outputPo = outputDataService.get(keyMap);
                outputPo.setStatus((byte) UtxoOutput.USEABLE);
                outputDataService.updateStatus(outputPo);

                UtxoOutput output = UtxoTransferTool.toOutput(outputPo);
                cacheService.putUtxo(output.getKey(), output);

                //calc balance
                address = new Address((short)1, output.getAddress()).getBase58();
                UtxoBalance balance = (UtxoBalance) cacheService.getBalance(address);
                if (balance == null) {
                    balance = new UtxoBalance(Na.valueOf(output.getValue()), Na.ZERO);
                    balance.addUnSpend(output);
                }
            }

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
                inputs.add(input);
                input.setParent(tx);
                input.setIndex(i);
                inputValue += output.getValue();
            }
        }


        int i = 0;
        long outputValue = 0;
        for (Map.Entry<String, Coin> entry : coinParam.getToMap().entrySet()) {
            UtxoOutput output = new UtxoOutput();
            String address = entry.getKey();
            Coin coin = entry.getValue();
            output.setAddress(new Address(address).getHash());
            output.setValue(coin.getNa().getValue());
            output.setIndex(i);
            if (coin.getUnlockHeight() > 0) {
                output.setLockTime(coin.getUnlockHeight());
            } else {
                output.setLockTime(coin.getUnlockTime());
            }
            output.setParent(tx);
            outputValue += output.getValue();
            outputs.add(output);
            i++;
        }

        //the balance leave to myself
        long balance = inputValue - outputValue - coinParam.getFee().getValue();
        if (balance > 0) {
            UtxoOutput output = new UtxoOutput();
            output.setAddress(inputs.get(0).getFrom().getAddress());
            output.setValue(balance);
            output.setIndex(i);
            outputs.add(output);
        }

        utxoData.setInputs(inputs);
        utxoData.setOutputs(outputs);
        return utxoData;
    }

}

package io.nuls.ledger.service.impl;

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.CoinData;
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.UtxoInput;
import io.nuls.ledger.entity.UtxoOutput;
import io.nuls.ledger.entity.params.Coin;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.service.intf.CoinDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/21
 */
public class UtxoCoinDataProvider implements CoinDataProvider {

    private static final UtxoCoinDataProvider INSTANCE = new UtxoCoinDataProvider();

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
        return null;
    }

    @Override
    public void approve(CoinData coinData, String txHash) {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void save(CoinData coinData, String txHash) {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void rollback(CoinData coinData, String txHash) {
        // todo auto-generated method stub(niels)

    }

    @Override
    public CoinData createTransferData(Transaction tx, CoinTransferData coinParam, String password) throws NulsException {
        UtxoData utxoData = new UtxoData();
        List<UtxoInput> inputs = new ArrayList<>();
        List<UtxoOutput> outputs = new ArrayList<>();

        //find unSpends to create inputs for this tx
        List<UtxoOutput> unSpends = coinManager.getAccountsUnSpend(coinParam.getFrom(), coinParam.getTotalNa().add(coinParam.getFee()));
        if (unSpends.isEmpty()) {
            throw new NulsException(ErrorCode.BALANCE_NOT_ENOUGH);
        }

        long inputValue = 0;
        for (UtxoOutput output : unSpends) {
            UtxoInput input = new UtxoInput();
            input.setFrom(output);
            inputs.add(input);
            input.setParent(tx);
            inputValue += output.getValue();
        }

        int i = 0;
        long outputValue = 0;
        for (Map.Entry<String, Coin> entry : coinParam.getToMap().entrySet()) {
            UtxoOutput output = new UtxoOutput();
            String address = entry.getKey();
            Coin coin = entry.getValue();
            output.setAddress(new Address(address).getHash160());
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
            output.setAddress(unSpends.get(0).getAddress());
            output.setValue(balance);
            output.setIndex(i);
            outputs.add(output);
        }

        utxoData.setInputs(inputs);
        utxoData.setOutputs(outputs);
        return utxoData;
    }


}

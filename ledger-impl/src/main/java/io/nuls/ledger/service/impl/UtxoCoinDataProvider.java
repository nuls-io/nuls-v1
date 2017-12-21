package io.nuls.ledger.service.impl;

import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.CoinData;
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.service.intf.CoinDataProvider;

/**
 * @author Niels
 * @date 2017/12/21
 */
public class UtxoCoinDataProvider implements CoinDataProvider {

    @Override
    public CoinData parse(NulsByteBuffer byteBuffer) {
        UtxoData data = new UtxoData();
        data.parse(byteBuffer);
        return data;
    }

    @Override
    public CoinTransferData getTransferData(CoinData coinData) {
        // todo auto-generated method stub(niels)
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
    public void createTransferData(CoinTransferData coinParam, String password) {
        // todo auto-generated method stub(niels)

    }
}

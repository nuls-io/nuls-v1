package io.nuls.ledger.service.impl;

import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.CoinData;
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.params.CoinBaseData;
import io.nuls.ledger.entity.params.LockData;
import io.nuls.ledger.entity.params.TransferData;
import io.nuls.ledger.entity.params.UnlockData;
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
    public CoinData createLockCoinData(LockData txData, String password) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public CoinData createTransferCoinData(TransferData params, String password) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public CoinData createUnlockCoinData(UnlockData txData, String password) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public CoinData createCoinBaseCoinData(CoinBaseData txData, String password) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public LockData getLockData(CoinData coinData) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public UnlockData getUnlockData(CoinData coinData) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public TransferData getTransferData(CoinData coinData) {
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
}

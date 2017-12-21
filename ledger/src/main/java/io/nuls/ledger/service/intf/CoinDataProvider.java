package io.nuls.ledger.service.intf;

import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.CoinData;
import io.nuls.ledger.entity.params.CoinTransferData;

/**
 * @author Niels
 * @date 2017/12/21
 */
public interface CoinDataProvider {

    CoinData parse(NulsByteBuffer byteBuffer);

    CoinData createLockCoinData(CoinTransferData txData, String password);

    CoinData createTransferCoinData(CoinTransferData params, String password);

    CoinData createUnlockCoinData(CoinTransferData txData, String password);

    CoinData createCoinBaseCoinData(CoinTransferData txData, String password);

    CoinTransferData getTransferData(CoinData coinData);

    void approve(CoinData coinData,String txHash);

    void save(CoinData coinData,String txHash);

    void rollback(CoinData coinData,String txHash);


}

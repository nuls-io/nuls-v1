package io.nuls.ledger.service.intf;

import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.CoinData;
import io.nuls.ledger.entity.params.CoinBaseData;
import io.nuls.ledger.entity.params.LockData;
import io.nuls.ledger.entity.params.TransferData;
import io.nuls.ledger.entity.params.UnlockData;

/**
 * @author Niels
 * @date 2017/12/21
 */
public interface CoinDataProvider {

    CoinData parse(NulsByteBuffer byteBuffer);

    CoinData createLockCoinData(LockData txData, String password);

    CoinData createTransferCoinData(TransferData params, String password);

    CoinData createUnlockCoinData(UnlockData txData, String password);

    CoinData createCoinBaseCoinData(CoinBaseData txData, String password);

    LockData getLockData(CoinData coinData);
    UnlockData getUnlockData(CoinData coinData);
    TransferData getTransferData(CoinData coinData);
}

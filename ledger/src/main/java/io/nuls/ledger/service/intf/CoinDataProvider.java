package io.nuls.ledger.service.intf;

import io.nuls.ledger.entity.CoinData;
import io.nuls.ledger.entity.LockData;

/**
 * @author Niels
 * @date 2017/12/21
 */
public interface CoinDataProvider {
    CoinData getLockCoinData(LockData txData);
}

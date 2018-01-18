package io.nuls.ledger.service.intf;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.CoinData;
import io.nuls.ledger.entity.params.CoinTransferData;

/**
 * @author Niels
 * @date 2017/12/21
 */
public interface CoinDataProvider {

    CoinData parse(NulsByteBuffer byteBuffer) throws NulsException;

    CoinTransferData getTransferData(CoinData coinData);

    void approve(CoinData coinData, String txHash);

    void save(CoinData coinData, String txHash);

    void rollback(CoinData coinData, String txHash);

    CoinData createTransferData(Transaction tx, CoinTransferData coinParam, String password) throws NulsException;
}

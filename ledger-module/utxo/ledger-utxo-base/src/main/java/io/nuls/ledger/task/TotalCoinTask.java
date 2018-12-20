package io.nuls.ledger.task;

import io.nuls.core.tools.calc.DoubleUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.db.model.Entry;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.model.Coin;
import io.nuls.ledger.storage.service.UtxoLedgerUtxoStorageService;

import java.util.List;

public class TotalCoinTask implements Runnable {

    @Autowired
    private UtxoLedgerUtxoStorageService utxoLedgerUtxoStorageService;

    @Override
    public void run() {
        try {
            doStatistics();
        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void doStatistics() throws NulsException {
        long height = NulsContext.getInstance().getBestHeight();
        List<Entry<byte[], byte[]>> coinBytesList = getUtxoLedgerUtxoStorageService().getAllUtxoEntryBytes();
        long totalNuls = 0;
        long lockedNuls = 0;
        Coin coin = new Coin();
        for (Entry<byte[], byte[]> coinEntryBytes : coinBytesList) {
            coin.parse(coinEntryBytes.getValue(), 0);

            totalNuls += coin.getNa().getValue();
            if (coin.getLockTime() == -1 || coin.getLockTime() > System.currentTimeMillis() || (coin.getLockTime() < 1531152000000L && coin.getLockTime() > height)) {
                lockedNuls += coin.getNa().getValue();
            }
        }
        NulsContext.totalNuls = totalNuls;
        NulsContext.lockedNuls = lockedNuls;
    }

    private UtxoLedgerUtxoStorageService getUtxoLedgerUtxoStorageService() {
        if (utxoLedgerUtxoStorageService == null) {
            utxoLedgerUtxoStorageService = NulsContext.getServiceBean(UtxoLedgerUtxoStorageService.class);
        }
        return utxoLedgerUtxoStorageService;
    }
}

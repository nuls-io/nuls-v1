package io.nuls.ledger.service.impl;

import io.nuls.core.chain.entity.Na;
import io.nuls.db.dao.UtxoOutputDataService;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.ledger.entity.UtxoBalance;
import io.nuls.ledger.entity.UtxoOutput;
import io.nuls.ledger.util.UtxoTransferTool;

import java.util.ArrayList;
import java.util.List;

/**
 * cache and operate all unspend utxo
 */
public class UtxoCoinManager {

    private static UtxoCoinManager instance = new UtxoCoinManager();

    private UtxoCoinManager() {
        cacheService = LedgerCacheService.getInstance();
    }

    public static UtxoCoinManager getInstance() {
        return instance;
    }

    private LedgerCacheService cacheService;


    private UtxoOutputDataService outputDataService;


    public void cacheAllUnSpendOutPut() {
        List<UtxoOutputPo> utxoOutputPos = outputDataService.getAllUnSpend();
        String address = null;
        long userable = 0;
        long lock = 0;
        UtxoOutput output;
        UtxoOutputPo po;
        UtxoBalance balance;
        List<UtxoOutput> list = new ArrayList<>();

        for (int i = 0; i < utxoOutputPos.size(); i++) {
            po = utxoOutputPos.get(i);
            output = UtxoTransferTool.toOutput(po);
            if (i == 0) {
                address = po.getAddress();
            } else if (!address.equals(po.getAddress())) {
                balance = new UtxoBalance();
                balance.setUseable(Na.valueOf(userable));
                balance.setLocked(Na.valueOf(lock));
                balance.setBalance(balance.getUseable().add(balance.getLocked()));
                balance.setUnSpends(list);
                cacheService.putBalance(address, balance);

                userable = 0;
                lock = 0;
                list = new ArrayList<>();
            }
            if (po.getStatus() == 0) {
                userable += po.getValue();
            } else if (po.getStatus() == 1) {
                lock += po.getValue();
            }
            list.add(output);
        }
    }

    public void setOutputDataService(UtxoOutputDataService outputDataService) {
        this.outputDataService = outputDataService;
    }
}

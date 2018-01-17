package io.nuls.ledger.service.impl;

import io.nuls.core.chain.entity.Na;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.UtxoOutputDataService;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.ledger.entity.UtxoBalance;
import io.nuls.ledger.entity.UtxoOutput;
import io.nuls.ledger.util.UtxoTransferTool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    private Lock lock = new ReentrantLock();

    public void cacheAllUnSpendOutPut() {
        List<UtxoOutputPo> utxoOutputPos = outputDataService.getAllUnSpend();
        String address = null;
        long useable = 0;
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
                balance.setUseable(Na.valueOf(useable));
                balance.setLocked(Na.valueOf(lock));
                balance.setBalance(balance.getUseable().add(balance.getLocked()));
                balance.setUnSpends(list);
                cacheService.putBalance(address, balance);

                useable = 0;
                lock = 0;
                list = new ArrayList<>();
            }
            if (po.getStatus() == 0) {
                useable += po.getValue();
            } else if (po.getStatus() == 1) {
                lock += po.getValue();
            }
            list.add(output);
        }
    }

    /**
     * Utxo is used to ensure that each transaction will not double
     *
     * @param address
     * @param value
     * @return
     */
    public List<UtxoOutput> getAccountUnSpend(String address, Na value) {
        lock.lock();
        List<UtxoOutput> unSpends = new ArrayList<>();
        try {
            UtxoBalance balance = (UtxoBalance) cacheService.getBalance(address);
            if (balance == null || balance.getUseable().isLessThan(value)) {
                return unSpends;
            }

            boolean enough = false;
            Na amount = Na.ZERO;
            for (int i = 0; i < balance.getUnSpends().size(); i++) {
                UtxoOutput output = balance.getUnSpends().get(i);
                output.setStatus(output.LOCKED);
                unSpends.add(output);
                amount = amount.add(Na.valueOf(output.getValue()));
                if (amount.isGreaterThan(value)) {
                    enough = true;
                    break;
                }
            }
            if(!enough) {
                for (UtxoOutput output : unSpends) {
                    output.setStatus(output.USEABLE);
                }
                unSpends = new ArrayList<>();
            }
        } catch (Exception e) {
            Log.error(e);
            for (UtxoOutput output : unSpends) {
                output.setStatus(output.USEABLE);
            }
        } finally {
            lock.unlock();
        }
        return unSpends;
    }

    public List<UtxoOutput> getAccountsUnSpend(List<String> addressList, Na value) {
        lock.lock();
        List<UtxoOutput> unSpends = new ArrayList<>();
        try {
            //check use-able is enough , find unSpend utxo
            Na amount = Na.ZERO;
            boolean enough = false;
            for (String address : addressList) {
                UtxoBalance balance = (UtxoBalance) cacheService.getBalance(address);
                for (int i = 0; i < balance.getUnSpends().size(); i++) {
                    UtxoOutput output = balance.getUnSpends().get(i);
                    output.setStatus(output.LOCKED);
                    unSpends.add(output);
                    amount = amount.add(Na.valueOf(output.getValue()));
                    if (amount.isGreaterThan(value)) {
                        enough = true;
                        break;
                    }
                }
                if (enough) {
                    break;
                }
            }
            if (!enough) {
                for (UtxoOutput output : unSpends) {
                    output.setStatus(output.USEABLE);
                }
                unSpends = new ArrayList<>();
            }
        } catch (Exception e) {
            Log.error(e);
            for (UtxoOutput output : unSpends) {
                output.setStatus(output.USEABLE);
            }
            unSpends = new ArrayList<>();
        } finally {
            lock.unlock();
        }
        return unSpends;
    }

    public void setOutputDataService(UtxoOutputDataService outputDataService) {
        this.outputDataService = outputDataService;
    }
}

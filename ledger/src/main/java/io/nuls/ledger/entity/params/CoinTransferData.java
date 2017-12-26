package io.nuls.ledger.entity.params;

import io.nuls.account.constant.AccountConstant;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.context.NulsContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/21
 */
public class CoinTransferData {

    private Map<String, Na> fromMap;
    private Map<String, Na> toMap;

    private Na totalNa;

    private Na fee;

    private long unlockTime;

    private int unlockHeight;

    private boolean canBeUnlocked;

    public Map<String, Na> getFromMap() {
        return fromMap;
    }

    public void setFromMap(Map<String, Na> fromMap) {
        this.fromMap = fromMap;
    }

    public Map<String, Na> getToMap() {
        return toMap;
    }

    public void setToMap(Map<String, Na> toMap) {
        this.toMap = toMap;
    }

    public Na getTotalNa() {
        return totalNa;
    }

    public void setTotalNa(Na totalNa) {
        this.totalNa = totalNa;
    }

    public Na getFee() {
        return fee;
    }

    public void setFee(Na fee) {
        this.fee = fee;
    }

    public long getUnlockTime() {
        return unlockTime;
    }

    public void setUnlockTime(long unlockTime) {
        this.unlockTime = unlockTime;
    }

    public int getUnlockHeight() {
        return unlockHeight;
    }

    public void setUnlockHeight(int unlockHeight) {
        this.unlockHeight = unlockHeight;
    }

    public boolean isCanBeUnlocked() {
        return canBeUnlocked;
    }

    public void setCanBeUnlocked(boolean canBeUnlocked) {
        this.canBeUnlocked = canBeUnlocked;
    }

    public void addFrom(String address, Na na) {
        if (null == this.fromMap) {
            this.fromMap = new HashMap<>();
        }
        this.fromMap.put(address, na);
    }

    public void addTo(String address, Na na) {
        if (null == this.toMap) {
            this.toMap = new HashMap<>();
        }
        this.toMap.put(address, na);
    }

    public static CoinTransferData getBaseUnlockCoinData(String address, Na na) {
        CoinTransferData coinData = new CoinTransferData();
        coinData.setFee(NulsContext.getInstance().getTxFee());
        coinData.setCanBeUnlocked(false);
        coinData.setUnlockHeight(0);
        coinData.setUnlockTime(0);
        coinData.setTotalNa(na);
        coinData.addFrom(address, na);
        return coinData;
    }

    public static CoinTransferData getLockHeightCoinData(String address, Na na, int unlockHeight) {
        CoinTransferData coinData =getBaseUnlockCoinData(address, na);
        coinData.setCanBeUnlocked(true);
        coinData.setUnlockHeight(unlockHeight);
        return coinData;
    }

    public static CoinTransferData getLockTimeCoinData(String address, Na na, long unlockTime) {
        CoinTransferData coinData =getBaseUnlockCoinData(address, na);
        coinData.setCanBeUnlocked(true);
        coinData.setUnlockTime(unlockTime);
        return coinData;
    }
}

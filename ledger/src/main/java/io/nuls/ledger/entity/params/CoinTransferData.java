package io.nuls.ledger.entity.params;

import io.nuls.core.chain.entity.Na;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.str.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/21
 */
public class CoinTransferData {

    private Map<String, Na> fromMap;

    private Map<String, Coin> toMap;

    private Na totalNa;

    private Na fee;

    public CoinTransferData() {
        this.fromMap = new HashMap<>();
        this.toMap = new HashMap<>();
        this.fee = NulsContext.getInstance().getTxFee();
    }

    public CoinTransferData(Na totalNa) {
        this();
        this.totalNa = totalNa;
    }

    public CoinTransferData(Na totalNa, String from, String to) {
        this(totalNa);
        if (StringUtils.isNotBlank(from)) {
            this.addFrom(from, totalNa);
        }
        if (StringUtils.isNotBlank(to)) {
            this.addTo(to, new Coin(totalNa));
        }
    }

    public Map<String, Na> getFromMap() {
        return fromMap;
    }

    public void setFromMap(Map<String, Na> fromMap) {
        this.fromMap = fromMap;
    }

    public Map<String, Coin> getToMap() {
        return toMap;
    }

    public void setToMap(Map<String, Coin> toMap) {
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

    public void addFrom(String address, Na na) {
        this.fromMap.put(address, na);
    }

    public void addTo(String address, Coin coin) {
        this.toMap.put(address, coin);
    }

}

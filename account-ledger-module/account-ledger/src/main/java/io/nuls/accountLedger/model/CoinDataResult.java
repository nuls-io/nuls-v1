package io.nuls.accountLedger.model;

import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;

import java.util.List;

/**
 * 获取coinData时，包含coin集合以及需要花费的手续费
 *
 * @author Vivi
 */
public class CoinDataResult {

    private boolean enough;

    private List<Coin> coinList;
    /**
     * 找零
     */
    private Coin change;

    private Na fee;

    public List<Coin> getCoinList() {
        return coinList;
    }

    public void setCoinList(List<Coin> coinList) {
        this.coinList = coinList;
    }

    public Na getFee() {
        return fee;
    }

    public void setFee(Na fee) {
        this.fee = fee;
    }

    public boolean isEnough() {
        return enough;
    }

    public void setEnough(boolean enough) {
        this.enough = enough;
    }

    public Coin getChange() {
        return change;
    }

    public void setChange(Coin change) {
        this.change = change;
    }
}

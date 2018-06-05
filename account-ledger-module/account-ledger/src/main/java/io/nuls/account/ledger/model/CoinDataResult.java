/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.account.ledger.model;

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

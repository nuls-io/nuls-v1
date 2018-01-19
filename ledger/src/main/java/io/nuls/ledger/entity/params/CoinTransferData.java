/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.ledger.entity.params;

import io.nuls.core.chain.entity.Na;
import io.nuls.core.context.NulsContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/21
 */
public class CoinTransferData {

    private List from;

    private Map<String, Coin> toMap;

    private Na totalNa;

    private Na fee;

    public CoinTransferData() {
        this.from = new ArrayList();
        this.toMap = new HashMap<>();
        this.fee = NulsContext.getInstance().getTxFee();
    }

    public CoinTransferData(Na totalNa) {
        this();
        this.totalNa = totalNa;
    }

    public CoinTransferData(Na totalNa, String from) {
        this(totalNa);
        this.addFrom(from, totalNa);
    }

    public CoinTransferData(Na totalNa, String from, String to) {
        this(totalNa);
        this.addFrom(from, totalNa);
        this.addTo(to, new Coin(totalNa));
    }

    public CoinTransferData(Na totalNa, List<String> from) {
        this(totalNa);
        this.from = from;
    }

    public CoinTransferData(Na totalNa, List<String> from, String to) {
        this(totalNa);
        this.from = from;
        this.addTo(to, new Coin(totalNa));
    }

    public void setFrom(List<String> from) {
        this.from = from;
    }

    public List<String> getFrom() {
        return from;
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
        this.from.add(address);
    }

    public void addTo(String address, Coin coin) {
        this.toMap.put(address, coin);
    }

}

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

import io.nuls.core.utils.str.StringUtils;
import io.nuls.protocol.model.Na;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/21
 */
public class CoinTransferData {

    private OperationType type;

    private byte[] priKey;

    private List<String> from;

    private List<Coin> toList = new ArrayList<>();


    private Na totalNa;

    private Na fee;

    private Map<String, Object> specialData;

    public CoinTransferData() {
        this.from = new ArrayList();
        this.totalNa = Na.ZERO;
        this.fee = Na.ZERO;
    }

    public CoinTransferData(OperationType type, Na fee) {
        this();
        this.type = type;
        this.fee = fee;
    }

    public CoinTransferData(OperationType type, Na totalNa, Na fee) {
        this(type, fee);
        this.totalNa = totalNa;
    }

    public CoinTransferData(OperationType type, Na totalNa, String from, Na fee) {
        this(type, totalNa, fee);
        this.addFrom(from);
    }

    public CoinTransferData(OperationType type, Na totalNa, String from, String to, Na fee) {
        this(type, totalNa, from, fee);
        if (StringUtils.isNotBlank(to)) {
            this.addTo(new Coin(to, totalNa, 0, 0));
        }
    }

    public CoinTransferData(OperationType type, Na totalNa, List<String> from, Na fee) {
        this(type, totalNa);
        this.setFrom(from);
    }

    public CoinTransferData(OperationType type, Na totalNa, List<String> from, String to, Na fee) {
        this(type, totalNa, from, fee);
        if (StringUtils.isNotBlank(to)) {
            this.addTo(new Coin(to, totalNa, 0, 0));
        }
    }

    public void setFrom(List<String> from) {
        this.from = from;
    }

    public List<String> getFrom() {
        return from;
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

    public void addFrom(String address) {
        this.from.add(address);
    }

    public void addTo(Coin coin) {
        toList.add(coin);
    }

    public byte[] getPriKey() {
        return priKey;
    }

    public void setPriKey(byte[] priKey) {
        this.priKey = priKey;
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public Map<String, Object> getSpecialData() {
        return specialData;
    }

    public void setSpecialData(Map<String, Object> specialData) {
        this.specialData = specialData;
    }

    public List<Coin> getToCoinList() {
        return toList;
    }

}

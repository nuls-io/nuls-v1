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

package io.nuls.account.ledger.sdk.model;

import io.nuls.sdk.utils.StringUtils;

import java.util.Map;

public class OutputDto {

    /**
     * 交易hash
     */
    private String txHash;

    /**
     * 索引
     */
    private int index;

    /**
     * 地址
     */
    private String address;

    /**
     * 数量
     */
    private long value;

    /**
     * 锁定时间
     */
    private long lockTime;

    /**
     * 状态 0:usable(未花费), 1:timeLock(高度锁定), 2:consensusLock(参与共识锁定), 3:spent(已花费)
     */
    private int status;

    public OutputDto() {

    }

    public OutputDto(Map<String, Object> map) {
        this.txHash = (String) map.get("txHash");
        this.index = (Integer) map.get("index");
        this.address = (String) map.get("address");
        this.value = StringUtils.parseLong(map.get("value"));
        this.lockTime = StringUtils.parseLong(map.get("lockTime"));
        this.status = (Integer) map.get("status");
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

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
package io.nuls.consensus.entity;

import io.nuls.account.entity.Account;
import io.nuls.core.chain.entity.Na;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class ConsensusStatusInfo implements Serializable {
    private Account account;
    private int status;
    private long startTime;
    private int parkedCount;
    private Na accumulativeReward;
    private Map<String, Object> extend = new HashMap<>();

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void putExtend(String key, Object value) {
        extend.put(key, value);
    }

    public void removeExtend(String key) {
        extend.remove(key);
    }

    public Object getExtendValue(String key) {
        return extend.get(key);
    }

    public Map<String, Object> getExtend() {
        return extend;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Na getAccumulativeReward() {
        return accumulativeReward;
    }

    public void setAccumulativeReward(Na accumulativeReward) {
        this.accumulativeReward = accumulativeReward;
    }

    public int getParkedCount() {
        return parkedCount;
    }

    public void setParkedCount(int parkedCount) {
        this.parkedCount = parkedCount;
    }
}

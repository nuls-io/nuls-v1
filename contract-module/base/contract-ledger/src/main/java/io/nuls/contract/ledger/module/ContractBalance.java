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
package io.nuls.contract.ledger.module;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.core.tools.map.MapUtil;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/7
 */
public class ContractBalance implements Serializable {

    private Na balance;

    private Na locked;

    private Na usable;

    private Na usableConsensusReward;

    private LinkedHashMap<String, Coin> consensusRewardCoins;

    public ContractBalance() {
        this.locked = Na.ZERO;
        this.usable = Na.ZERO;
        this.usableConsensusReward = Na.ZERO;
        this.consensusRewardCoins = MapUtil.createLinkedHashMap(64);
    }

    public ContractBalance(Na usable, Na locked, Na usableConsensusReward, LinkedHashMap<String, Coin> lockedCoins) {
        if (usable == null) {
            usable = Na.ZERO;
        }
        if (locked == null) {
            locked = Na.ZERO;
        }
        if (usableConsensusReward == null) {
            usableConsensusReward = Na.ZERO;
        }
        this.usable = usable;
        this.locked = locked;
        this.usableConsensusReward = usableConsensusReward;
        this.consensusRewardCoins = lockedCoins;

    }

    public Na getBalance() {
        this.balance = this.getRealUsable().add(locked);
        return balance;
    }

    public void setLocked(Na locked) {
        this.locked = locked;
    }

    public Na getLocked() {
        return locked;
    }

    @JsonIgnore
    public Na getRealUsable() {
        if (usableConsensusReward == null) {
            usableConsensusReward = Na.ZERO;
        }
        return usable.add(usableConsensusReward);
    }

    public Na getUsable() {
        return usable;
    }

    @JsonIgnore
    public LinkedHashMap<String, Coin> getConsensusRewardCoins() {
        return consensusRewardCoins;
    }

    public void addLocked(Na locked) {
        this.locked = this.locked.add(locked);
    }

    public void addUsable(Na usable) {
        this.usable = this.usable.add(usable);
    }

    public void minusUsable(Na usable) {
        this.usable = this.usable.minus(usable);
    }

    public Na getUsableConsensusReward() {
        return usableConsensusReward;
    }

    public void setUsableConsensusReward(Na usableConsensusReward) {
        this.usableConsensusReward = usableConsensusReward;
    }

    public void addUsableConsensusReward(Na usableConsensusReward) {
        this.usableConsensusReward = this.usableConsensusReward.add(usableConsensusReward);
    }

}
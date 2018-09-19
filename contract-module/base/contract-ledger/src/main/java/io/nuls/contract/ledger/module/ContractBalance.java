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

import io.nuls.kernel.model.Na;

import java.io.Serializable;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/7
 */
public class ContractBalance implements Serializable {

    private Na balance;

    private Na locked;

    private Na usable;

    public ContractBalance() {
        this.balance = Na.ZERO;
        this.locked = Na.ZERO;
        this.usable = Na.ZERO;
    }

    public ContractBalance(Na usable, Na locked) {
        if(usable == null) {
            usable = Na.ZERO;
        }
        if(locked == null) {
            locked = Na.ZERO;
        }
        this.usable = usable;
        this.locked = locked;
        this.balance = locked.add(usable);
    }

    public Na getBalance() {
        return balance;
    }

    public void setBalance(Na balance) {
        this.balance = balance;
    }

    public Na getLocked() {
        return locked;
    }

    public void setLocked(Na locked) {
        this.locked = locked;
    }

    public Na getUsable() {
        return usable;
    }

    public void setUsable(Na usable) {
        this.usable = usable;
    }

    public void addLocked(Na locked) {
        this.locked = this.locked.add(locked);
        this.balance = this.locked.add(usable);
    }

    public void addUsable(Na usable) {
        this.usable = this.usable.add(usable);
        this.balance = this.usable.add(locked);
    }

    public void minusLocked(Na locked) {
        this.locked = this.locked.minus(locked);
        this.balance = this.locked.add(usable);
    }

    public void minusUsable(Na usable) {
        this.usable = this.usable.minus(usable);
        this.balance = this.usable.add(locked);
    }
}

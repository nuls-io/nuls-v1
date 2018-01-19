/**
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
 */
package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.intf.NulsCloneable;

import java.io.Serializable;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class Balance implements Serializable, NulsCloneable {

    private Na balance;

    private Na locked;

    private Na useable;

    public Balance() {

    }

    public Balance(Na useable, Na locked) {
        this.useable = useable;
        this.locked = locked;
        this.balance = locked.add(useable);
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

    public Na getUseable() {
        return useable;
    }

    public void setUseable(Na useable) {
        this.useable = useable;
    }

    @Override
    public Object copy() {
        Balance obj = new Balance();
        obj.setBalance(balance);
        obj.setLocked(locked);
        obj.setUseable(this.useable);
        return obj;
    }
}

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

/**
 * @author Niels
 * @date 2017/12/26
 */
public class Coin {

    public Coin() {

    }

    public Coin(Na na) {
        this.na = na;
    }

    public Coin(Na na, long unlockTime) {
        this(na);
        this.unlockTime = unlockTime;
    }

    public Coin(Na na, int unlockHeight) {
        this(na);
        this.unlockHeight = unlockHeight;
    }

    private Na na;

    private long unlockTime;

    private long unlockHeight;

    private boolean canBeUnlocked;

    public Na getNa() {
        return na;
    }

    public void setNa(Na na) {
        this.na = na;
    }

    public long getUnlockTime() {
        return unlockTime;
    }

    public void setUnlockTime(long unlockTime) {
        this.unlockTime = unlockTime;
    }

    public long getUnlockHeight() {
        return unlockHeight;
    }

    public void setUnlockHeight(long unlockHeight) {
        this.unlockHeight = unlockHeight;
    }

    public boolean isCanBeUnlocked() {
        return canBeUnlocked;
    }

    public void setCanBeUnlocked(boolean canBeUnlocked) {
        this.canBeUnlocked = canBeUnlocked;
    }

}

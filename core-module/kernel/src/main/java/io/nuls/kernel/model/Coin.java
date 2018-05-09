/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.kernel.model;

import io.protostuff.Tag;

/**
 * Created by ln on 2018/5/5.
 */
public class Coin extends BaseNulsData {
    @Tag(1)
    private byte[] owner;
    @Tag(2)
    private Na na;
    @Tag(3)
    private long lockTime;

    private transient Coin from;

    public Coin(byte[] owner, Na na, long lockTime) {
        this.owner = owner;
        this.na = na;
        this.lockTime = lockTime;
    }

    public Na getNa() {
        return na;
    }

    public byte[] getOwner() {
        return owner;
    }

    public long getLockTime() {
        return lockTime;
    }

    public Coin getFrom() {
        return from;
    }

    public void setFrom(Coin from) {
        this.from = from;
    }
}
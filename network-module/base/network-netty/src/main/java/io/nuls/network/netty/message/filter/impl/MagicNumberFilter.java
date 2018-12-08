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

package io.nuls.network.netty.message.filter.impl;

import io.nuls.network.netty.message.filter.NulsMessageFilter;
import io.nuls.protocol.message.base.BaseMessage;
import io.nuls.protocol.message.base.MessageHeader;

import java.util.HashSet;
import java.util.Set;

public class MagicNumberFilter implements NulsMessageFilter {

    private Set<Long> magicSet = new HashSet<>();

    private static MagicNumberFilter instance = new MagicNumberFilter();

    private MagicNumberFilter() {

    }

    public static MagicNumberFilter getInstance() {
        return instance;
    }

    @Override
    public boolean filter(BaseMessage message) {
        MessageHeader header = message.getHeader();
        return magicSet.contains(header.getMagicNumber());
    }

    public void addMagicNum(Long magicNum) {
        magicSet.add(magicNum);
    }

    public void removeMagicNum(Long magicNum) {
        magicSet.remove(magicNum);
    }
}

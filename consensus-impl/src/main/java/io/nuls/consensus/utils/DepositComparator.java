/*
 *
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

package io.nuls.consensus.utils;

import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Deposit;

import java.util.Comparator;

/**
 * @author Niels
 * @date 2018/3/24
 */
public class DepositComparator implements Comparator<Consensus<Deposit>> {


    private static final DepositComparator INSTANCE  =  new DepositComparator();

    public static DepositComparator getInstance() {
        return INSTANCE;
    }

    private DepositComparator() {

    }

    @Override
    public int compare(Consensus<Deposit> o1, Consensus<Deposit> o2) {
        return (int) (o1.getExtend().getStartTime()-o2.getExtend().getStartTime());
    }
}

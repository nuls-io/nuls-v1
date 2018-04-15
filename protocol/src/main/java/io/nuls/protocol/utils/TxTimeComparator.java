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
 */
package io.nuls.protocol.utils;

import io.nuls.core.chain.entity.Transaction;

import java.util.Comparator;

/**
 * @author Niels
 * @date 2017/12/26
 */
public class TxTimeComparator implements Comparator<Transaction> {

    private static  final TxTimeComparator INSTANCE = new TxTimeComparator();
    private TxTimeComparator(){}
    public static TxTimeComparator getInstance(){
        return INSTANCE;
    }

    @Override
    public int compare(Transaction o1, Transaction o2) {
//        long key = o1.size() - o2.size();
        long key = o1.getTime() - o2.getTime();
        int val = 0;
        if (key > 0) {
            return 1;
        } else if (key < 0) {
            return -1;
        }
        return val;
    }
}

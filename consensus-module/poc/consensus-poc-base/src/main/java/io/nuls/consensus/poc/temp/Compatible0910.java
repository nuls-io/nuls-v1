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

package io.nuls.consensus.poc.temp;

import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Transaction;
import io.nuls.protocol.constant.ProtocolConstant;

import java.util.List;

/**
 * @author: Niels Wang
 * @date: 2018/6/21
 */
//todo
public class Compatible0910 {

    public static boolean isCompatible(Transaction tx) {
        if (tx.getType() == ProtocolConstant.TX_TYPE_COINBASE && tx.getBlockHeight() < 20000) {
            List<Coin> list = tx.getCoinData().getTo();
            boolean result = true;
            for (int i = 1; i < list.size(); i++) {
                result = list.get(i).getNa().equals(0D);
                if (!result) {
                    return result;
                }

            }
            return result;
        }
        return false;
    }
}

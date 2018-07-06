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
package io.nuls.consensus.poc.util;

import io.nuls.consensus.poc.container.TxContainer;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.NulsByteBuffer;

import java.util.Comparator;

/**
 * author Facjas
 * date 2018/5/23.
 */
public class TxContainerTimeComparator implements Comparator<TxContainer> {
    private static TxContainerTimeComparator instance = new TxContainerTimeComparator();

    private TxContainerTimeComparator() {

    }

    public static TxContainerTimeComparator getInstance() {
        return instance;
    }

    @Override
    public int compare(TxContainer obj1, TxContainer obj2) {
        Transaction o1 = obj1.getTx();
        Transaction o2 = obj2.getTx();
        if (o1.getTime() < o2.getTime()) {
            return -1;
        } else if (o1.getTime() > o2.getTime()) {
            return 1;
        } else {
            for (Coin coin : o2.getCoinData().getFrom()) {
                NulsByteBuffer buffer = new NulsByteBuffer(coin.getOwner());
                NulsDigestData hash = null;
                try {
                    hash = buffer.readHash();
                } catch (NulsException e) {
                    Log.error(e);
                }
                if (o1.getHash().equals(hash)) {
                    return -1;
                }
            }
            for (Coin coin : o1.getCoinData().getFrom()) {
                NulsByteBuffer buffer = new NulsByteBuffer(coin.getOwner());
                NulsDigestData hash = null;
                try {
                    hash = buffer.readHash();
                } catch (NulsException e) {
                    Log.error(e);
                }
                if (o2.getHash().equals(hash)) {
                    return 1;
                }
            }
        }
        return 0;
    }
}

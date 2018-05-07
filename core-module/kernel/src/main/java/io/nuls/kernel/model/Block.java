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
package io.nuls.kernel.model;

import io.nuls.core.tools.log.Log;
import io.protostuff.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * @author win10
 * @date 2017/10/30
 */
public class Block extends BaseNulsData implements NulsCloneable {

    @Tag(1)
    private BlockHeader header;
    @Tag(2)
    private List<Transaction> txs;

    public Block() {
    }

    public List<Transaction> getTxs() {
        return txs;
    }

    public void setTxs(List<Transaction> txs) {
        this.txs = txs;
    }

    public BlockHeader getHeader() {
        return header;
    }

    public void setHeader(BlockHeader header) {
        this.header = header;
    }


    @Override
    public Object copy() {
        //Temporary non realization
        try {
            return this.clone();
        } catch (CloneNotSupportedException e) {
            Log.error(e);
            return null;
        }
    }

    public List<NulsDigestData> getTxHashList() {
        List<NulsDigestData> list = new ArrayList<>();
        for (Transaction tx : txs) {
            list.add(tx.getHash());
        }
        return list;
    }
}

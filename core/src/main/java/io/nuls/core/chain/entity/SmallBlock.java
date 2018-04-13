/**
 * MIT License
 **
 * Copyright (c) 2017-2018 nuls.io
 **
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 **
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 **
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.core.chain.entity;

import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2018/1/2
 */
public class SmallBlock extends BaseNulsData {
    private BlockHeader header;
    private List<NulsDigestData> txHashList;
    private List<Transaction> subTxList = new ArrayList<>();

    @Override
    public int size() {
        int size = header.size();
        size += Utils.sizeOfVarInt(txHashList.size());
        for (NulsDigestData hash : txHashList) {
            size += Utils.sizeOfNulsData(hash);
        }
        size += Utils.sizeOfVarInt(subTxList.size());
        for (Transaction tx : subTxList) {
            size += Utils.sizeOfNulsData(tx);
        }
        return size;
    }

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(header);
        stream.writeVarInt(txHashList.size());
        for (NulsDigestData hash : txHashList) {
            stream.writeNulsData(hash);
        }
        stream.writeVarInt(subTxList.size());
        for (Transaction tx : subTxList) {
            stream.writeNulsData(tx);
        }
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.header = byteBuffer.readNulsData(new BlockHeader());

        this.txHashList = new ArrayList<>();
        long hashListSize = byteBuffer.readVarInt();
        for (int i = 0; i < hashListSize; i++) {
            this.txHashList.add(byteBuffer.readHash());
        }

        this.subTxList = new ArrayList<>();
        long subTxListSize = byteBuffer.readVarInt();
        for (int i = 0; i < subTxListSize; i++) {
            Transaction tx = byteBuffer.readTransaction();
            tx.setBlockHeight(header.getHeight());
            this.subTxList.add(tx);
        }
    }

    public BlockHeader getHeader() {
        return header;
    }

    public void setHeader(BlockHeader header) {
        this.header = header;
    }

    public List<NulsDigestData> getTxHashList() {
        return txHashList;
    }

    public void setTxHashList(List<NulsDigestData> txHashList) {
        this.txHashList = txHashList;
    }

    public List<Transaction> getSubTxList() {
        return subTxList;
    }

    public void addConsensusTx(Transaction tx) {
        this.subTxList.add(tx);
    }

}

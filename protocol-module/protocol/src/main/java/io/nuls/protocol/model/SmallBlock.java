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
package io.nuls.protocol.model;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 小区块，用于新区块打包完成后进行广播，小区块中包含区块头、块中交易hash列表、打包过程中产生的交易（其他节点一定没有的交易）
 * Block block, used for broadcasting after the new block is packaged,
 * and the blocks in the block are included in the block header ,tx hash list of the block
 * and the transaction generated in the packaging process (other transactions that must not be made by other nodes).
 *
 * @author Niels
 * @date 2018/1/2
 */
public class SmallBlock extends BaseNulsData {
    /**
     * 区块头
     * block header
     */

    private BlockHeader header;

    /**
     * 交易摘要列表
     * transaction hash list
     */

    private List<NulsDigestData> txHashList;

    /**
     * 共识交易列表（其他节点一定没有的交易）
     * Consensus trading list (transactions that no other node must have)
     */

    private List<Transaction> subTxList = new ArrayList<>();

    public SmallBlock() {
    }

    @Override
    public int size() {
        int size = header.size();
        size += SerializeUtils.sizeOfVarInt(txHashList.size());
        for (NulsDigestData hash : txHashList) {
            size += SerializeUtils.sizeOfNulsData(hash);
        }
        size += SerializeUtils.sizeOfVarInt(subTxList.size());
        for (Transaction tx : subTxList) {
            size += SerializeUtils.sizeOfNulsData(tx);
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

    /**
     * 区块头
     * block header
     */
    public BlockHeader getHeader() {
        return header;
    }

    public void setHeader(BlockHeader header) {
        this.header = header;
    }

    /**
     * 交易摘要列表
     * transaction hash list
     */
    public List<NulsDigestData> getTxHashList() {
        return txHashList;
    }

    public void setTxHashList(List<NulsDigestData> txHashList) {
        this.txHashList = txHashList;
    }

    /**
     * 共识交易列表（其他节点一定没有的交易）
     * Consensus trading list (transactions that no other node must have)
     */
    public List<Transaction> getSubTxList() {
        return subTxList;
    }

    public void addBaseTx(Transaction tx) {
        this.subTxList.add(tx);
    }

}

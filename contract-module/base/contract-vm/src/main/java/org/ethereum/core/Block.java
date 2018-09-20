/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.core;

import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.toHexString;

/**
 * The block in Ethereum is the collection of relevant pieces of information
 * (known as the blockheader), H, together with information corresponding to
 * the comprised transactions, R, and a set of other blockheaders U that are known
 * to have a parent equal to the present block’s parent’s parent
 * (such blocks are known as uncles).
 *
 * @author Roman Mandeleil
 * @author Nick Savers
 * @since 20.05.2014
 */
public class Block {

    private static final Logger logger = LoggerFactory.getLogger("blockchain");

    private BlockHeader header;

    /* Uncles */
    private List<BlockHeader> uncleList = new CopyOnWriteArrayList<>();

    /* Private */

    private byte[] rlpEncoded;
    private boolean parsed = false;

    /* Constructors */

    private Block() {
    }

    public Block(byte[] rawData) {
        logger.debug("new from [" + toHexString(rawData) + "]");
        this.rlpEncoded = rawData;
    }

    private synchronized void parseRLP() {
        if (parsed) {
            return;
        }

        RLPList params = RLP.decode2(rlpEncoded);
        RLPList block = (RLPList) params.get(0);

        // Parse Header
        RLPList header = (RLPList) block.get(0);
        this.header = new BlockHeader(header);

        // Parse Transactions
        RLPList txTransactions = (RLPList) block.get(1);
        //this.parseTxs(this.header.getTxTrieRoot(), txTransactions, false);

        // Parse Uncles
        RLPList uncleBlocks = (RLPList) block.get(2);
        for (RLPElement rawUncle : uncleBlocks) {

            RLPList uncleHeader = (RLPList) rawUncle;
            BlockHeader blockData = new BlockHeader(uncleHeader);
            this.uncleList.add(blockData);
        }
        this.parsed = true;
    }

    public BlockHeader getHeader() {
        parseRLP();
        return this.header;
    }

    public byte[] getHash() {
        parseRLP();
        return this.header.getHash();
    }

    public byte[] getParentHash() {
        parseRLP();
        return this.header.getParentHash();
    }

    public byte[] getUnclesHash() {
        parseRLP();
        return this.header.getUnclesHash();
    }

    public byte[] getCoinbase() {
        parseRLP();
        return this.header.getCoinbase();
    }

    public byte[] getStateRoot() {
        parseRLP();
        return this.header.getStateRoot();
    }

    public void setStateRoot(byte[] stateRoot) {
        parseRLP();
        this.header.setStateRoot(stateRoot);
    }

    public byte[] getTxTrieRoot() {
        parseRLP();
        return this.header.getTxTrieRoot();
    }

    public byte[] getReceiptsRoot() {
        parseRLP();
        return this.header.getReceiptsRoot();
    }


    public byte[] getLogBloom() {
        parseRLP();
        return this.header.getLogsBloom();
    }

    public byte[] getDifficulty() {
        parseRLP();
        return this.header.getDifficulty();
    }

    public BigInteger getDifficultyBI() {
        parseRLP();
        return this.header.getDifficultyBI();
    }


    public BigInteger getCumulativeDifficulty() {
        parseRLP();
        BigInteger calcDifficulty = new BigInteger(1, this.header.getDifficulty());
        for (BlockHeader uncle : uncleList) {
            calcDifficulty = calcDifficulty.add(new BigInteger(1, uncle.getDifficulty()));
        }
        return calcDifficulty;
    }

    public long getTimestamp() {
        parseRLP();
        return this.header.getTimestamp();
    }

    public long getNumber() {
        parseRLP();
        return this.header.getNumber();
    }

    public byte[] getGasLimit() {
        parseRLP();
        return this.header.getGasLimit();
    }

    public long getGasUsed() {
        parseRLP();
        return this.header.getGasUsed();
    }


    public byte[] getExtraData() {
        parseRLP();
        return this.header.getExtraData();
    }

    public byte[] getMixHash() {
        parseRLP();
        return this.header.getMixHash();
    }


    public byte[] getNonce() {
        parseRLP();
        return this.header.getNonce();
    }

    public void setNonce(byte[] nonce) {
        this.header.setNonce(nonce);
        rlpEncoded = null;
    }

    public void setMixHash(byte[] hash) {
        this.header.setMixHash(hash);
        rlpEncoded = null;
    }

    public void setExtraData(byte[] data) {
        this.header.setExtraData(data);
        rlpEncoded = null;
    }

    public List<BlockHeader> getUncleList() {
        parseRLP();
        return uncleList;
    }

    private StringBuffer toStringBuff = new StringBuffer();
    // [parent_hash, uncles_hash, coinbase, state_root, tx_trie_root,
    // difficulty, number, minGasPrice, gasLimit, gasUsed, timestamp,
    // extradata, nonce]

    /**
     * check if param block is son of this block
     *
     * @param block - possible a son of this
     * @return - true if this block is parent of param block
     */
    public boolean isParentOf(Block block) {
        return Arrays.areEqual(this.getHash(), block.getParentHash());
    }

    public boolean isGenesis() {
        return this.header.isGenesis();
    }

    public boolean isEqual(Block block) {
        return Arrays.areEqual(this.getHash(), block.getHash());
    }

    private byte[] getUnclesEncoded() {

        byte[][] unclesEncoded = new byte[uncleList.size()][];
        int i = 0;
        for (BlockHeader uncle : uncleList) {
            unclesEncoded[i] = uncle.getEncoded();
            ++i;
        }
        return RLP.encodeList(unclesEncoded);
    }

    public void addUncle(BlockHeader uncle) {
        uncleList.add(uncle);
        this.getHeader().setUnclesHash(sha3(getUnclesEncoded()));
        rlpEncoded = null;
    }

    public byte[] getEncoded() {
        return rlpEncoded;
    }

    public byte[] getEncodedWithoutNonce() {
        parseRLP();
        return this.header.getEncodedWithoutNonce();
    }

    public String getShortHash() {
        parseRLP();
        return Hex.toHexString(getHash()).substring(0, 6);
    }

    public static class Builder {

        private BlockHeader header;
        private byte[] body;

        public Builder withHeader(BlockHeader header) {
            this.header = header;
            return this;
        }

        public Builder withBody(byte[] body) {
            this.body = body;
            return this;
        }
    }

}

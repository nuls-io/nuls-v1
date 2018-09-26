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
import org.ethereum.util.RLPList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.Arrays;

import java.util.ArrayList;
import java.util.List;

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

    public Block(byte[] parentHash, byte[] hash, long number) {
        this.header = new BlockHeader(parentHash, hash, number);
        this.parsed = true;
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

    public long getNumber() {
        parseRLP();
        return this.header.getNumber();
    }

    public boolean isEqual(Block block) {
        return Arrays.areEqual(this.getHash(), block.getHash());
    }

    public byte[] getEncoded() {
        if (rlpEncoded == null) {
            byte[] header = this.header.getEncoded();

            List<byte[]> block = getBodyElements();
            block.add(0, header);
            byte[][] elements = block.toArray(new byte[block.size()][]);

            this.rlpEncoded = RLP.encodeList(elements);
        }
        return rlpEncoded;
    }

    private List<byte[]> getBodyElements() {
        parseRLP();

        List<byte[]> body = new ArrayList<>();

        return body;
    }

}

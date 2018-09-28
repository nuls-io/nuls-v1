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

import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.util.Arrays;

import java.math.BigInteger;

import static org.ethereum.util.ByteUtil.toHexString;

/**
 * Block header is a value object containing
 * the basic information of a block
 */
public class BlockHeader {

    private byte[] parentHash;
    private byte[] hash;
    private long number;

    public BlockHeader(byte[] encoded) {
        this((RLPList) RLP.decode2(encoded).get(0));
    }

    public BlockHeader(RLPList rlpHeader) {
        this.parentHash = rlpHeader.get(0).getRLPData();
        this.hash = rlpHeader.get(1).getRLPData();
        byte[] nrBytes = rlpHeader.get(2).getRLPData();
        this.number = ByteUtil.byteArrayToLong(nrBytes);
    }

    public BlockHeader(byte[] parentHash, byte[] hash, long number) {
        this.parentHash = parentHash;
        this.hash = hash;
        this.number = number;
    }

    public byte[] getParentHash() {
        return parentHash;
    }

    public long getNumber() {
        return number;
    }

    public byte[] getHash() {
        return hash;
    }

    public byte[] getEncoded() {
        return this.getEncoded(true); // with nonce
    }

    public byte[] getEncoded(boolean withNonce) {
        byte[] parentHash = RLP.encodeElement(this.parentHash);
        byte[] hash = RLP.encodeElement(this.hash);
        byte[] number = RLP.encodeBigInteger(BigInteger.valueOf(this.number));
        return RLP.encodeList(parentHash, hash, number);
    }

    @Override
    public String toString() {
        return toStringWithSuffix("\n");
    }

    private String toStringWithSuffix(final String suffix) {
        StringBuilder toStringBuff = new StringBuilder();
        toStringBuff.append("  hash=").append(toHexString(hash)).append(suffix);
        toStringBuff.append("  parentHash=").append(toHexString(parentHash)).append(suffix);
        toStringBuff.append("  number=").append(number).append(suffix);
        return toStringBuff.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BlockHeader that = (BlockHeader) o;
        return FastByteComparisons.equal(getHash(), that.getHash());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getHash());
    }
}

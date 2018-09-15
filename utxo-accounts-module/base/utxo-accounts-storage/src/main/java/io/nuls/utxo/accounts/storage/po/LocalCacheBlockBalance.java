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
package io.nuls.utxo.accounts.storage.po;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalCacheBlockBalance extends BaseNulsData{
    private long blockHeight;
    private NulsDigestData hash;
    private NulsDigestData preHash;
    private List<UtxoAccountsBalancePo> balanceList=new ArrayList<>();

    public NulsDigestData getHash() {
        return hash;
    }

    public void setHash(NulsDigestData hash) {
        this.hash = hash;
    }

    public NulsDigestData getPreHash() {
        return preHash;
    }

    public void setPreHash(NulsDigestData preHash) {
        this.preHash = preHash;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public List<UtxoAccountsBalancePo> getBalanceList() {
        return balanceList;
    }

    public void setBalanceList(List<UtxoAccountsBalancePo> balanceList) {
        this.balanceList = balanceList;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeInt64(blockHeight);
        stream.write(this.getHash().serialize());
        stream.write(this.getPreHash().serialize());
        int balanceListCount = balanceList == null ? 0 : balanceList.size();
        stream.writeVarInt(balanceListCount);
        if (null != balanceList) {
            for (UtxoAccountsBalancePo balance : balanceList) {
                stream.writeBytesWithLength(balance.getOwner());
                stream.writeInt64(balance.getInputBalance());
                stream.writeInt64(balance.getOutputBalance());
                stream.writeInt64(balance.getLockedPermanentBalance());
                stream.writeInt64(balance.getUnLockedPermanentBalance());
                stream.writeInt64(balance.getContractFromBalance());
                stream.writeInt64(balance.getContractToBalance());
                stream.writeInt64(balance.getBlockHeight());
                stream.writeUint32(balance.getTxIndex());
                int lockedTimeListSize = balance.getLockedTimeList() == null ? 0 :  balance.getLockedTimeList() .size();
                stream.writeVarInt(lockedTimeListSize);
                if (null !=  balance.getLockedTimeList() ) {
                    for (LockedBalance lockedBalance :  balance.getLockedTimeList() ) {
                        stream.writeInt64(lockedBalance.getLockedTime());
                        stream.writeInt64(lockedBalance.getLockedBalance());
                    }
                }
                int lockedHeightListSize = balance.getLockedHeightList() == null ? 0 :  balance.getLockedHeightList().size();
                stream.writeVarInt(lockedHeightListSize);
                if (null !=  balance.getLockedHeightList()) {
                    for (LockedBalance lockedBalance :  balance.getLockedHeightList()) {
                        stream.writeInt64(lockedBalance.getLockedTime());
                        stream.writeInt64(lockedBalance.getLockedBalance());
                    }
                }
            }
        }

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.blockHeight=byteBuffer.readInt64();
        this.hash=new NulsDigestData( byteBuffer.readByte(),byteBuffer.readByLengthByte());
        this.preHash=new NulsDigestData( byteBuffer.readByte(),byteBuffer.readByLengthByte());
        int listCount = (int) byteBuffer.readVarInt();
        if (0 < listCount) {
            List<UtxoAccountsBalancePo> list = new ArrayList<>();
            for (int i = 0; i < listCount; i++) {
                list.add(byteBuffer.readNulsData(new UtxoAccountsBalancePo()));
            }
            this.balanceList = list;
        }
    }

    @Override
    public int size() {
        int size=0;
        size += SerializeUtils.sizeOfInt64();
        size+=this.hash.size();
        size+=this.preHash.size();
        size+= SerializeUtils.sizeOfVarInt(balanceList == null ? 0 : balanceList.size());
        if (null != balanceList) {
            for (UtxoAccountsBalancePo balance : balanceList) {
                size += balance.size();
            }
        }
        return size;
    }
}

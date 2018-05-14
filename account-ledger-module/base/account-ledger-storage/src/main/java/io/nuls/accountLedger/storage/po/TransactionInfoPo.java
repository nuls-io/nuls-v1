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

package io.nuls.accountLedger.storage.po;

import io.nuls.account.model.Address;
import io.nuls.accountLedger.model.TransactionInfo;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;

import java.io.IOException;
import java.util.List;

/**
 * author Facjas
 * date 2018/5/10.
 */
public class TransactionInfoPo extends BaseNulsData {

    private NulsDigestData txHash;

    private long blockHeight;

    private long time;

    private byte[] addresses;

    private int txType;

    public TransactionInfoPo() {

    }

    public TransactionInfoPo(Transaction tx) {
        if (tx == null) {
            return;
        }
        this.txHash = tx.getHash();
        this.blockHeight = tx.getBlockHeight();
        this.time = tx.getTime();
        List<byte[]> addressList = tx.getAllRelativeAddress();

        byte[] addresses = new byte[addressList.size() * Address.size()];
        for (int i = 0; i < addressList.size(); i++) {
            System.arraycopy(addressList.get(i), 0, addressList, Address.size() * i, Address.size());
        }
        this.addresses = addresses;

        this.txType = tx.getType();
    }


    public TransactionInfoPo(TransactionInfo txInfo) {
        if (txInfo == null) {
            return;
        }
        //todo  check weather need to clone the object
        this.txHash = txInfo.getTxHash();
        this.blockHeight = txInfo.getBlockHeight();
        this.time = txInfo.getTime();
        this.addresses = txInfo.getAddresses();
        this.txType = txInfo.getTxType();
    }

    public TransactionInfo toTransactionInfo() {
        //todo  check weather need to clone the object
        TransactionInfo txInfo = new TransactionInfo();
        txInfo.setTxHash(this.txHash);
        txInfo.setBlockHeight(this.blockHeight);
        txInfo.setTime(this.time);
        txInfo.setAddresses(this.addresses);
        txInfo.setTxType(this.txType);
        return txInfo;
    }

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        // todo auto-generated method stub

    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        // todo auto-generated method stub

    }

    @Override
    public int size() {
        // todo auto-generated method stub
        return 0;
    }

    public NulsDigestData getTxHash() {
        return txHash;
    }

    public void setTxHash(NulsDigestData txHash) {
        this.txHash = txHash;
    }

    public byte[] getAddresses() {
        return addresses;
    }

    public void setAddresses(byte[] addresses) {
        this.addresses = addresses;
    }
}

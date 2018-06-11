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

package io.nuls.account.ledger.storage.po;

import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.TransactionManager;

import java.io.IOException;

/**
 *
 * @author ln
 * @date 2018-06-11
 */
public class UnconfirmedTxPo extends BaseNulsData {

    private Transaction tx;
    private long sequence;

    public UnconfirmedTxPo() {
    }

    public UnconfirmedTxPo(Transaction tx, long sequence) {
        this.tx = tx;
        this.sequence = sequence;
    }

    public UnconfirmedTxPo(byte[] txBytes) {
        super();
        try {
            parse(txBytes);
        } catch (NulsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int size() {
        return tx.size() + 8;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        tx.serializeToStream(stream);
        stream.writeInt64(sequence);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        try {
            tx = TransactionManager.getInstance(byteBuffer);
        } catch (Exception e) {
            Log.info("Load local transaction Error");
        }
        sequence = byteBuffer.readInt64();
    }

    public Transaction getTx() {
        return tx;
    }

    public void setTx(Transaction tx) {
        this.tx = tx;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }
}

/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.consensus.poc.protocol.tx;

import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.NulsSignData;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.script.SignatureUtil;


import java.io.IOException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * @author: Niels Wang
 */
public class TxSerializeTest {

    protected ECKey ecKey1 = new ECKey();
    protected ECKey ecKey2 = new ECKey();
    protected ECKey ecKey3 = new ECKey();

    protected void setCommonFields(Transaction tx) {
        tx.setTime(System.currentTimeMillis());
        tx.setBlockHeight(1);
        tx.setRemark("for test".getBytes());
    }

    protected void signTransaction(Transaction tx, ECKey ecKey) {
        NulsDigestData hash = null;
        try {
            hash = NulsDigestData.calcDigestData(tx.serializeForHash());
        } catch (IOException e) {
            Log.error(e);
        }
        tx.setHash(hash);

        List<ECKey> keys = new ArrayList<>();
        keys.add(ecKey);

        try {
            SignatureUtil.createTransactionSignture(tx, null, keys);
        } catch (Exception e) {
            Log.error(e);
        }
    }

    public void testTxSerialize(Transaction tx, Transaction nullTx) throws NulsException, IOException {
        byte[] bytes = tx.serialize();

        assertEquals(bytes.length, tx.size());

        nullTx.parse(bytes, 0);

        assertEquals(bytes.length, nullTx.size());

        assertTrue(Arrays.equals(bytes, nullTx.serialize()));

    }
}

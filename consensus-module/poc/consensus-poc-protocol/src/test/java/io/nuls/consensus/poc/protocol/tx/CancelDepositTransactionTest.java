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

package io.nuls.consensus.poc.protocol.tx;

import io.nuls.consensus.poc.protocol.entity.CancelDeposit;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.utils.AddressTool;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author: Niels Wang
 * @date: 2018/5/12
 */
public class CancelDepositTransactionTest extends TxSerializeTest {


    @Test
    public void testSerialize() {
        CancelDepositTransaction tx = new CancelDepositTransaction();
        this.setCommonFields(tx);
        CancelDeposit txData = new CancelDeposit();
        txData.setAddress(AddressTool.getAddress(ecKey1.getPubKey()));
        txData.setJoinTxHash(NulsDigestData.calcDigestData("1234567890".getBytes()));
        this.signTransaction(tx, ecKey1);
        try {
            this.testTxSerialize(tx, new CancelDepositTransaction());
        } catch (Exception e) {
            assertTrue(false);
        }
    }

}
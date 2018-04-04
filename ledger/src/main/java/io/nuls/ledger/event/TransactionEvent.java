/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.ledger.event;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.NoticeData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.constant.LedgerConstant;

/**
 * @author Niels
 * @date 2017/11/8
 */
public class TransactionEvent extends io.nuls.core.event.BaseEvent<Transaction> {

    public TransactionEvent() {
        super(NulsConstant.MODULE_ID_LEDGER, LedgerConstant.EVENT_TYPE_TRANSACTION);
    }

    @Override
    protected Transaction parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        try {
            return TransactionManager.getInstance(byteBuffer);
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    @Override
    public NoticeData getNotice() {
        NoticeData data = new NoticeData();
        data.setMessage(ErrorCode.NEW_TX_RECIEVED);
        data.setData(this.getEventBody().getHash().getDigestHex());
        return data;
    }

    @Override
    public boolean needToRemoveDuplication() {
        return true;
    }
}

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
package io.nuls.sdk.model;

import io.nuls.sdk.constant.SDKConstant;
import io.nuls.sdk.constant.TransactionConstant;
import io.nuls.sdk.exception.NulsException;
import io.nuls.sdk.utils.AddressTool;
import io.nuls.sdk.utils.NulsByteBuffer;

import java.util.Arrays;

/**
 * @author Niels
 * @date 2017/11/20
 */
public class TransferTransaction extends Transaction {

    public TransferTransaction() {
        this(TransactionConstant.TX_TYPE_TRANSFER);
    }

    protected TransferTransaction(int type) {
        super(type);
    }

    @Override
    public String getInfo(byte[] address) {
        boolean isTransfer = false;
        byte[] addressOwner = new byte[AddressTool.HASH_LENGTH];
        Coin to = coinData.getTo().get(0);
        System.arraycopy(to.getOwner(), 0, addressOwner, 0, AddressTool.HASH_LENGTH);
        if (!Arrays.equals(address, addressOwner)) {
            isTransfer = true;
        }
        if (isTransfer) {
            return "-" + to.getNa().toCoinString();
        } else {
            return "+" + to.getNa().toCoinString();
        }
    }

    @Override
    protected TransactionLogicData parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        byteBuffer.readBytes(SDKConstant.PLACE_HOLDER.length);
        return null;
    }

}

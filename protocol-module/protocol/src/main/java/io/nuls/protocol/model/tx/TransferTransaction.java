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
package io.nuls.protocol.model.tx;

import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.*;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.protocol.constant.ProtocolConstant;

import java.util.Arrays;

/**
 * @author Niels
 * @date 2017/11/20
 */
public class TransferTransaction extends Transaction {

    public TransferTransaction() {
        this(ProtocolConstant.TX_TYPE_TRANSFER);
    }

    protected TransferTransaction(int type) {
        super(type);
    }

    @Override
    public String getInfo(byte[] address) {
        CoinData coinData = this.getCoinData();

        boolean isTransfer = false;
        Na to = Na.ZERO;
        byte[] addressOwner = new byte[AddressTool.HASH_LENGTH];
        for (Coin coin : coinData.getFrom()) {
            System.arraycopy(coin.getFrom().getOwner(), 0, addressOwner, 0, AddressTool.HASH_LENGTH);
            if (Arrays.equals(address, addressOwner)) {
                isTransfer = true;
                break;
            }
        }
        for (Coin coin : coinData.getTo()) {
            System.arraycopy(coin.getFrom().getOwner(), 0, addressOwner, 0, AddressTool.HASH_LENGTH);
            if (isTransfer && !Arrays.equals(address, addressOwner)) {
                to = to.add(coin.getNa());
            } else if (Arrays.equals(address, addressOwner)) {
                to = to.add(coin.getNa());
            }
        }
        if (isTransfer) {
            return "" + (-1 * to.getValue());
        }
        return "" + to.getValue();
    }

    @Override
    protected TransactionLogicData parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        byteBuffer.readBytes(NulsConstant.PLACE_HOLDER.length);
        return null;
    }

}

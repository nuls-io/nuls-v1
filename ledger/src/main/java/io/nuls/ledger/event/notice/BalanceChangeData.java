/*
 *
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

package io.nuls.ledger.event.notice;

import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.protocol.model.BaseNulsData;
import io.nuls.protocol.model.Na;
import io.nuls.protocol.utils.io.NulsByteBuffer;
import io.nuls.protocol.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2018/3/8
 */
public class BalanceChangeData extends BaseNulsData {

    private String address;
    /**
     * 0 income,1 expenditure
     */
    private int type;
    /**
     * 0 Unconfirmed,1 Confirmed
     */
    private int status;
    private Na amount;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getType() {
        return type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Na getAmount() {
        return amount;
    }

    public void setAmount(Na amount) {
        this.amount = amount;
    }

    @Override
    public int size() {
        int size = 0;
        size += Utils.sizeOfString(address);
        size += 1;
        size += 1;
        size += Utils.sizeOfVarInt(amount.getValue());
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(address);
        stream.write(type);
        stream.write(status);
        stream.writeVarInt(amount.getValue());

    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
       this.address = byteBuffer.readString();
       this.type = byteBuffer.readByte();
       this.status = byteBuffer.readByte();
       this.amount = Na.valueOf(byteBuffer.readVarInt());
    }
}

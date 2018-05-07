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
 */
package io.nuls.protocol.message;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.protocol.constant.ProtocolMessageType;
import io.nuls.protocol.message.base.NoticeData;
import io.nuls.protocol.model.SmallBlock;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class SmallBlockMessage extends BaseProtocolMessage<SmallBlock> {
    public SmallBlockMessage() {
        super(ProtocolMessageType.NEW_BLOCK);
    }

    public SmallBlockMessage(SmallBlock newBlock) {
        this();
        this.setMsgBody(newBlock);
    }

    @Override
    public NoticeData getNotice() {
        if (null == this.getMsgBody()) {
            Log.warn("recieved a null SmallBlock!");
            return null;
        }
        NoticeData data = new NoticeData();
        data.setMessage(KernelErrorCode.NEW_BLOCK_HEADER_RECIEVED);
        data.setData(this.getMsgBody().getHeader().getHash());
        return data;
    }

}

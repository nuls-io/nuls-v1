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
package io.nuls.protocol.event;

import io.nuls.protocol.constant.ConsensusEventType;
import io.nuls.core.chain.entity.SmallBlock;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.event.NoticeData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class SmallBlockEvent extends BaseConsensusEvent<SmallBlock> {
    public SmallBlockEvent() {
        super(ConsensusEventType.NEW_BLOCK);
    }

    public SmallBlockEvent(SmallBlock newBlock) {
        this();
        this.setEventBody(newBlock);
    }

    @Override
    protected SmallBlock parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new SmallBlock());
    }

    @Override
    public NoticeData getNotice() {
        if (null == this.getEventBody()) {
            Log.warn("recieved a null SmallBlock!");
            return null;
        }
        NoticeData data = new NoticeData();
        data.setMessage(ErrorCode.NEW_BLOCK_HEADER_RECIEVED);
        data.setData(this.getEventBody().getHeader().getHash());
        return data;
    }

}

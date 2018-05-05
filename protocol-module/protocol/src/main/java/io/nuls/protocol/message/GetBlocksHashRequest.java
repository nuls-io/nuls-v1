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
package io.nuls.protocol.message;

import io.nuls.kernel.constant.NulsConstant;
import io.nuls.protocol.constant.ProtocolEventType;
import io.nuls.protocol.message.base.BaseMessage;
import io.nuls.protocol.message.base.NoticeData;
import io.nuls.protocol.model.GetBlocksHashParam;

/**
 * @author Niels
 * @date 2018/1/15
 */
public class GetBlocksHashRequest extends BaseMessage<GetBlocksHashParam> {


    public GetBlocksHashRequest() {
        super(NulsConstant.MODULE_ID_CONSENSUS, ProtocolEventType.EVENT_TYPE_GET_BLOCKS_HASH);
    }

    public GetBlocksHashRequest(long start, long size) {
        this();
        GetBlocksHashParam param = new GetBlocksHashParam();
        param.setStart(start);
        param.setSize(size);
        this.setMsgBody(param);
    }

    @Override
    public NoticeData getNotice() {
        return null;
    }

}

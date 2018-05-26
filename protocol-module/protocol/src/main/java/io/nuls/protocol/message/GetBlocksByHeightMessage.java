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

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.protocol.constant.ProtocolConstant;
import io.nuls.protocol.model.GetBlocksByHeightParam;

/**
 * 获取区块的消息
 * The message for get block or blocks
 *
 * @author Niels
 * @date 2017/11/13
 */
public class GetBlocksByHeightMessage extends BaseProtocolMessage<GetBlocksByHeightParam> {

    public GetBlocksByHeightMessage() {
        super(ProtocolConstant.MESSAGE_TYPE_GET_BLOCKS_BY_HEIGHT);
    }

    public GetBlocksByHeightMessage(long startHeight, long endHeight) {
        this();
        GetBlocksByHeightParam param = new GetBlocksByHeightParam(startHeight, endHeight);
        setMsgBody(param);
    }

    @Override
    protected GetBlocksByHeightParam parseMessageBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new GetBlocksByHeightParam());
    }
}

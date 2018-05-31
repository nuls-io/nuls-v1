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

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.protocol.constant.ProtocolConstant;
import io.nuls.protocol.model.GetBlocksHashParam;

/**
 * 从对等节点处获取区块头摘要列表的消息
 * The message of gets the block header summary list from the peer node.
 *
 * @author Niels
 * @date 2018/1/15
 */
public class GetBlocksHashMessage extends BaseProtocolMessage<GetBlocksHashParam> {

    public GetBlocksHashMessage() {
        super(ProtocolConstant.MESSAGE_TYPE_GET_BLOCKS_HASH);
    }

    @Override
    protected GetBlocksHashParam parseMessageBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new GetBlocksHashParam());
    }

    public GetBlocksHashMessage(long startHeight, long endHeight) {
        this();
        GetBlocksHashParam param = new GetBlocksHashParam(startHeight, endHeight);
        this.setMsgBody(param);
    }
}

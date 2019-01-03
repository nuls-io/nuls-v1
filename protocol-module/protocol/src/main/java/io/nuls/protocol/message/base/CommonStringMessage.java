/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.protocol.message.base;

import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.protocol.constant.ProtocolConstant;
import io.nuls.protocol.message.BaseProtocolMessage;
import io.nuls.protocol.model.basic.NulsStringData;

/**
 * 消息体只有字符串的消息类
 * The message body has only a string of message classes.
 *
 * @author Niels
 */
public class CommonStringMessage extends BaseProtocolMessage<NulsStringData> {

    public CommonStringMessage() {
        super(ProtocolConstant.PROTOCOL_STRING);
    }

    @Override
    protected NulsStringData parseMessageBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new NulsStringData());
    }

    public void setMessage(String message) {
        if (StringUtils.isBlank(message)) {
            return;
        }
        this.setMsgBody(new NulsStringData(message));
    }

    public String getMessage() {
        NulsStringData data = this.getMsgBody();
        if (null == data) {
            return null;
        }
        return data.getVal();
    }
}

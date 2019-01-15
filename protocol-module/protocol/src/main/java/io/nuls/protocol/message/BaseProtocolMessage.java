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
package io.nuls.protocol.message;

import io.nuls.kernel.model.BaseNulsData;
import io.nuls.protocol.constant.ProtocolConstant;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * 协议模块消息基类，用于规范本模块的所有消息
 * The protocol module message base class is used to normalize all messages for this module.
 *
 * @author Niels
 */
public abstract class BaseProtocolMessage<T extends BaseNulsData> extends BaseMessage<T> {

    /**
     * 构造方法默认了模块id，实现者只需要关注自己的消息类型
     * The constructor defaults to the module id, and the implementer only needs to focus on the message type.
     *
     * @param messageType 消息类型
     */
    public BaseProtocolMessage(short messageType) {
        super(ProtocolConstant.MODULE_ID_PROTOCOL, messageType);
    }

}

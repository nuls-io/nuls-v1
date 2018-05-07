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
package io.nuls.protocol.message.base;

import io.nuls.kernel.model.BaseNulsData;
import io.protostuff.Tag;

/**
 * 网络消息头，消息头中包含魔法参数、消息体大小、校验位、加密算法标识、模块id、消息类型信息
 * Network message header, the message header contains
 * magic parameters, message body size, parity, encryption algorithm id, module id, message type information.
 * @author Niels
 * @date 2017/11/7
 */
public class MessageHeader extends BaseNulsData {

    @Tag(1)
    private int magicNumber;

    @Tag(2)
    // the messageBody length
    private int length;

    @Tag(3)
    private byte xor;

    @Tag(4)
    private byte arithmetic;

    @Tag(5)
    private short moduleId;

    @Tag(6)
    private short msgType;

    public MessageHeader() {
    }

    public MessageHeader(short moduleId, short msgType) {
        this.moduleId = moduleId;
        this.msgType = msgType;
    }

    public short getMsgType() {
        return msgType;
    }

    public void setMsgType(short msgType) {
        this.msgType = msgType;
    }

    public short getModuleId() {
        return moduleId;
    }

    public void setModuleId(short moduleId) {
        this.moduleId = moduleId;
    }

    public int getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(int magicNumber) {
        this.magicNumber = magicNumber;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte getXor() {
        return xor;
    }

    public void setXor(byte xor) {
        this.xor = xor;
    }

    public byte getArithmetic() {
        return arithmetic;
    }

    public void setArithmetic(byte arithmetic) {
        this.arithmetic = arithmetic;
    }
}

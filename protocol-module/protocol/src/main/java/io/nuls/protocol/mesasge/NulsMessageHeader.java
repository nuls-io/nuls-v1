/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.protocol.mesasge;

import io.nuls.core.tools.crypto.Hex;
import io.nuls.protocol.model.BaseNulsData;

/**
 * @author vivi
 * @date 2017-11-10
 */
public class NulsMessageHeader extends BaseNulsData {

    public static final transient int MESSAGE_HEADER_SIZE = 10;

    private int magicNumber;

    // the messageBody length
    private int length;

    private byte xor;

    private byte arithmetic;

    public NulsMessageHeader() {
        this.magicNumber = 0;
        this.length = 0;
        this.xor = Hex.decode("00")[0];
        this.arithmetic = 0;
    }

    public NulsMessageHeader(int magicNumber) {
        this();
        this.magicNumber = magicNumber;
    }

    public NulsMessageHeader(int magicNumber, int length) {
        this(magicNumber);
        this.length = length;
    }

    public NulsMessageHeader(int magicNumber, int length, byte xor) {
        this(magicNumber, length);
        this.xor = xor;
    }

    public NulsMessageHeader(int magicNumber, int length, byte xor, byte arithmetic) {
        this(magicNumber, length, xor);
        this.arithmetic = arithmetic;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("messageHeader:{");
        buffer.append("magicNumber:" + magicNumber + ", ");
        buffer.append("length:" + length + ", ");
        buffer.append("xor:" + xor + ", ");
        buffer.append("arithmetic:" + arithmetic + "}");

        return buffer.toString();
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(int magicNumber) {
        this.magicNumber = magicNumber;
    }

    public byte getXor() {
        return xor;
    }

    public void setXor(byte xor) {
        this.xor = xor;
    }

    public void setArithmetic(byte arithmetic) {
        this.arithmetic = arithmetic;
    }

    public byte getArithmetic() {
        return arithmetic;
    }


}

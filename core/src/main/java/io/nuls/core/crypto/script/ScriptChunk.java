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
package io.nuls.core.crypto.script;

import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.crypto.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

import static io.nuls.core.crypto.script.ScriptOpCodes.*;


/**
 * A script element that is either a data push (signature, pubkey, etc) or a non-push (logic, numeric, etc) operation.
 */
public class ScriptChunk {
    /** Operation to be executed. Opcodes are defined in {@link ScriptOpCodes}. */
    public final int opcode;
    /**
     * For push operations, this is the vector to be pushed on the stack. For {@link ScriptOpCodes#OP_0}, the vector is
     * empty. Null for non-push operations.
     */
    public final byte[] data;
    private int startLocationInProgram;

    public ScriptChunk(int opcode, byte[] data) {
        this(opcode, data, -1);
    }

    public ScriptChunk(int opcode, byte[] data, int startLocationInProgram) {
        this.opcode = opcode;
        this.data = data;
        this.startLocationInProgram = startLocationInProgram;
    }

    public boolean equalsOpCode(int opcode) {
        return opcode == this.opcode;
    }

    /**
     * If this chunk is a single byte of non-pushdata content (could be OP_RESERVED or some invalid Opcode)
     */
    public boolean isOpCode() {
        return opcode > OP_PUSHDATA4;
    }

    /**
     * Returns true if this chunk is pushdata content, including the single-byte pushdatas.
     */
    public boolean isPushData() {
        return opcode <= OP_16;
    }

    public int getStartLocationInProgram() {
        Utils.checkState(startLocationInProgram >= 0);
        return startLocationInProgram;
    }

    /** If this chunk is an OP_N opcode returns the equivalent integer value. */
    public int decodeOpN() {
        Utils.checkState(isOpCode());
        return Script.decodeFromOpN(opcode);
    }

    /**
     * Called on a pushdata chunk, returns true if it uses the smallest possible way (according to BIP62) to push the data.
     */
    public boolean isShortestPossiblePushData() {
        Utils.checkState(isPushData());
        if (data == null) {
            return true;   // OP_N
        }
        if (data.length == 0) {
            return opcode == OP_0;
        }
        if (data.length == 1) {
            byte b = data[0];
            if (b >= 0x01 && b <= 0x10) {
                return opcode == OP_1 + b - 1;
            }
            if ((b & 0xFF) == 0x81) {
                return opcode == OP_1NEGATE;
            }
        }
        if (data.length < OP_PUSHDATA1) {
            return opcode == data.length;
        }
        if (data.length < 256) {
            return opcode == OP_PUSHDATA1;
        }
        if (data.length < 65536) {
            return opcode == OP_PUSHDATA2;
        }

        // can never be used, but implemented for completeness
        return opcode == OP_PUSHDATA4;
    }

    public void write(OutputStream stream) throws IOException {
        if (isOpCode()) {
            Utils.checkState(data == null);
            stream.write(opcode);
        } else if (data != null) {
            if (opcode < OP_PUSHDATA1) {
                Utils.checkState(data.length == opcode);
                stream.write(opcode);
            } else if (opcode == OP_PUSHDATA1) {
                Utils.checkState(data.length <= 0xFF);
                stream.write(OP_PUSHDATA1);
                stream.write(data.length);
            } else if (opcode == OP_PUSHDATA2) {
                Utils.checkState(data.length <= 0xFFFF);
                stream.write(OP_PUSHDATA2);
                stream.write(0xFF & data.length);
                stream.write(0xFF & (data.length >> 8));
            } else if (opcode == OP_PUSHDATA4) {
                Utils.checkState(data.length <= Script.MAX_SCRIPT_ELEMENT_SIZE);
                stream.write(OP_PUSHDATA4);
                Utils.uint32ToByteStreamLE(data.length, stream);
            } else {
                throw new RuntimeException("Unimplemented");
            }
            stream.write(data);
        } else {
            stream.write(opcode); // smallNum
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (isOpCode()) {
            buf.append(getOpCodeName(opcode));
        } else if (data != null) {
            // Data chunk
            buf.append(getPushDataName(opcode)).append("[").append(Hex.encode(data)).append("]");
        } else {
            // Small num
            buf.append(Script.decodeFromOpN(opcode));
        }
        return buf.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScriptChunk other = (ScriptChunk) o;
        return opcode == other.opcode && startLocationInProgram == other.startLocationInProgram
            && Arrays.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opcode, startLocationInProgram, Arrays.hashCode(data));
    }
}

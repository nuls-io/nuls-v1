/**
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
package io.nuls.core.chain.entity;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.io.NulsByteBuffer;


/**
 * @author vivi
 * @date 2017/11/23.
 */
public class NulsVersion {

    /**
     * The version number is 2 bytes
     */
    private short version;

    public static final short MAX_VERSION = 32767;

    public static final short MAX_MAIN_VERSION = 15;

    public static final short MAX_SUB_VERSION = 4095;

    public NulsVersion(short version) {
        setVersion(version);
    }

    public NulsVersion(short mainVersion, short subVersion) {
        setVersionBy(mainVersion, subVersion);
    }

    public void setVersion(short version) {
        if (version > MAX_VERSION) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
        this.version = version;
    }

    public short getVersion() {
        return version;
    }

    /**
     * version = mainVersion << 12 + subVersion
     *
     * @param main
     * @param sub
     */
    public void setVersionBy(short main, short sub) {
        if (main <= 0 || main > MAX_MAIN_VERSION) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
        if (main <= 0 || sub > MAX_SUB_VERSION) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
        version = (short) (main << 12 | sub);
    }

    public short getMainVersion() {
        return (short) ((version >> 12) & 0xF);
    }

    public short getSubVersion() {
        return (short) (version & 0xFFF);
    }

    public String getStringVersion() {
        return getMainVersion() + "." + getSubVersion();
    }

    public int size() {
        return 2;
    }
}

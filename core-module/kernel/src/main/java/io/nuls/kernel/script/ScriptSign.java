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
package io.nuls.kernel.script;

import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScriptSign extends BaseNulsData {

    private List<Script> scripts;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        if (scripts != null && scripts.size() > 0) {
            for (Script script : scripts) {
                stream.writeBytesWithLength(script.getProgram());
            }
        } else {
            stream.write(NulsConstant.PLACE_HOLDER);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        List<Script> scripts = new ArrayList<>();
        while (!byteBuffer.isFinished()) {
            scripts.add(new Script(byteBuffer.readByLengthByte()));
        }
        this.scripts = scripts;
    }

    public static ScriptSign createFromBytes(byte[] bytes) throws NulsException {
        ScriptSign sig = new ScriptSign();
        sig.parse(bytes, 0);
        return sig;
    }

    public List<Script> getScripts() {
        return scripts;
    }

    public void setScripts(List<Script> scripts) {
        this.scripts = scripts;
    }

    @Override
    public int size() {
        int size = 0;
        if (scripts != null && scripts.size() > 0) {
            for (Script script : scripts) {
                size += SerializeUtils.sizeOfBytes(script.getProgram());
            }
        } else {
            size = 4;
        }
        return size;
    }

}

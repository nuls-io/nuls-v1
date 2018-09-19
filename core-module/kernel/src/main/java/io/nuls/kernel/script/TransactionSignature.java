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

package io.nuls.kernel.script;

import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransactionSignature extends BaseNulsData {
    private List<P2PHKSignature> p2PHKSignatures;
    private List<Script> scripts;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        // 旧签名数据写入流中
        if (p2PHKSignatures != null && p2PHKSignatures.size() > 0) {
            for (P2PHKSignature p2PHKSignature : p2PHKSignatures) {
                if (p2PHKSignature != null)
                    stream.writeNulsData(p2PHKSignature);
            }
        }
        if (scripts != null && scripts.size() > 0) {
            //写入标识位，用于标识是否存在脚本未读
            stream.write(NulsConstant.SIGN_HOLDER);
            //写入脚本
            for (Script script : scripts) {
                if (script != null && script.getProgram() != null && script.getProgram().length > 0)
                    stream.writeBytesWithLength(script.getProgram());
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        // 从流中读取签名,兼容新老版本
        int course = 0;
        boolean isScript = false;
        List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
        List<Script> scripts = new ArrayList<>();
        while (!byteBuffer.isFinished()) {
            course = byteBuffer.getCursor();
            //读取两个字节（脚本标识位），如果两个字节都为0x00则表示后面的数据流为脚本数据
            if (!isScript && byteBuffer.getPayload().length < 2)
                break;
            if (isScript || Arrays.equals(NulsConstant.SIGN_HOLDER, byteBuffer.readBytes(2))) {
                isScript = true;
                if (!byteBuffer.isFinished()) {
                    scripts.add(new Script(byteBuffer.readByLengthByte()));
                }
            } else {
                byteBuffer.setCursor(course);
                p2PHKSignatures.add(byteBuffer.readNulsData(new P2PHKSignature()));
            }
        }
        this.p2PHKSignatures = p2PHKSignatures;
        this.scripts = scripts;
    }

    @Override
    public int size() {
        // 当前签名数据长度
        int size = 0;
        if (p2PHKSignatures != null && p2PHKSignatures.size() > 0) {
            for (P2PHKSignature p2PHKSignature : p2PHKSignatures) {
                if (p2PHKSignature != null)
                    size += SerializeUtils.sizeOfNulsData(p2PHKSignature);
            }
        }
        if (scripts != null && scripts.size() > 0) {
            //写入标识位，用于标识是否存在脚本未读
            size += NulsConstant.SIGN_HOLDER.length;
            //写入脚本
            for (Script script : scripts) {
                if (script != null && script.getProgram() != null && script.getProgram().length > 0)
                    size += SerializeUtils.sizeOfBytes(script.getProgram());
            }
        }
        return size;
    }

    public List<P2PHKSignature> getP2PHKSignatures() {
        return p2PHKSignatures;
    }

    public void setP2PHKSignatures(List<P2PHKSignature> p2PHKSignatures) {
        this.p2PHKSignatures = p2PHKSignatures;
    }

    public List<Script> getScripts() {
        return scripts;
    }

    public void setScripts(List<Script> scripts) {
        this.scripts = scripts;
    }

    public int getSignersCount() {
        int count = 0;

        return count;
    }

}

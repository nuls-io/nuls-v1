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
package io.nuls.core.script;

import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.validate.ValidateResult;
import org.bouncycastle.jce.provider.JDKKeyFactory;

import java.io.IOException;

/**
 * author Facjas
 * date 2018/3/8.
 */
public class P2PKHScriptSig extends Script {

    private NulsSignData signData;
    private byte[] publicKey;

    public P2PKHScriptSig(){

    }

    @Override
    public int size() {
        return signData.size()+ Utils.sizeOfBytes(publicKey);
    }

    public P2PKHScriptSig(byte[] signBytes, byte[] publicKey){
        this.signData = new NulsSignData(signBytes);
        this.publicKey = publicKey;
    }

    public NulsSignData getSignData() {
        return signData;
    }

    public void setSignData(NulsSignData signData) {
        this.signData = signData;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public P2PKHScriptSig(NulsSignData signData, byte[] publicKey){
        this.signData = signData;
        this.publicKey = publicKey;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        signData.serializeToStream(stream);
        stream.writeBytesWithLength(publicKey);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer)throws NulsException {
        signData = byteBuffer.readNulsData(new NulsSignData());
        publicKey = byteBuffer.readByLengthByte();
    }

    @Override
    public byte[] getBytes() {

        //todo
        return new byte[0];
    }

    public ValidateResult verifySign(NulsDigestData digestData){
        boolean b = ECKey.verify(digestData.getDigestBytes(),signData.getSignBytes(),this.getPublicKey());
        if(b){
            return ValidateResult.getSuccessResult();
        }else {
            return ValidateResult.getFailedResult(ErrorCode.SIGNATURE_ERROR);
        }
    }

    public byte[] getSignerHash160(){
        return Utils.sha256hash160(getPublicKey());
    }

    public static P2PKHScriptSig createFromBytes(byte[] bytes) throws NulsException {
        return new NulsByteBuffer(bytes).readNulsData(new P2PKHScriptSig());
    }
}

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
package io.nuls.ledger.script;

import io.nuls.core.chain.entity.NulsDigestData;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * author Facjas
 * date 2018/3/8.
 */
public class TransferScript {

    private P2PKHScriptSig scriptSig;
    private P2PKHScript script;

    public TransferScript(){

    }

    public TransferScript(P2PKHScriptSig scriptSig, P2PKHScript script){
        this.script = script;
        this.scriptSig = scriptSig;
    }

    public P2PKHScriptSig getScript() {
        return scriptSig;
    }

    public P2PKHScript getScriptSig() {
        return script;
    }

    public void setScriptSig(P2PKHScriptSig scriptSig) {
        this.scriptSig = scriptSig;
    }

    public void setScript(P2PKHScript script) {
        this.script = script;
    }

    public boolean verifyScript(){
        return  verifyPublicKey() && verifySign();
    }

    boolean verifyPublicKey(){
        //verify the public-KEY-hashes  are the same
        byte[] publicKey = scriptSig.getPublicKey();
        NulsDigestData digestData = NulsDigestData.calcDigestData(publicKey,NulsDigestData.DIGEST_ALG_SHA160);
        if(Arrays.equals(digestData.getDigestBytes(),script.getPublicKeyDigest().getDigestBytes())){
            return true;
        }
        return false;
    }

    boolean verifySign(){
        //TODO verify the signature
        return true;
    }

}

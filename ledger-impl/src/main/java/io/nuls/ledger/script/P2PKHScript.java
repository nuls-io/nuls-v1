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
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * author Facjas
 * date 2018/3/8.
 */
public class P2PKHScript extends Script {

    private NulsDigestData publicKeyDigest;

    public NulsDigestData getPublicKeyDigest() {
        return publicKeyDigest;
    }

    public void setPublicKeyDigest(NulsDigestData publicKeyDigest) {
        this.publicKeyDigest = publicKeyDigest;
    }

    public P2PKHScript(byte[] bytes)throws NulsException{
        NulsDigestData nulsDigestData = new NulsDigestData();
        nulsDigestData.parse(bytes);
        this.publicKeyDigest = nulsDigestData;
    }

    public P2PKHScript(NulsDigestData publicKeyDigest){
        this.publicKeyDigest = publicKeyDigest;
    }

    @Override
    public int size() {
        return publicKeyDigest.size();
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(publicKeyDigest);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        publicKeyDigest = byteBuffer.readNulsData(new NulsDigestData());
    }

    @Override
    public byte[] getBytes(){
        try {
            return publicKeyDigest.serialize();
        } catch (IOException e) {
            return  null;
        }
    }

    //todo  not finished
}

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
package io.nuls.core.crypto;

import java.math.BigInteger;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECPoint;

/**
 * Created by facjas on 2017/11/20.
 */
public class Cipher {
    private int ct;
    private ECPoint p2;
    private SM3Digest sm3keybase;
    private SM3Digest sm3c3;
    private byte key[];
    private byte keyOff;

    public Cipher() {
        this.ct = 1;
        this.key = new byte[32];
        this.keyOff = 0;
    }

    private void reset() {
        this.sm3keybase = new SM3Digest();
        this.sm3c3 = new SM3Digest();

        byte p[] = Util.byteConvert32Bytes(p2.getX().toBigInteger());
        this.sm3keybase.update(p, 0, p.length);
        this.sm3c3.update(p, 0, p.length);

        p = Util.byteConvert32Bytes(p2.getY().toBigInteger());
        this.sm3keybase.update(p, 0, p.length);
        this.ct = 1;
        nextkey();
    }

    private void nextkey() {
        SM3Digest sm3keycur = new SM3Digest(this.sm3keybase);
        sm3keycur.update((byte) (ct >> 24 & 0xff));
        sm3keycur.update((byte) (ct >> 16 & 0xff));
        sm3keycur.update((byte) (ct >> 8 & 0xff));
        sm3keycur.update((byte) (ct & 0xff));
        sm3keycur.doFinal(key, 0);
        this.keyOff = 0;
        this.ct++;
    }

    public ECPoint initEnc(SM2 sm2, ECPoint userKey) {
        AsymmetricCipherKeyPair key = sm2.ecc_key_pair_generator.generateKeyPair();
        ECPrivateKeyParameters ecpriv = (ECPrivateKeyParameters) key.getPrivate();
        ECPublicKeyParameters ecpub = (ECPublicKeyParameters) key.getPublic();
        BigInteger k = ecpriv.getD();
        ECPoint c1 = ecpub.getQ();
        this.p2 = userKey.multiply(k);
        reset();
        return c1;
    }

    public void encrypt(byte data[]) {
        this.sm3c3.update(data, 0, data.length);
        for (int i = 0; i < data.length; i++) {
            if (keyOff == key.length) {
                nextkey();
            }
            data[i] ^= key[keyOff++];
        }
    }

    public void initDec(BigInteger userD, ECPoint c1) {
        this.p2 = c1.multiply(userD);
        reset();
    }

    public void decrypt(byte data[]) {
        for (int i = 0; i < data.length; i++) {
            if (keyOff == key.length) {
                nextkey();
            }
            data[i] ^= key[keyOff++];
        }
        this.sm3c3.update(data, 0, data.length);
    }

    public void dofinal(byte c3[]) {
        byte p[] = Util.byteConvert32Bytes(p2.getY().toBigInteger());
        this.sm3c3.update(p, 0, p.length);
        this.sm3c3.doFinal(c3, 0);
        reset();
    }
}

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Enumeration;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Base64;

/**
 * Created by facjas on 2017/11/20.
 */
public class SM2Utils {
    public static byte[] encrypt(byte[] publicKey, byte[] data) throws IOException {
        if (publicKey == null || publicKey.length == 0) {
            return null;
        }

        if (data == null || data.length == 0) {
            return null;
        }

        byte[] source = new byte[data.length];
        System.arraycopy(data, 0, source, 0, data.length);

        Cipher cipher = new Cipher();
        SM2 sm2 = SM2.Instance();
        ECPoint userKey = sm2.ecc_curve.decodePoint(publicKey);

        ECPoint c1 = cipher.initEnc(sm2, userKey);
        cipher.encrypt(source);
        byte[] c3 = new byte[32];
        cipher.dofinal(c3);

        DERInteger x = new DERInteger(c1.getX().toBigInteger());
        DERInteger y = new DERInteger(c1.getY().toBigInteger());
        DEROctetString derDig = new DEROctetString(c3);
        DEROctetString derEnc = new DEROctetString(source);
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(x);
        v.add(y);
        v.add(derDig);
        v.add(derEnc);
        DERSequence seq = new DERSequence(v);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DEROutputStream dos = new DEROutputStream(bos);
        dos.writeObject(seq);
        return bos.toByteArray();
    }

    public static byte[] decrypt(byte[] privateKey, byte[] encryptedData) throws IOException {
        if (privateKey == null || privateKey.length == 0) {
            return null;
        }

        if (encryptedData == null || encryptedData.length == 0) {
            return null;
        }

        byte[] enc = new byte[encryptedData.length];
        System.arraycopy(encryptedData, 0, enc, 0, encryptedData.length);

        SM2 sm2 = SM2.Instance();
        BigInteger userD = new BigInteger(1, privateKey);

        ByteArrayInputStream bis = new ByteArrayInputStream(enc);
        ASN1InputStream dis = new ASN1InputStream(bis);
        DERObject derObj = dis.readObject();
        ASN1Sequence asn1 = (ASN1Sequence) derObj;
        DERInteger x = (DERInteger) asn1.getObjectAt(0);
        DERInteger y = (DERInteger) asn1.getObjectAt(1);
        ECPoint c1 = sm2.ecc_curve.createPoint(x.getValue(), y.getValue(), true);

        Cipher cipher = new Cipher();
        cipher.initDec(userD, c1);
        DEROctetString data = (DEROctetString) asn1.getObjectAt(3);
        enc = data.getOctets();
        cipher.decrypt(enc);
        byte[] c3 = new byte[32];
        cipher.dofinal(c3);
        return enc;
    }

    public static byte[] sign(byte[] userId, byte[] privateKey, byte[] sourceData) throws IOException {
        if (privateKey == null || privateKey.length == 0) {
            return null;
        }

        if (sourceData == null || sourceData.length == 0) {
            return null;
        }

        SM2 sm2 = SM2.Instance();
        BigInteger userD = new BigInteger(privateKey);


        ECPoint userKey = sm2.ecc_point_g.multiply(userD);

        SM3Digest sm3 = new SM3Digest();
        byte[] z = sm2.sm2GetZ(userId, userKey);

        sm3.update(z, 0, z.length);
        sm3.update(sourceData, 0, sourceData.length);
        byte[] md = new byte[32];
        sm3.doFinal(md, 0);


        SM2Result sm2Result = new SM2Result();
        sm2.sm2Sign(md, userD, userKey, sm2Result);
        System.out.println("r: " + sm2Result.r.toString(16));
        System.out.println("s: " + sm2Result.s.toString(16));
        System.out.println("");

        DERInteger dR = new DERInteger(sm2Result.r);
        DERInteger dS = new DERInteger(sm2Result.s);
        ASN1EncodableVector v2 = new ASN1EncodableVector();
        v2.add(dR);
        v2.add(dS);
        DERObject sign = new DERSequence(v2);
        byte[] signdata = sign.getDEREncoded();
        return signdata;
    }

    public static boolean verifySign(byte[] userId, byte[] publicKey, byte[] sourceData, byte[] signData) throws IOException {
        if (publicKey == null || publicKey.length == 0) {
            return false;
        }

        if (sourceData == null || sourceData.length == 0) {
            return false;
        }

        SM2 sm2 = SM2.Instance();
        ECPoint userKey = sm2.ecc_curve.decodePoint(publicKey);

        SM3Digest sm3 = new SM3Digest();
        byte[] z = sm2.sm2GetZ(userId, userKey);
        sm3.update(z, 0, z.length);
        sm3.update(sourceData, 0, sourceData.length);
        byte[] md = new byte[32];
        sm3.doFinal(md, 0);
        System.out.println("SM3" + Util.getHexString(md));
        System.out.println("");

        ByteArrayInputStream bis = new ByteArrayInputStream(signData);
        ASN1InputStream dis = new ASN1InputStream(bis);
        DERObject derObj = dis.readObject();
        Enumeration<DERInteger> e = ((ASN1Sequence) derObj).getObjects();
        BigInteger r = ((DERInteger) e.nextElement()).getValue();
        BigInteger s = ((DERInteger) e.nextElement()).getValue();
        SM2Result sm2Result = new SM2Result();
        sm2Result.r = r;
        sm2Result.s = s;

        /*
        * debug
         */
        System.out.println("r: " + sm2Result.r.toString(16));
        System.out.println("s: " + sm2Result.s.toString(16));
        System.out.println("");


        sm2.sm2Verify(md, userKey, sm2Result.r, sm2Result.s, sm2Result);
        return sm2Result.r.equals(sm2Result.R);
    }

    public static void main(String[] args) throws Exception {
        String plainText = "message digest";
        byte[] sourceData = plainText.getBytes();


        String prik = "128B2FA8BD433C6C068C8D803DFF79792A519A55171B1B650C23661D15897263";
        String prikS = new String(Base64.encode(Util.hexToByte(prik)));
        System.out.println("prikS: " + prikS);
        System.out.println("");


        String userId = "NULS";

        System.out.println("ID: " + Util.getHexString(userId.getBytes()));
        System.out.println("");


        byte[] c = SM2Utils.sign(userId.getBytes(), Base64.decode(prikS.getBytes()), sourceData);
        System.out.println("sign: " + Util.getHexString(c));
        System.out.println("");


        String pubk = "040AE4C7798AA0F119471BEE11825BE46202BB79E2A5844495E97C04FF4DF2548A7C0240F88F1CD4E16352A73C17B7F16F07353E53A176D684A9FE0C6BB798E857";
        String pubkS = new String(Base64.encode(Util.hexToByte(pubk)));
        System.out.println("pubkS: " + pubkS);
        System.out.println("");


        boolean vs = SM2Utils.verifySign(userId.getBytes(), Base64.decode(pubkS.getBytes()), sourceData, c);
        System.out.println("vs: " + vs);
        System.out.println("");


        byte[] cipherText = SM2Utils.encrypt(Base64.decode(pubkS.getBytes()), sourceData);
        System.out.println(new String(Base64.encode(cipherText)));
        System.out.println("");

        plainText = new String(SM2Utils.decrypt(Base64.decode(prikS.getBytes()), cipherText));
        System.out.println(plainText);
    }
}

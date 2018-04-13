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
package io.nuls.core.crypto;

import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.log.Log;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * AES加密
 * @author ln
 */
public class AESEncrypt {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static byte[] encrypt(byte[] plainBytes, String password) {
        EncryptedData ed = encrypt(plainBytes, new KeyParameter(Sha256Hash.hash(password.getBytes())));
        return ed.getEncryptedBytes();

    }

    /**
     * 加密
     *
     * @param plainBytes
     * @param aesKey
     * @return EncryptedData
     */
    public static EncryptedData encrypt(byte[] plainBytes, KeyParameter aesKey) {
        return encrypt(plainBytes, null, aesKey);
    }

    /**
     * 加密
     *
     * @param plainBytes
     * @param iv
     * @param aesKey
     * @return EncryptedData
     */
    public static EncryptedData encrypt(byte[] plainBytes, byte[] iv, KeyParameter aesKey) throws NulsRuntimeException {
        Utils.checkNotNull(plainBytes);
        Utils.checkNotNull(aesKey);

        try {
            if (iv == null) {
                iv = EncryptedData.DEFAULT_IV;
                //SECURE_RANDOM.nextBytes(iv);
            }

            ParametersWithIV keyWithIv = new ParametersWithIV(aesKey, iv);

            // Encrypt using AES.
            BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
            cipher.init(true, keyWithIv);
            byte[] encryptedBytes = new byte[cipher.getOutputSize(plainBytes.length)];
            final int length1 = cipher.processBytes(plainBytes, 0, plainBytes.length, encryptedBytes, 0);
            final int length2 = cipher.doFinal(encryptedBytes, length1);

            return new EncryptedData(iv, Arrays.copyOf(encryptedBytes, length1 + length2));
        } catch (Exception e) {
            throw new NulsRuntimeException(e);
        }
    }

    public static byte[] decrypt(byte[] dataToDecrypt,String password){
        byte [] defaultiv = new byte[16];
        EncryptedData data = new EncryptedData(defaultiv,dataToDecrypt);
        return decrypt(data,new KeyParameter(Sha256Hash.hash(password.getBytes())));
    }

    public static byte[] decrypt(byte[] dataToDecrypt,String password,String charset) throws UnsupportedEncodingException {
        byte [] defaultiv = new byte[16];
        EncryptedData data = new EncryptedData(defaultiv,dataToDecrypt);
        return decrypt(data,new KeyParameter(Sha256Hash.hash(password.getBytes(charset))));
    }

    /**
     * 解密
     *
     * @param dataToDecrypt
     * @param aesKey
     * @return byte[]
     * @throws NulsRuntimeException
     */
    public static byte[] decrypt(EncryptedData dataToDecrypt, KeyParameter aesKey) throws NulsRuntimeException {
        Utils.checkNotNull(dataToDecrypt);
        Utils.checkNotNull(aesKey);

        try {
            ParametersWithIV keyWithIv = new ParametersWithIV(new KeyParameter(aesKey.getKey()), dataToDecrypt.getInitialisationVector());

            // Decrypt the message.
            BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
            cipher.init(false, keyWithIv);

            byte[] cipherBytes = dataToDecrypt.getEncryptedBytes();
            byte[] decryptedBytes = new byte[cipher.getOutputSize(cipherBytes.length)];
            final int length1 = cipher.processBytes(cipherBytes, 0, cipherBytes.length, decryptedBytes, 0);
            final int length2 = cipher.doFinal(decryptedBytes, length1);

            return Arrays.copyOf(decryptedBytes, length1 + length2);
        } catch (Exception e) {
            throw new NulsRuntimeException(e);
        }
    }

}

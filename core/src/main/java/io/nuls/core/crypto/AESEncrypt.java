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

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * AES加密
 *
 * @author ln
 */
public class AESEncrypt {

    private static final SecureRandom secureRandom = new SecureRandom();

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
                iv = new byte[16];
                secureRandom.nextBytes(iv);
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

    public static void main(String[] args) {
        String str = "test 加密测试";

        String pw = "sssssfds";

        EncryptedData data = encrypt(str.getBytes(), new KeyParameter(Sha256Hash.hash(pw.getBytes())));
        Log.debug(data+"");

        Log.debug(new String(decrypt(data, new KeyParameter(Sha256Hash.hash(pw.getBytes())))));

    }
}

package io.nuls.consensus.poc.util;

import io.nuls.consensus.poc.storage.po.RandomSeedStatusPo;
import io.nuls.core.tools.crypto.Sha256Hash;

import java.math.BigInteger;
import java.util.Random;

/**
 * @author Niels
 */
public class RandomSeedUtils {

    public static RandomSeedStatusPo CACHE_SEED = new RandomSeedStatusPo();

    public static byte[] createRandomSeed() {
        BigInteger value = new BigInteger(256, new Random());
        byte[] result = value.toByteArray();
        if (result.length > 32) {
            byte[] temp = new byte[32];
            System.arraycopy(result, result.length - 32, temp, 0, 32);
            result = temp;
        } else if (result.length < 32) {
            byte[] temp = new byte[32];
            System.arraycopy(result, 0, temp, 0, result.length);
            result = temp;
        }
        return result;
    }

    public static byte[] getLastDigestEightBytes(byte[] bytes) {
        byte[] hash = Sha256Hash.hashTwice(bytes);
        byte[] result = new byte[8];
        System.arraycopy(hash, bytes.length - 8, result, 0, 8);
        return result;
    }
}

package io.nuls.core.tools.crypto;

import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.str.StringUtils;

import java.util.List;

/**
 * @author Niels
 */
public class RandomSeedCaculator {

    public static byte[] clac(List<byte[]> seeds, String algorithm) {
        if (StringUtils.isBlank(algorithm)) {
            algorithm = "sha3";
        }
        if ("SHA3".equals(algorithm.toUpperCase())) {
            byte[] bytes = ArraysTool.concatenate(seeds.toArray(new byte[seeds.size()][]));
            return Sha3Hash.sha3bytes(bytes, 256);
        } else if ("KECCAK".equals(algorithm.toUpperCase())) {
            byte[] bytes = ArraysTool.concatenate(seeds.toArray(new byte[seeds.size()][]));
            return KeccakHash.keccakBytes(bytes, 256);
        }
        return null;
    }
}

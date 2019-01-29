package io.nuls.kernel.utils;

import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.crypto.KeccakHash;
import io.nuls.core.tools.crypto.Sha256Hash;
import io.nuls.core.tools.crypto.Sha3Hash;
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
        } else if ("MERKLE".equals(algorithm.toUpperCase())) {
            return calcMerkleDigestData(seeds);
        }
        return null;
    }


    public static byte[] calcMerkleDigestData(List<byte[]> seeds) {
        int levelOffset = 0;
        for (int levelSize = seeds.size(); levelSize > 1; levelSize = (levelSize + 1) / 2) {
            for (int left = 0; left < levelSize; left += 2) {
                int right = Math.min(left + 1, levelSize - 1);
                byte[] leftBytes = SerializeUtils.reverseBytes(seeds.get(levelOffset + left));
                byte[] rightBytes = SerializeUtils.reverseBytes(seeds.get(levelOffset + right));
                byte[] whole = new byte[leftBytes.length + rightBytes.length];
                System.arraycopy(leftBytes, 0, whole, 0, leftBytes.length);
                System.arraycopy(rightBytes, 0, whole, leftBytes.length, rightBytes.length);
                byte[] digest = Sha256Hash.hashTwice(whole);
                seeds.add(digest);
            }
            levelOffset += levelSize;
        }
        byte[] bytes = seeds.get(seeds.size() - 1);
        Sha256Hash merkleHash = Sha256Hash.wrap(bytes);
        return merkleHash.getBytes();
    }
}

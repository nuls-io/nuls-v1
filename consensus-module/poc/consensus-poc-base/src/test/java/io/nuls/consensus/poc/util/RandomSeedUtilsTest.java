package io.nuls.consensus.poc.util;

import io.nuls.core.tools.array.ArraysTool;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Niels
 */
public class RandomSeedUtilsTest {

    @Test
    public void createRandomSeed() {
        for (int i = 0; i < 1000000; i++) {
            byte[] result = RandomSeedUtils.createRandomSeed();
            assertEquals(result.length, 32);
        }
    }

    @Test
    public void getLastDigestEightBytes() {
        byte[] seed = RandomSeedUtils.createRandomSeed();
        byte[] hash = RandomSeedUtils.getLastDigestEightBytes(seed);
        byte[] hash2 = RandomSeedUtils.getLastDigestEightBytes(seed);
        assertTrue(ArraysTool.arrayEquals(hash, hash2));
        assertEquals(hash.length, 8);
    }
}
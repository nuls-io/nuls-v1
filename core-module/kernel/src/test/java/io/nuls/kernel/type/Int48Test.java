package io.nuls.kernel.type;

import io.nuls.kernel.utils.SerializeUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Niels
 * @date 2018/5/17
 */
public class Int48Test {
    @Test
    public void test() {
        long time = -1L;
        byte[] bytes = SerializeUtils.int48ToBytes(time);
        long value = readInt48(bytes);
//        assertEquals(value, time);
    }

    @Test
    public void testLong() {
        byte[] bytes = new byte[]{-1,-1,-1,-1,-1,-1,0,0};
        long value = SerializeUtils.readInt64LE(bytes,0);
        System.out.println(value);
    }

    private long readInt48(byte[] bytes) {
        long value = (bytes[0] & 0xffL) |
                ((bytes[1] & 0xffL) << 8) |
                ((bytes[2] & 0xffL) << 16) |
                ((bytes[3] & 0xffL) << 24) |
                ((bytes[4] & 0xffL) << 32) |
                ((bytes[5] & 0xffL) << 40) ;
        return value;
    }
}

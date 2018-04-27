package io.nuls.core.utils.crypto;

import io.nuls.core.exception.NulsException;
import org.junit.Test;

/**
 * @author: Niels Wang
 * @date: 2018/4/27
 */
public class Base58Test {

    @Test
    public void test() {
        try {
            byte[] bytes = Base58.decode("ns");
            System.out.println(Utils.bytes2Short(bytes));
        } catch (NulsException e) {
            e.printStackTrace();
        }
    }

}
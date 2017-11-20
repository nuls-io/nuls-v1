package io.nuls.test;

import io.nuls.core.crypto.ECKey;
import io.nuls.core.utils.crypto.Base58;
import io.nuls.core.utils.crypto.Utils;

/**
 * Created by Niels on 2017/11/20.
 */
public class MainTest {
    public static void main(String[] args) {
        int i = 0;
        while (i++<10){
            ECKey ecKey = new ECKey();
            System.out.println(ecKey.getPrivKeyBytes().length);
        }
    }
}

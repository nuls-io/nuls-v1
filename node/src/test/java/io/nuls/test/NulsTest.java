package io.nuls.test;

import io.nuls.account.entity.Address;
import io.nuls.account.entity.Alias;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.utils.crypto.Utils;
import org.junit.Test;

/**
 * @author vivi
 * @date 2017/12/23.
 */
public class NulsTest {

    @Test
    public void testAlias() {
        ECKey key = new ECKey();
        Address address = new Address(NulsContext.getInstance().getChainId(NulsContext.CHAIN_ID), Utils.sha256hash160(key.getPubKey(false)));

        Alias alias = new Alias(address.getBase58(), "vivi");


        System.out.println(address.getBase58());
        System.out.println(address.getHash().length);
        System.out.println(address.getHash160().length);

    }
}

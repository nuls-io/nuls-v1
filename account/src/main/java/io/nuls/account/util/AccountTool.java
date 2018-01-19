/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.account.util;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.entity.Alias;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.db.entity.AccountPo;
import io.nuls.db.entity.AliasPo;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @author ln
 */
public final class AccountTool {

    /**
     * create a new address
     *
     * @return Address
     */

    public static final int CREATE_MAX_SIZE = 100;

    public static Address newAddress(ECKey key) throws NulsException {
        return Address.fromHashs(Utils.sha256hash160(key.getPubKey(false)));
    }


    public static Account createAccount() {
        ECKey key = new ECKey();
        Address address = new Address(NulsContext.getInstance().getChainId(NulsContext.CHAIN_ID), Utils.sha256hash160(key.getPubKey(false)));
        Account account = new Account();
        account.setPriSeed(key.getPrivKeyBytes());
        account.setVersion(new NulsVersion((short) 0));
        account.setAddress(address);
        account.setId(address.toString());
        account.setPubKey(key.getPubKey(true));
        account.setEcKey(key);
        account.setPriKey(key.getPrivKeyBytes());
        account.setCreateTime(TimeService.currentTimeMillis());
        return account;
    }

    /**
     * Generate the corresponding account management private key or transaction private key according to the seed private key and password
     */
    public static BigInteger genPrivKey(byte[] priSeed, byte[] pw) {
        byte[] privSeedSha256 = Sha256Hash.hash(priSeed);
        //get sha256 of priSeed and  sha256 of pw，
        byte[] pwSha256 = Sha256Hash.hash(pw);
        //privSeedSha256 + pwPwSha256
        byte[] pwPriBytes = new byte[privSeedSha256.length + pwSha256.length];
        for (int i = 0; i < pwPriBytes.length; i += 2) {
            int index = i / 2;
            pwPriBytes[index] = privSeedSha256[index];
            pwPriBytes[index + 1] = pwSha256[index];
        }
        //get prikey
        return new BigInteger(1, Sha256Hash.hash(pwPriBytes));
    }

    public static void toBean(AccountPo src, Account desc) {
        AssertUtil.canNotEmpty(src, "Object type conversion failed!");
        AssertUtil.canNotEmpty(desc, "Object type conversion failed!");
        desc.setCreateTime(src.getCreateTime());
        desc.setVersion(new NulsVersion(src.getVersion()));
        try {
            desc.setAddress(newAddress(src.getPubKey()));
        } catch (NulsException e) {
            Log.error(e);
        }
        desc.setAlias(src.getAlias());
        desc.setExtend(src.getExtend());
        desc.setId(src.getId());
        desc.setPubKey(src.getPubKey());
        desc.setPriKey(src.getPriKey().getBytes());
        desc.setPriSeed(src.getPriSeed());
        desc.setEcKey(ECKey.fromPrivate(new BigInteger(desc.getPriKey())));
        desc.setStatus(src.getStatus());
    }

    private static Address newAddress(byte[] pubKey) throws NulsException {
        return Address.fromHashs(Utils.sha256hash160(pubKey));
    }

    public static void toPojo(Account src, AccountPo desc) {
        AssertUtil.canNotEmpty(src, "Object type conversion failed!");
        AssertUtil.canNotEmpty(desc, "Object type conversion failed!");
        desc.setId(src.getId());
        desc.setAddress(src.getAddress().toString());
        desc.setAlias(src.getAlias());
        desc.setCreateTime(src.getCreateTime());
        desc.setPubKey(src.getPubKey());
        desc.setVersion(src.getVersion().getVersion());
        desc.setExtend(src.getExtend());
        desc.setPriKey(Hex.encode(src.getPriKey()));
        desc.setPriSeed(src.getPriSeed());
        desc.setStatus(src.getStatus());
    }

    public static AliasPo toAliasPojo(Alias alias) {
        AliasPo po = new AliasPo();
        po.setAddress(alias.getAddress());
        po.setAlias(alias.getAlias());
        po.setStatus(alias.getStatus());
        return po;
    }

    public static Alias toAliasBean(AliasPo po) {
        Alias alias = new Alias(po.getAddress(), po.getAlias());
        alias.setStatus(po.getStatus());
        return alias;
    }

}

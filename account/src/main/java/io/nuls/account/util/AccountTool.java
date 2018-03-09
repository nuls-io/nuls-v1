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
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.EncryptedData;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.entity.AccountPo;
import io.nuls.db.entity.AliasPo;

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

    public static Account createAccount(String prikey) throws NulsException {
        ECKey key = null;
        if (StringUtils.isBlank(prikey)) {
            key = new ECKey();
        } else {
            try {
                key = ECKey.fromPrivate(new BigInteger(Hex.decode(prikey)));
            } catch (Exception e) {
                throw new NulsException(ErrorCode.DATA_PARSE_ERROR);
            }
        }
        Address address = new Address(NulsContext.getInstance().getChainId(NulsContext.CHAIN_ID), Utils.sha256hash160(key.getPubKey(false)));
        Account account = new Account();
        account.setEncryptedPriKey(new byte[0]);
        account.setAddress(address);
        account.setPubKey(key.getPubKey(true));
        account.setEcKey(key);
        account.setPriKey(key.getPrivKeyBytes());
        account.setCreateTime(TimeService.currentTimeMillis());
        return account;
    }

    public static Account createAccount() throws NulsException {
        return createAccount(null);
    }

    /**
     * Generate the corresponding account management private key or transaction private key according to the seed private key and password
     */
    public static BigInteger genPrivKey(byte[] encryptedPriKey, byte[] pw) {
        byte[] privSeedSha256 = Sha256Hash.hash(encryptedPriKey);
        //get sha256 of encryptedPriKey and  sha256 of pw，
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
        try {
            desc.setAddress(Address.fromHashs(src.getAddress()));
        } catch (NulsException e) {
            Log.error(e);
        }
        desc.setAlias(src.getAlias());
        desc.setExtend(src.getExtend());
        desc.setPriKey(src.getPriKey());
        desc.setPubKey(src.getPubKey());
        desc.setEncryptedPriKey(src.getEncryptedPriKey());
        if (src.getPriKey() != null && src.getPriKey().length > 1) {
            desc.setEcKey(ECKey.fromPrivate(new BigInteger(desc.getPriKey())));
        } else {
            desc.setEcKey(ECKey.fromEncrypted(new EncryptedData(src.getEncryptedPriKey()), src.getPubKey()));
        }

        desc.setStatus(src.getStatus());
    }

    public static void toPojo(Account src, AccountPo desc) {
        AssertUtil.canNotEmpty(src, "Object type conversion failed!");
        AssertUtil.canNotEmpty(desc, "Object type conversion failed!");
        desc.setAddress(src.getAddress().toString());
        desc.setAlias(src.getAlias());
        desc.setCreateTime(src.getCreateTime());
        desc.setExtend(src.getExtend());
        desc.setPriKey(src.getPriKey());
        desc.setPubKey(src.getPubKey());
        desc.setEncryptedPriKey(src.getEncryptedPriKey());
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

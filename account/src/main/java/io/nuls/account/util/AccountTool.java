package io.nuls.account.util;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.ByteBuffer;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.db.entity.AccountPo;
import io.nuls.db.entity.LocalAccountPo;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ln
 */
public final class AccountTool {

    /**
     * create a new address
     *
     * @return Address
     */
    public static Address newAddress(ECKey key) {
        return new Address(Utils.sha256hash160(key.getPubKey(false)));
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
        AssertUtil.canNotEmpty(src, "Object type conversion faild!");
        AssertUtil.canNotEmpty(desc, "Object type conversion faild!");
        desc.parse(new ByteBuffer(src.getBytes()));
        desc.setCreateHeight(src.getCreateHeight());
        desc.setCreateTime(src.getCreateTime());
        desc.setTxHash(src.getTxHash());
        if (src instanceof LocalAccountPo) {
            desc.setPriKey(((LocalAccountPo) src).getPriKey());
            desc.setPriSeed(((LocalAccountPo) src).getPriSeed());
        }
    }

    public static void toPojo(Account src, AccountPo desc) throws IOException, NulsException {
        AssertUtil.canNotEmpty(src, "Object type conversion faild!");
        AssertUtil.canNotEmpty(desc, "Object type conversion faild!");
        desc.setId(src.getId());
        desc.setAddress(src.getAddress().toString());
        desc.setAlias(src.getAlias());
        desc.setCreateHeight(src.getCreateHeight());
        desc.setCreateTime(src.getCreateTime());
        desc.setPubKey(src.getPubKey());
        desc.setTxHash(src.getTxHash());
        desc.setVersion(src.getVersion());
        desc.setBytes(src.serialize());
        if (desc instanceof LocalAccountPo) {
            ((LocalAccountPo) desc).setPriKey(src.getPriKey());
            ((LocalAccountPo) desc).setPriSeed(src.getPriSeed());
        }
    }
}

package io.nuls.account.util;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Account;
import io.nuls.account.model.Address;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.crypto.Sha256Hash;
import io.nuls.core.tools.crypto.Utils;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.func.TimeService;

import java.math.BigInteger;

/**
 * @author: Charlie
 * @date: 2018/5/9
 */
public class AccountTool {
    /**
     * create a new address
     *
     * @return Address
     */
    public static final int CREATE_MAX_SIZE = 100;

    public static Address newAddress(ECKey key) throws NulsException {
        return newAddress(key.getPubKey());
    }

    public static Address newAddress(byte[] publicKey) throws NulsException {
        return new Address(NulsContext.DEFAULT_CHAIN_ID, Utils.sha256hash160(publicKey));
    }

    public static Account createAccount(String prikey) throws NulsException {
        ECKey key = null;
        if (StringUtils.isBlank(prikey)) {
            key = new ECKey();
        } else {
            try {
                key = ECKey.fromPrivate(new BigInteger(Hex.decode(prikey)));
            } catch (Exception e) {
                throw new NulsException(AccountErrorCode.DATA_PARSE_ERROR);
            }
        }
        Address address = new Address(NulsContext.DEFAULT_CHAIN_ID, Utils.sha256hash160(key.getPubKey()));
        Account account = new Account();
        account.setEncryptedPriKey(new byte[0]);
        account.setAddress(address);
        account.setPubKey(key.getPubKey());
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

}

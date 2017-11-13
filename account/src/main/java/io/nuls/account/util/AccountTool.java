package io.nuls.account.util;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.utils.crypto.Utils;

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

}

/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.account.model;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.core.tools.crypto.*;
import io.nuls.core.tools.crypto.Exception.CryptoException;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;
import org.spongycastle.crypto.params.KeyParameter;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * @author: Charlie
 * @date: 2018/5/4
 */
public class Account extends BaseNulsData {


    /**
     * 账户地址
     */

    private Address address;

    /**
     * 账户别名
     */

    private String alias;

    /**
     * is default acct
     */

    private int status;

    /**
     * 账户公钥
     */

    private byte[] pubKey;

    /**
     *
     */

    private byte[] extend;

    /**
     * 创建时间
     */

    private Long createTime;


    private byte[] encryptedPriKey;

    /**
     * Decrypted  prikey
     */

    private byte[] priKey;

    /**
     * local field
     */

    private ECKey ecKey;


    /**
     * 账户是否被加密(是否设置过密码)
     * Whether the account is encrypted (Whether the password is set)
     */
    public boolean isEncrypted() {
        if (getEncryptedPriKey() != null && getEncryptedPriKey().length > 0) {
            return true;
        }
        return false;
    }


    /**
     * 锁定账户
     * Lock account
     */
    public void lock() {
        if (!isEncrypted()) {
            return;
        }

        if (this.getEcKey().getEncryptedPrivateKey() != null) {
            ECKey result = ECKey.fromEncrypted(getEcKey().getEncryptedPrivateKey(), getPubKey());
            this.setPriKey(new byte[0]);
            this.setEcKey(result);
        }
    }

    public byte[] getHash160() {
        return this.getAddress().getHash160();
    }

    /**
     * 根据密码解锁账户
     * Unlock account based on password
     */
    public boolean unlock(String password) throws NulsException {
        decrypt(password);
        if (isLocked()) {
            return false;
        }
        return true;
    }

    /**
     * 账户是否被锁定(是否有明文私钥) 有私钥表示解锁
     * Whether the account is locked (is there a cleartext private key)
     *
     * @return true: Locked, false: not Locked
     */
    public boolean isLocked() {
        return (this.getPriKey() == null) || (this.getPriKey().length == 0);
    }

    public boolean validatePassword(String password) {
        boolean result = StringUtils.validPassword(password);
        if (!result) {
            return result;
        }
        byte[] unencryptedPrivateKey;
        try {
            unencryptedPrivateKey = AESEncrypt.decrypt(this.getEncryptedPriKey(), password);
        } catch (CryptoException e) {
            Log.error(e);
            return false;
        }
        BigInteger newPriv = new BigInteger(1, unencryptedPrivateKey);
        ECKey key = ECKey.fromPrivate(newPriv);

        if (!Arrays.equals(key.getPubKey(), getPubKey())) {
            return false;
        }
        return true;
    }

    /**
     * 根据密码加密账户(给账户设置密码)
     * Password-encrypted account (set password for account)
     */
    public void encrypt(String password) throws NulsException {
        encrypt(password, false);
    }

    /**
     * 根据密码加密账户(给账户设置密码)
     * Password-encrypted account (set password for account)
     */
    public void encrypt(String password, boolean isForce) throws NulsException {
        if (this.isEncrypted()) {
            if (isForce) {
                if (isLocked()) {
                    throw new NulsException(AccountErrorCode.ACCOUNT_IS_ALREADY_ENCRYPTED_AND_LOCKED);
                }
            } else {
                throw new NulsException(AccountErrorCode.ACCOUNT_IS_ALREADY_ENCRYPTED);
            }
        }
        ECKey eckey = this.getEcKey();
        byte[] privKeyBytes = eckey.getPrivKeyBytes();
        EncryptedData encryptedPrivateKey = AESEncrypt.encrypt(privKeyBytes, EncryptedData.DEFAULT_IV, new KeyParameter(Sha256Hash.hash(password.getBytes())));
        eckey.setEncryptedPrivateKey(encryptedPrivateKey);
        ECKey result = ECKey.fromEncrypted(encryptedPrivateKey, getPubKey());
        this.setPriKey(new byte[0]);
        this.setEcKey(result);
        this.setEncryptedPriKey(encryptedPrivateKey.getEncryptedBytes());
    }

    /**
     * 根据解密账户, 包括生成账户明文私钥
     * According to the decryption account, including generating the account plaintext private key
     */
    public boolean decrypt(String password) throws NulsException {
        try {
            byte[] unencryptedPrivateKey = AESEncrypt.decrypt(this.getEncryptedPriKey(), password);
            BigInteger newPriv = new BigInteger(1, unencryptedPrivateKey);
            ECKey key = ECKey.fromPrivate(newPriv);

            if (!Arrays.equals(key.getPubKey(), getPubKey())) {
                return false;
            }
            key.setEncryptedPrivateKey(new EncryptedData(this.getEncryptedPriKey()));
            this.setPriKey(key.getPrivKeyBytes());
            this.setEcKey(key);
        } catch (Exception e) {
            throw new NulsException(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        return true;
    }

    @Override
    public int size() {
        int s = Address.ADDRESS_LENGTH;
        s += SerializeUtils.sizeOfString(alias);
        s += SerializeUtils.sizeOfBytes(encryptedPriKey);
        s += SerializeUtils.sizeOfBytes(pubKey);
        s += 1;
        s += SerializeUtils.sizeOfBytes(extend);
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(address.getAddressBytes());
        stream.writeString(alias);
        stream.writeBytesWithLength(encryptedPriKey);
        stream.writeBytesWithLength(pubKey);
        stream.write(status);
        stream.writeBytesWithLength(extend);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        address = Address.fromHashs(byteBuffer.readBytes(Address.ADDRESS_LENGTH));
        alias = new String(byteBuffer.readByLengthByte());
        encryptedPriKey = byteBuffer.readByLengthByte();
        pubKey = byteBuffer.readByLengthByte();
        status = (int) (byteBuffer.readByte());
        extend = byteBuffer.readByLengthByte();
    }

    public Object copy() {
        Account account = new Account();
        account.setAlias(alias);
        account.setAddress(address);
        account.setStatus(status);
        account.setPubKey(pubKey);
        account.setExtend(extend);
        account.setCreateTime(createTime);
        account.setEncryptedPriKey(encryptedPriKey);
        account.setPriKey(priKey);
        account.setEcKey(ecKey);
        return account;
    }


    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public byte[] getPubKey() {
        return pubKey;
    }

    public void setPubKey(byte[] pubKey) {
        this.pubKey = pubKey;
    }

    public byte[] getExtend() {
        return extend;
    }

    public void setExtend(byte[] extend) {
        this.extend = extend;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public byte[] getEncryptedPriKey() {
        return encryptedPriKey;
    }

    public void setEncryptedPriKey(byte[] encryptedPriKey) {
        this.encryptedPriKey = encryptedPriKey;
    }

    public byte[] getPriKey() {
        return priKey;
    }

    public byte[] getPriKey(String password) throws NulsException {
        boolean result = validatePassword(password);
        if (!result) {
            throw new NulsException(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        byte[] unencryptedPrivateKey;
        try {
            unencryptedPrivateKey = AESEncrypt.decrypt(this.getEncryptedPriKey(), password);
        } catch (CryptoException e) {
            Log.error(e);
            throw new NulsException(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        BigInteger newPriv = new BigInteger(1, unencryptedPrivateKey);
        ECKey key = ECKey.fromPrivate(newPriv);

        if (!Arrays.equals(key.getPubKey(), getPubKey())) {
            throw new NulsException(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        return unencryptedPrivateKey;
    }

    public void setPriKey(byte[] priKey) {
        this.priKey = priKey;
    }

    public ECKey getEcKey() {
        return ecKey;
    }

    public void setEcKey(ECKey ecKey) {
        this.ecKey = ecKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Account)) {
            return false;
        }
        Account other = (Account) obj;
        return Arrays.equals(pubKey, other.getPubKey());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(pubKey);
    }
}

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
package io.nuls.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.sdk.constant.AccountErrorCode;
import io.nuls.sdk.crypto.AESEncrypt;
import io.nuls.sdk.crypto.ECKey;
import io.nuls.sdk.crypto.EncryptedData;
import io.nuls.sdk.crypto.Sha256Hash;
import io.nuls.sdk.exception.NulsException;
import io.nuls.sdk.utils.StringUtils;
import org.spongycastle.crypto.params.KeyParameter;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * @author: Charlie
 * @date: 2018/6/10
 */
public class Account {


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
     *Decrypted  prikey
     */

    private byte[] priKey;

    /**
     * local field
     */
    private ECKey ecKey;


    /**
     * 账户是否被加密(是否设置过密码)
     * Whether the account is encrypted (Whether the password is set)
     * @return
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
    @JsonIgnore
    public byte[] getHash160() {
        return this.getAddress().getHash160();
    }

    /**
     * 根据密码解锁账户
     * Unlock account based on password
     * @param password
     * @return
     * @throws NulsException
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
     * @return true: Locked, false: not Locked
     */
    @JsonIgnore
    public boolean isLocked() {
        return (this.getPriKey() == null) || (this.getPriKey().length == 0);
    }

    public boolean validatePassword(String password) {
        return StringUtils.validPassword(password);
    }

    /**
     * 根据密码加密账户(给账户设置密码)
     * Password-encrypted account (set password for account)
     * @param password
     */
    public void encrypt(String password) throws NulsException {
        encrypt(password, false);
    }

    /**
     * 根据密码加密账户(给账户设置密码)
     * Password-encrypted account (set password for account)
     * @param password
     */
    public void encrypt(String password, boolean isForce) throws NulsException {
        if (this.isEncrypted() && !isForce) {
            if (!unlock(password)) {
                throw new NulsException(AccountErrorCode.PASSWORD_IS_WRONG);
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
     * @param password
     * @return
     * @throws NulsException
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

    public void setPriKey(byte[] priKey) {
        this.priKey = priKey;
    }

    public ECKey getEcKey() {
        return ecKey;
    }

    public void setEcKey(ECKey ecKey) {
        this.ecKey = ecKey;
    }

}

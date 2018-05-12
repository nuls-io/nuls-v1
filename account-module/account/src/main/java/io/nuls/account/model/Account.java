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
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.exception.NulsException;
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
     *Decrypted  prikey
     */

    private byte[] priKey;

    /**
     * local field
     */

    private ECKey ecKey;


    public boolean isEncrypted() {
        if (getEncryptedPriKey() != null && getEncryptedPriKey().length > 0) {
            return true;
        }
        return false;
    }


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

    public boolean unlock(String password) throws NulsException {
        decrypt(password);
        if (isLocked()) {
            return false;
        }
        return true;
    }

    public boolean isLocked() {
        return (this.getPriKey() == null) || (this.getPriKey().length == 0);
    }

    public boolean validatePassword(String password) {
        return StringUtils.validPassword(password);
    }

    /**
     * @param password
     */
    public void encrypt(String password) throws NulsException {
        encrypt(password, false);
    }

    /**
     * @param password
     */
    public void encrypt(String password, boolean isForce) throws NulsException {
        if (this.isEncrypted() && !isForce) {
            if (!unlock(password)) {
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
        int s = SerializeUtils.sizeOfBytes(address.calcBase58bytes());
        s += SerializeUtils.sizeOfString(alias);
        s += SerializeUtils.sizeOfBytes(encryptedPriKey);
        s += SerializeUtils.sizeOfBytes(pubKey);
        s += 1;
        s += SerializeUtils.sizeOfBytes(extend);
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBytesWithLength(address.calcBase58bytes());
        stream.writeString(alias);
        stream.writeBytesWithLength(encryptedPriKey);
        stream.writeBytesWithLength(pubKey);
        stream.write(status);
        stream.writeBytesWithLength(extend);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        address = Address.fromHashs(byteBuffer.readByLengthByte());
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

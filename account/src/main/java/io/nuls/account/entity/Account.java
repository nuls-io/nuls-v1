/**
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
package io.nuls.account.entity;

import io.nuls.core.cfg.NulsConfig;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.crypto.*;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.intf.NulsCloneable;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.protocol.model.BaseNulsData;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.utils.io.NulsByteBuffer;
import io.nuls.protocol.utils.io.NulsOutputStreamBuffer;
import org.spongycastle.crypto.params.KeyParameter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * @author Niels
 * @date 2017/10/30
 */
public class Account extends BaseNulsData implements NulsCloneable {

    private Address address;

    private String alias;

    // is default acct
    private int status;

    private byte[] pubKey;

    private byte[] extend;

    private Long createTime;

    private byte[] encryptedPriKey;
    // Decrypted  prikey
    private byte[] priKey;
    //local field
    private ECKey ecKey;

    private List<Transaction> myTxs;

    public Account() {

    }

    public Account(NulsByteBuffer buffer) throws NulsException {
        super(buffer);
    }

    public ECKey getEcKey() {
        return ecKey;
    }

    public void setEcKey(ECKey ecKey) {
        this.ecKey = ecKey;
    }

    public byte[] getPriKey() {
        return priKey;
    }

    public void setPriKey(byte[] priKey) {
        this.priKey = priKey;
    }

    public boolean isEncrypted() {
        if(getEncryptedPriKey()!=null && getEncryptedPriKey().length>0) {
            return true;
        }
        return false;
    }


    public void lock() {
        if(!isEncrypted()){
            return;
        }

        if(this.getEcKey().getEncryptedPrivateKey()!= null) {
            ECKey result = ECKey.fromEncrypted(getEcKey().getEncryptedPrivateKey(), getPubKey());
            this.setPriKey(new byte[0]);
            this.setEcKey(result);
        }
    }

    public byte[] getHash160(){
        return this.getAddress().getHash160();
    }

    public boolean unlock(String password) throws NulsException {
        decrypt(password);
        if(isLocked()){
            return false;
        }
        return true;
    }

    public boolean isLocked(){
        return (this.getPriKey()== null) || (this.getPriKey().length==0);
    }

    public boolean validatePassword(String password) {
        return StringUtils.validPassword(password);
    }

    /**
     * @param password
     */
    public void encrypt(String password) throws NulsException{
        encrypt( password, false);
    }

    /**
     * @param password
     */
    public void encrypt(String password,boolean isForce) throws NulsException{
        if(this.isEncrypted() && !isForce){
            if(!unlock(password)) {
                throw new NulsException(ErrorCode.ACCOUNT_IS_ALREADY_ENCRYPTED);
            }
        }
        ECKey eckey = this.getEcKey();
        byte [] privKeyBytes = eckey.getPrivKeyBytes();
        EncryptedData encryptedPrivateKey = AESEncrypt.encrypt(privKeyBytes,EncryptedData.DEFAULT_IV,new KeyParameter(Sha256Hash.hash(password.getBytes())));
        eckey.setEncryptedPrivateKey(encryptedPrivateKey);
        ECKey result = ECKey.fromEncrypted(encryptedPrivateKey, getPubKey());
        this.setPriKey(new byte[0]);
        this.setEcKey(result);
        this.setEncryptedPriKey(encryptedPrivateKey.getEncryptedBytes());

    }

    public boolean decrypt(String password) throws NulsException{
        try {
            byte[] unencryptedPrivateKey = AESEncrypt.decrypt(this.getEncryptedPriKey(),password);
            BigInteger newPriv = new BigInteger(1, unencryptedPrivateKey);
            ECKey key = ECKey.fromPrivate(newPriv);

        //todo  pub key compress?
            if (!Arrays.equals(key.getPubKey(), getPubKey())) {
                return false;
            }
            key.setEncryptedPrivateKey(new EncryptedData(this.getEncryptedPriKey()));
            this.setPriKey(key.getPrivKeyBytes());
            this.setEcKey(key);
        }catch (Exception e){
            throw new NulsException(ErrorCode.PASSWORD_IS_WRONG);
        }
        return true;
    }

    @Override
    public int size() {
        int s = 0;

        if (StringUtils.isNotBlank(alias)) {
            try {
                s += alias.getBytes(NulsConfig.DEFAULT_ENCODING).length + 1;
            } catch (UnsupportedEncodingException e) {
                Log.error(e);
            }
        } else {
            s++;
        }
        try {
            s += address.getBase58().getBytes(NulsConfig.DEFAULT_ENCODING).length;
        } catch (UnsupportedEncodingException e) {
            Log.error(e);
        }
        if (null != encryptedPriKey) {
            s += encryptedPriKey.length + 1;
        } else {
            s++;
        }
        s += 1;//status

        s += pubKey.length + 1;
        if (null != extend) {
            s += extend.length + 1;
        } else {
            s++;
        }
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(alias);
        stream.writeString(address.getBase58());
        stream.writeBytesWithLength(encryptedPriKey);
        stream.writeBytesWithLength(pubKey);
        stream.write(new VarInt(status).encode());
        stream.writeBytesWithLength(extend);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        alias = new String(byteBuffer.readByLengthByte());
        address = new Address(new String(byteBuffer.readByLengthByte()));
        encryptedPriKey = byteBuffer.readByLengthByte();
        pubKey = byteBuffer.readByLengthByte();
        status = (int)(byteBuffer.readVarInt());
        extend = byteBuffer.readByLengthByte();
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public byte[] getEncryptedPriKey() {
        return encryptedPriKey;
    }

    public void setEncryptedPriKey(byte[] encryptedPriKey) {
        this.encryptedPriKey = encryptedPriKey;
    }

    public byte[] getExtend() {
        return extend;
    }

    public void setExtend(byte[] extend) {
        this.extend = extend;
    }

    public byte[] getPubKey() {
        return pubKey;
    }

    public void setPubKey(byte[] pubKey) {
        this.pubKey = pubKey;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
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

    public List<Transaction> getMyTxs() {
        return myTxs;
    }

    public void setMyTxs(List<Transaction> myTxs) {
        this.myTxs = myTxs;
    }


//    public static void main(String[] args)throws NulsException{
//        System.out.println("***  create a new account  ***************************");
//        Account testAccount = AccountTool.createAccount();
//        showAccount(testAccount);
//
//        System.out.println("***  encrypt the account   ***************************");
//        testAccount.encrypt("nuls123456");
//        showAccount(testAccount);
//
//        System.out.println("***  decrypt the account   ***************************");
//        testAccount.decrypt("nuls123456");
//        showAccount(testAccount);
//
//        testAccount.lock();
//        System.out.println("***  lock the account   ***************************");
//        showAccount(testAccount);
//
//        testAccount.unlock("nuls123456");
//        System.out.println("***  unlock the account   ***************************");
//        showAccount(testAccount);
//
//        String hash160Hex = Hex.encode(testAccount.getHash160());
//        System.out.println("hash160: "+hash160Hex);
//        System.out.println("hash160_1: "+ Hex.encode(Utils.sha256hash160(testAccount.getPubKey())));
//    }

    public static void  showAccount(Account account){
        System.out.println("---- account info ----");
        System.out.println("Address： "+ account.getAddress().getBase58());
        System.out.println("Public key： "+ Hex.encode(account.getPubKey()));
        System.out.println("Private key："+ Hex.encode(account.getPriKey()));
        System.out.println("Encrypted pri key： "+ Hex.encode(account.getEncryptedPriKey()));
        System.out.println("key object：");
        System.out.println("\tpublic key："+ account.getEcKey().getPublicKeyAsHex());
        try {
            System.out.println("\tprivate key：" + Hex.encode(account.getEcKey().getPrivKeyBytes()));
        }catch (ECKey.MissingPrivateKeyException e){
            System.out.println("\tprivate key：is NULL" );
        }
        System.out.println("\tencrypted pkey： "+ account.getEcKey().getEncryptedPrivateKey());
        System.out.println("---- account info end----");
    }
}

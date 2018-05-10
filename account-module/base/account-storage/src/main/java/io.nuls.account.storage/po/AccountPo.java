package io.nuls.account.storage.po;

import io.nuls.account.model.Account;
import io.nuls.account.model.Address;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.EncryptedData;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.model.BaseNulsData;
import io.protostuff.Tag;

import java.math.BigInteger;

/**
 * @author: Charlie
 * @date: 2018/5/9
 */
public class AccountPo extends BaseNulsData {

    private transient Address addressObj;

    @Tag(1)
    private String address;
    @Tag(2)
    private Long createTime;
    @Tag(3)
    private String alias;
    @Tag(4)
    private byte[] pubKey;
    @Tag(5)
    private byte[] priKey;
    @Tag(6)
    private byte[] encryptedPriKey;
    @Tag(7)
    private byte[] extend;
    @Tag(8)
    private int status;

    public AccountPo(){
    }
    public AccountPo(Account account){
        this.addressObj = account.getAddress();
        this.address = account.getAddress().toString();
        this.createTime = account.getCreateTime();
        this.alias = account.getAlias();
        this.pubKey = account.getPubKey();
        this.priKey = account.getPriKey();
        this.encryptedPriKey = account.getEncryptedPriKey();
        this.extend = account.getExtend();
        this.status = account.getStatus();
    }

    public Account toAccount(){
        Account account = new Account();
        account.setCreateTime(this.getCreateTime());
        try {
            account.setAddress(Address.fromHashs(this.getAddress()));
        } catch (Exception e) {
            Log.error(e);
        }
        account.setAlias(this.getAlias());
        account.setExtend(this.getExtend());
        account.setPriKey(this.getPriKey());
        account.setPubKey(this.getPubKey());
        account.setEncryptedPriKey(this.getEncryptedPriKey());
        if (this.getPriKey() != null && this.getPriKey().length > 1) {
            account.setEcKey(ECKey.fromPrivate(new BigInteger(account.getPriKey())));
        } else {
            account.setEcKey(ECKey.fromEncrypted(new EncryptedData(this.getEncryptedPriKey()), this.getPubKey()));
        }
        account.setStatus(this.getStatus());
        return account;
    }

    public String getAddress() {
        return address;
    }

    public Address getAddressObj() {
        return addressObj;
    }

    public void setAddressObj(Address addressObj) {
        this.addressObj = addressObj;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public byte[] getPubKey() {
        return pubKey;
    }

    public void setPubKey(byte[] pubKey) {
        this.pubKey = pubKey;
    }

    public byte[] getPriKey() {
        return priKey;
    }

    public void setPriKey(byte[] priKey) {
        this.priKey = priKey;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

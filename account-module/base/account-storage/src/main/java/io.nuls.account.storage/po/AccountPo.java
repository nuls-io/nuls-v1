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

    public AccountPo(Account account){
        this.address = account.getAddress().toString();
        this.createTime = account.getCreateTime();
        this.alias = account.getAlias();
        this.pubKey = account.getPubKey();
        this.priKey = account.getPriKey();
        this.encryptedPriKey = account.getEncryptedPriKey();
        this.extend = account.getExtend();
        this.status = account.getStatus();
    }

    public Account toAccount(AccountPo accountPo){
        Account account = new Account();
        account.setCreateTime(accountPo.getCreateTime());
        try {
            account.setAddress(Address.fromHashs(accountPo.getAddress()));
        } catch (Exception e) {
            Log.error(e);
        }
        account.setAlias(accountPo.getAlias());
        account.setExtend(accountPo.getExtend());
        account.setPriKey(accountPo.getPriKey());
        account.setPubKey(accountPo.getPubKey());
        account.setEncryptedPriKey(accountPo.getEncryptedPriKey());
        if (accountPo.getPriKey() != null && accountPo.getPriKey().length > 1) {
            account.setEcKey(ECKey.fromPrivate(new BigInteger(account.getPriKey())));
        } else {
            account.setEcKey(ECKey.fromEncrypted(new EncryptedData(accountPo.getEncryptedPriKey()), accountPo.getPubKey()));
        }

        account.setStatus(accountPo.getStatus());
        return account;
    }

    public String getAddress() {
        return address;
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

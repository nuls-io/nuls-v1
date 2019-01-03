/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
 *
 */

package io.nuls.account.storage.po;

import io.nuls.account.model.Account;
import io.nuls.kernel.model.Address;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.EncryptedData;
import io.nuls.core.tools.log.Log;
import java.math.BigInteger;

/**
 * @author: Charlie
 */
public class AccountPo {

    private transient Address addressObj;


    private String address;

    private Long createTime;

    private String alias;

    private byte[] pubKey;

    private byte[] priKey;

    private byte[] encryptedPriKey;

    private byte[] extend;

    private int status;

    private String remark;

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
        this.remark = account.getRemark();
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
            account.setEcKey(ECKey.fromPrivate(new BigInteger(1, account.getPriKey())));
        } else {
            account.setEcKey(ECKey.fromEncrypted(new EncryptedData(this.getEncryptedPriKey()), this.getPubKey()));
        }
        account.setStatus(this.getStatus());
        account.setRemark(this.remark);
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

package io.nuls.account.rpc.model.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author: Charlie
 * @date: 2018/4/19
 */
@ApiModel(value = "导入账户表单数据")
public class AccountImportForm {

    @ApiModelProperty(name = "accountKeyStore", value = "备份的账户数据", required = true)
    private String accountKeyStore;

    @ApiModelProperty(name = "password", value = "密码")
    private String password;

    public String getAccountKeyStore() {
        return accountKeyStore;
    }

    public void setAccountKeyStore(String accountKeyStore) {
        this.accountKeyStore = accountKeyStore;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

/*  public AccountKeyStore toAccountKeyStore() {
        AccountKeyStore accountKeyStore = new AccountKeyStore();
        accountKeyStore.setAddress(this.address);
        accountKeyStore.setEncryptedPrivateKey(this.encryptedPrivateKey);
        accountKeyStore.setAlias(this.alias);
        accountKeyStore.setPubKey(Hex.decode(this.pubKey));
        accountKeyStore.setPrikey(Hex.decode(this.prikey));
        return accountKeyStore;
    }*/

   /* public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = StringUtils.formatStringPara(encryptedPrivateKey);
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getPrikey() {
        return prikey;
    }

    public void setPrikey(String prikey) {
        this.prikey = prikey;
    }*/
}

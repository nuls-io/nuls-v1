package io.nuls.account.rpc.model;

import io.nuls.account.model.AccountKeyStore;
import io.nuls.core.tools.crypto.Hex;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author: Charlie
 * @date: 2018/5/15
 */
@ApiModel(value = "导出账户JSON")
public class AccountKeyStoreDto {

    @ApiModelProperty(name = "address", value = "账户地址")
    private String address;
    @ApiModelProperty(name = "encryptedPrivateKey", value = "加密后的私钥")
    private String encryptedPrivateKey;
    @ApiModelProperty(name = "alias", value = "账户别名")
    private String alias;
    @ApiModelProperty(name = "pubKey", value = "公钥")
    private String pubKey;
    @ApiModelProperty(name = "prikey", value = "私钥")
    private String prikey;

    public AccountKeyStoreDto() {
    }

    public AccountKeyStoreDto(AccountKeyStore accountKeyStore) {
        this.address = accountKeyStore.getAddress();
        this.encryptedPrivateKey = accountKeyStore.getEncryptedPrivateKey();
        this.alias = accountKeyStore.getAlias();
        this.pubKey = Hex.encode(accountKeyStore.getPubKey());
        this.prikey = Hex.encode(accountKeyStore.getPrikey());
    }

    public AccountKeyStore toAccountKeyStore(){
        AccountKeyStore accountKeyStore = new AccountKeyStore();
        accountKeyStore.setAddress(this.address);
        accountKeyStore.setAlias(this.alias);
        accountKeyStore.setEncryptedPrivateKey(this.encryptedPrivateKey);
        accountKeyStore.setPrikey(Hex.decode(this.prikey));
        accountKeyStore.setPubKey(Hex.decode(this.pubKey));
        return accountKeyStore;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
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
}

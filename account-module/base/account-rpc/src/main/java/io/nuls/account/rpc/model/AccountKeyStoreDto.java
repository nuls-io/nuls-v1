package io.nuls.account.rpc.model;

import io.nuls.account.model.AccountKeyStore;
import io.nuls.core.tools.crypto.Hex;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

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
        this.encryptedPrivateKey = null == accountKeyStore.getEncryptedPrivateKey() ? null : accountKeyStore.getEncryptedPrivateKey();
        this.alias = accountKeyStore.getAlias();
        this.pubKey = Hex.encode(accountKeyStore.getPubKey());
        this.prikey = null == accountKeyStore.getPrikey() ? null : Hex.encode(accountKeyStore.getPrikey());
    }

    public AccountKeyStore toAccountKeyStore(){
        AccountKeyStore accountKeyStore = new AccountKeyStore();
        accountKeyStore.setAddress(this.address);
        accountKeyStore.setAlias(this.alias);
        accountKeyStore.setEncryptedPrivateKey(this.encryptedPrivateKey);
        accountKeyStore.setPrikey(null == this.prikey ? null : Hex.decode(this.prikey));
        accountKeyStore.setPubKey(Hex.decode(this.pubKey));
        return accountKeyStore;
    }

    public AccountKeyStoreDto(Map<String, Object> map) {
        this.address = (String)map.get("address");
        this.encryptedPrivateKey = null == map.get("encryptedPrivateKey") ? null : (String)map.get("encryptedPrivateKey");
        this.alias = null == map.get("alias") ? null : (String)map.get("alias");
        this.pubKey = null == map.get("pubKey") ? null : (String)map.get("pubKey");
        this.prikey = null == map.get("prikey") ? null : (String)map.get("prikey");
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

    public String getPrikey() {
        return prikey;
    }

    public void setPrikey(String prikey) {
        this.prikey = prikey;
    }
}

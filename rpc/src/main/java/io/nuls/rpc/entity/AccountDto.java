package io.nuls.rpc.entity;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.core.utils.crypto.Hex;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "accountJSON")
public class AccountDto {

    @ApiModelProperty(name = "address", value = "账户地址")
    private String address;

    @ApiModelProperty(name = "alias", value = "别名")
    private String alias;

    @ApiModelProperty(name = "pubKey", value = "公钥Hex.encode(byte[])")
    private String pubKey;

    @ApiModelProperty(name = "extend", value = "其他信息Hex.encode(byte[])")
    private String extend;

    @ApiModelProperty(name = "createTime", value = "创建时间")
    private Long createTime;

    public AccountDto() {

    }

    public AccountDto(Account account) {
        this.address = account.getAddress().getBase58();
        this.alias = account.getAlias();
        this.pubKey = Hex.encode(account.getPubKey());
        this.createTime = account.getCreateTime();
        if (account.getExtend() != null)
            this.extend = Hex.encode(account.getExtend());
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}

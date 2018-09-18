package io.nuls.account.rpc.model.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author: tag
 */
@ApiModel(value = "多签账户创建别名交易表单数据")
public class CreateMultiAliasForm {
    @ApiModelProperty(name = "address", value = "账户地址", required = true)
    private String address;

    @ApiModelProperty(name = "alias", value = "别名", required = true)
    private String alias;

    @ApiModelProperty(name = "password", value = "账户密码", required = true)
    private String password;

    @ApiModelProperty(name = "signAddress", value = "签名地址", required = true)
    private String signAddress;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSignAddress() {
        return signAddress;
    }

    public void setSignAddress(String signAddress) {
        this.signAddress = signAddress;
    }
}

package io.nuls.rpc.resources.form;

import io.nuls.core.utils.str.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author: Charlie
 * @date: 2018/4/18
 */
@ApiModel(value = "设置账户别名表单数据")
public class AccountAliasForm {

    @ApiModelProperty(name = "address", value = "账户地址", required = true)
    private String address;

    @ApiModelProperty(name = "password", value = "账户密码", required = true)
    private String password;

    @ApiModelProperty(name = "alias", value = "别名", required = true)
    private String alias;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = StringUtils.formatStringPara(address);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = StringUtils.formatStringPara(password);
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = StringUtils.formatStringPara(alias);
    }
}

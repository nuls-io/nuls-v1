package io.nuls.rpc.resources.form;

import io.nuls.core.utils.str.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author: Charlie
 * @date: 2018/4/18
 */
@ApiModel(value = "查询私钥表单数据")
public class AccountPrikeyForm {

    @ApiModelProperty(name = "address", value = "账户地址", required = true)
    private String address;

    @ApiModelProperty(name = "password", value = "账户密码", required = true)
    private String password;

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
}

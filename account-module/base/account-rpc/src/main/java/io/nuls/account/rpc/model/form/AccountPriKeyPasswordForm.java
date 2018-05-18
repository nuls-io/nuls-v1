package io.nuls.account.rpc.model.form;

import io.nuls.core.tools.str.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author: Charlie
 * @date: 2018/4/19
 */
@ApiModel(value = "私钥密码表单数据")
public class AccountPriKeyPasswordForm {

    @ApiModelProperty(name = "priKey", value = "私钥", required = true)
    private String priKey;

    @ApiModelProperty(name = "password", value = "密码")
    private String password;

    public String getPriKey() {
        return priKey;
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = StringUtils.formatStringPara(password);
    }

}

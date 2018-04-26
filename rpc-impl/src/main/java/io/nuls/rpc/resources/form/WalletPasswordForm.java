package io.nuls.rpc.resources.form;

import io.nuls.core.utils.str.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author: Charlie
 * @date: 2018/4/18
 */
@ApiModel(value = "重置钱包密码单数据")
public class WalletPasswordForm {

    @ApiModelProperty(name = "password", value = "原始密码", required = true)
    private String password;

    @ApiModelProperty(name = "newPassword", value = "新密码", required = true)
    private String newPassword;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = StringUtils.formatStringPara(password);
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = StringUtils.formatStringPara(newPassword);
    }
}

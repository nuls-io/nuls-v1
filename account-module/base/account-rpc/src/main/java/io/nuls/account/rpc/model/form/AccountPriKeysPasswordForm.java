package io.nuls.account.rpc.model.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/7/21
 */
@ApiModel(value = "多个私钥密码表单数据")
public class AccountPriKeysPasswordForm {

    @ApiModelProperty(name = "priKey", value = "私钥", required = true)
    private List<String> priKey;

    @ApiModelProperty(name = "password", value = "密码")
    private String password;

    public List<String> getPriKey() {
        return priKey;
    }

    public void setPriKey(List<String> priKey) {
        this.priKey = priKey;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}

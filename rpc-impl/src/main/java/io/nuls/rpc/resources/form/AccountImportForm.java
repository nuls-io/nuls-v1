package io.nuls.rpc.resources.form;

import io.nuls.core.utils.str.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author: Charlie
 * @date: 2018/4/19
 */
@ApiModel(value = "导入账户表单数据")
public class AccountImportForm {

    @ApiModelProperty(name = "password", value = "账户密码", required = true)
    private String password;

    @ApiModelProperty(name = "prikey", value = "私钥", required = true)
    private String prikey;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = StringUtils.formatStringPara(password);
    }

    public String getPrikey() {
        return prikey;
    }

    public void setPrikey(String prikey) {
        this.prikey = StringUtils.formatStringPara(prikey);
    }
}

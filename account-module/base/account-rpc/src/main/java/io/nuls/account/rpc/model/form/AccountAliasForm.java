package io.nuls.account.rpc.model.form;

import io.nuls.core.tools.str.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author: Charlie
 * @date: 2018/5/13
 */
@ApiModel(value = "设置账户别名表单数据")
public class AccountAliasForm {

    @ApiModelProperty(name = "alias", value = "别名", required = true)
    private String alias;

    @ApiModelProperty(name = "password", value = "账户密码", required = true)
    private String password;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = StringUtils.formatStringPara(alias);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

package io.nuls.account.rpc.model.form;

import io.nuls.core.tools.str.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author: Charlie
 * @date: 2018/5/13
 */
@ApiModel(value = "获取设置账户别名手续费表单数据")
public class AccountAliasFeeForm {

    @ApiModelProperty(name = "alias", value = "别名", required = true)
    private String alias;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = StringUtils.formatStringPara(alias);
    }
}

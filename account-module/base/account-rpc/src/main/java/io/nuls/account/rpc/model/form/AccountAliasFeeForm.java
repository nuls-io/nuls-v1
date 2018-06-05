package io.nuls.account.rpc.model.form;

import io.nuls.core.tools.str.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.ws.rs.QueryParam;

/**
 * @author: Charlie
 * @date: 2018/5/13
 */
@ApiModel(value = "获取设置账户别名手续费表单数据")
public class AccountAliasFeeForm {

    @ApiModelProperty(name = "address", value = "地址", required = true)
    @QueryParam("address")
    private String address;

    @ApiModelProperty(name = "alias", value = "别名", required = true)
    @QueryParam("alias")
    private String alias;

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
        this.alias = StringUtils.formatStringPara(alias);
    }
}

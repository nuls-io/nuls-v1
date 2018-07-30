package io.nuls.account.rpc.model.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.ws.rs.FormParam;

/**
 * @author: Charlie
 * @date: 2018/7/27
 */
@ApiModel(value = "账户备注")
public class AccountRemarkForm {

    @ApiModelProperty(name = "remark", value = "备注")
    @FormParam("remark")
    private String remark;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}

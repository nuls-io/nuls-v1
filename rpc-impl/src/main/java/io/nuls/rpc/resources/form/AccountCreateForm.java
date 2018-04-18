package io.nuls.rpc.resources.form;

import io.nuls.core.utils.str.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author: Charlie
 * @date: 2018/4/18
 */
@ApiModel(value = "创建账户表单数据")
public class AccountCreateForm {

    @ApiModelProperty(name = "count", value = "新建账户数量", required = true)
    private int count;

    @ApiModelProperty(name = "password", value = "账户密码", required = true)
    private String password;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = StringUtils.formatStringPara(password);
    }
}

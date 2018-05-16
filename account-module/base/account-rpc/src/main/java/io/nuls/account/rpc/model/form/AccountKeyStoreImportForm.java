package io.nuls.account.rpc.model.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author: Charlie
 * @date: 2018/5/16
 */
@ApiModel(value = "导入账户KeyStore表单数据")
public class AccountKeyStoreImportForm {

    @ApiModelProperty(name = "accountKeyStore", value = "备份的账户数据", required = true)
    private String accountKeyStore;

    @ApiModelProperty(name = "password", value = "密码")
    private String password;

    public String getAccountKeyStore() {
        return accountKeyStore;
    }

    public void setAccountKeyStore(String accountKeyStore) {
        this.accountKeyStore = accountKeyStore;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

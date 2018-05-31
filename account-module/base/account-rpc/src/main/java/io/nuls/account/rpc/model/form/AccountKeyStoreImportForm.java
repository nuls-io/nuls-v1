package io.nuls.account.rpc.model.form;

import io.nuls.account.rpc.model.AccountKeyStoreDto;
import io.nuls.core.tools.str.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author: Charlie
 * @date: 2018/5/16
 */
@ApiModel(value = "导入账户KeyStore表单数据")
public class AccountKeyStoreImportForm {

    @ApiModelProperty(name = "accountKeyStoreDto", value = "备份的账户数据", required = true)
    private AccountKeyStoreDto accountKeyStoreDto;

    @ApiModelProperty(name = "password", value = "密码")
    private String password;

    @ApiModelProperty(name = "overwrite", value = "是否覆盖账户: false:不覆盖导入, true:覆盖导入")
    private Boolean overwrite = false;

    public AccountKeyStoreDto getAccountKeyStoreDto() {
        return accountKeyStoreDto;
    }

    public void setAccountKeyStoreDto(AccountKeyStoreDto accountKeyStoreDto) {
        this.accountKeyStoreDto = accountKeyStoreDto;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = StringUtils.formatStringPara(password);
    }

    public Boolean getOverwrite() {
        return overwrite;
    }

    public void setOverwrite(Boolean overwrite) {
        this.overwrite = overwrite;
    }
}

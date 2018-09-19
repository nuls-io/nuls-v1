package io.nuls.contract.rpc.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/8/15
 */
@ApiModel(value = "智能合约基础参数")
public class ContractAddressBase {

    @ApiModelProperty(name = "accountAddress", value = "钱包账户地址", required = true)
    private String accountAddress;
    @ApiModelProperty(name = "contractAddress", value = "智能合约地址", required = true)
    private String contractAddress;

    public String getAccountAddress() {
        return accountAddress;
    }

    public void setAccountAddress(String accountAddress) {
        this.accountAddress = accountAddress;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

}

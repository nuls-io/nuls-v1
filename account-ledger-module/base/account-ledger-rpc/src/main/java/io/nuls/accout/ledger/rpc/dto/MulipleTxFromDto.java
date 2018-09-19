package io.nuls.accout.ledger.rpc.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "多地址转账from数据")
public class MulipleTxFromDto {

    @ApiModelProperty(name = "address", value = "账户地址", required = true)
    private String address;

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }


}
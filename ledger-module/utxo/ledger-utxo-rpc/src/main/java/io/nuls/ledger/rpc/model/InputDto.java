package io.nuls.ledger.rpc.model;

import io.nuls.core.tools.crypto.Base58;
import io.nuls.kernel.model.Coin;
import io.nuls.ledger.utils.LedgerUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "inputJSON")
public class InputDto {

    @ApiModelProperty(name = "fromHash", value = "来源output的txHash")
    private String fromHash;

    @ApiModelProperty(name = "fromIndex", value = "来源output的outIndex")
    private Integer fromIndex;

    @ApiModelProperty(name = "address", value = "转入地址")
    private String address;

    @ApiModelProperty(name = "value", value = "转入金额")
    private Long value;

    public InputDto(Coin input) {
        this.fromHash = LedgerUtil.getTxHash(input.getOwner());
        this.fromIndex = LedgerUtil.getIndex(input.getOwner());
        this.address = Base58.encode(input.getFrom().getOwner());
        this.value = input.getNa().getValue();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public String getFromHash() {
        return fromHash;
    }

    public void setFromHash(String fromHash) {
        this.fromHash = fromHash;
    }

    public Integer getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(Integer fromIndex) {
        this.fromIndex = fromIndex;
    }
}

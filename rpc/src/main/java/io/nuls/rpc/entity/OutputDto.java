package io.nuls.rpc.entity;

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.Na;
import io.nuls.ledger.entity.UtxoOutput;

public class OutputDto {

    private Integer index;

    private String address;

    private Double value;

    public OutputDto(UtxoOutput output) {
        this.index = output.getIndex();
        this.address = Address.fromHashs(output.getAddress()).getBase58();
        this.value = Na.valueOf(output.getValue()).toDouble();
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}

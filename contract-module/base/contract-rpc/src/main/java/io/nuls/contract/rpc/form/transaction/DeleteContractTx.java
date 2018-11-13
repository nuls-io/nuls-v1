package io.nuls.contract.rpc.form.transaction;

import io.nuls.accout.ledger.rpc.dto.InputDto;

import java.util.List;

/**
 * when you use sdk delete contract. DeleteContractTx is required
 * Created by wangkun23 on 2018/11/13.
 */
public class DeleteContractTx {

    private String sender;
    private String contractAddress;
    private String remark;
    private List<InputDto> utxos;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<InputDto> getUtxos() {
        return utxos;
    }

    public void setUtxos(List<InputDto> utxos) {
        this.utxos = utxos;
    }
}

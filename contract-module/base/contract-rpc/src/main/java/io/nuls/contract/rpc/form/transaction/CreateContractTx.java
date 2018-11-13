package io.nuls.contract.rpc.form.transaction;

import io.nuls.kernel.model.Coin;

import java.util.List;

/**
 * use sdk create contract request
 * Created by wangkun23 on 2018/11/13.
 */
public class CreateContractTx {

    private String sender;
    private long gasLimit;
    private Long price;
    private byte[] contractCode;
    private Object[] args;
    private String remark;
    private List<Coin> utxos;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public byte[] getContractCode() {
        return contractCode;
    }

    public void setContractCode(byte[] contractCode) {
        this.contractCode = contractCode;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<Coin> getUtxos() {
        return utxos;
    }

    public void setUtxos(List<Coin> utxos) {
        this.utxos = utxos;
    }
}

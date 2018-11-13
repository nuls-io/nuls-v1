package io.nuls.contract.rpc.form.transaction;

import io.nuls.accout.ledger.rpc.dto.InputDto;
import io.nuls.kernel.model.Na;

import java.util.List;

/**
 * user sdk call contract
 * Created by wangkun23 on 2018/11/13.
 */
public class CallContractTx {

    private String sender;
    private Long value;
    private Long gasLimit;
    private Long price;
    private String contractAddress;
    private String methodName;
    private String methodDesc;
    private Object[] args;
    private String remark;
    private List<InputDto> utxos;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(Long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
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

    public List<InputDto> getUtxos() {
        return utxos;
    }

    public void setUtxos(List<InputDto> utxos) {
        this.utxos = utxos;
    }
}

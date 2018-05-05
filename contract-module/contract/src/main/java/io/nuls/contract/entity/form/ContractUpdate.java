package io.nuls.contract.entity.form;

public class ContractUpdate extends ContractBase {

    private byte[] contractCode;
    /**
     * String contractAddress,
     * ...
     */
    private Object[] args;

    public byte[] getContractCode() {
        return contractCode;
    }

    public void setContractCode(byte[] contractCode) {
        this.contractCode = contractCode;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object... args) {
        this.args = args;
    }

}

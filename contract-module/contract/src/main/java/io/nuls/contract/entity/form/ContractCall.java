package io.nuls.contract.entity.form;

public class ContractCall extends ContractBase {

    /**
     * String contractAddress,
     * String methodName,
     * String methodDesc,
     * ...
     */
    private Object[] args;

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object... args) {
        this.args = args;
    }

}

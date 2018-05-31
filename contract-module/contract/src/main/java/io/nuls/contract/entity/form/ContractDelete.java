package io.nuls.contract.entity.form;

public class ContractDelete extends ContractBase {

    /**
     * String contractAddress,
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

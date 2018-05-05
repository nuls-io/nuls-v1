package io.nuls.contract.dto;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date: 2018/4/20
 */
public class ContractResult {
    private String contractAddress;
    private long naUsed;
    private Object result;

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public long getNaUsed() {
        return naUsed;
    }

    public void setNaUsed(long naUsed) {
        this.naUsed = naUsed;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}

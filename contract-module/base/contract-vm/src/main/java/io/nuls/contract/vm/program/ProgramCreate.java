package io.nuls.contract.vm.program;

import io.nuls.kernel.utils.AddressTool;

import java.math.BigInteger;
import java.util.Arrays;

import static io.nuls.contract.util.ContractUtil.argToString;

public class ProgramCreate {

    /**
     * 当前块编号
     */
    private long number;

    /**
     * 创建者
     */
    private byte[] sender;

    /**
     * 交易附带的货币量
     */
    private BigInteger value;

    /**
     * 最大Gas消耗
     */
    private long gasLimit;

    /**
     * 执行合约单价
     */
    private long price;

    /**
     * 合约地址
     */
    private byte[] contractAddress;

    /**
     * 合约代码
     */
    private byte[] contractCode;

    /**
     * 参数列表
     */
    private String[][] args;

    /**
     * 是否估计Gas
     */
    private boolean estimateGas;

    public void args(String... args) {
        setArgs(args);
    }

    public String[][] getArgs() {
        return args;
    }

    public void setArgs(String[][] args) {
        this.args = args;
    }

    public void setArgs(String[] args) {
        this.args = ProgramCall.twoDimensionalArray(args);
    }

    public ProgramCreate() {
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public byte[] getSender() {
        return sender;
    }

    public void setSender(byte[] sender) {
        this.sender = sender;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public byte[] getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(byte[] contractAddress) {
        this.contractAddress = contractAddress;
    }

    public byte[] getContractCode() {
        return contractCode;
    }

    public void setContractCode(byte[] contractCode) {
        this.contractCode = contractCode;
    }

    public boolean isEstimateGas() {
        return estimateGas;
    }

    public void setEstimateGas(boolean estimateGas) {
        this.estimateGas = estimateGas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProgramCreate that = (ProgramCreate) o;

        if (number != that.number) return false;
        if (gasLimit != that.gasLimit) return false;
        if (price != that.price) return false;
        if (estimateGas != that.estimateGas) return false;
        if (!Arrays.equals(sender, that.sender)) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (!Arrays.equals(contractAddress, that.contractAddress)) return false;
        if (!Arrays.equals(contractCode, that.contractCode)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(args, that.args);
    }

    @Override
    public int hashCode() {
        int result = (int) (number ^ (number >>> 32));
        result = 31 * result + Arrays.hashCode(sender);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (int) (gasLimit ^ (gasLimit >>> 32));
        result = 31 * result + (int) (price ^ (price >>> 32));
        result = 31 * result + Arrays.hashCode(contractAddress);
        result = 31 * result + Arrays.hashCode(contractCode);
        result = 31 * result + Arrays.hashCode(args);
        result = 31 * result + (estimateGas ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProgramCreate{" +
                "number=" + number +
                ", sender=" + (sender != null ? AddressTool.getStringAddressByBytes(sender) : sender) +
                ", value=" + value +
                ", gasLimit=" + gasLimit +
                ", price=" + price +
                ", contractAddress=" + (contractAddress != null ? AddressTool.getStringAddressByBytes(contractAddress) : contractAddress) +
                ", contractCode=" + (contractCode != null ? String.valueOf(contractCode.length) : 0) +
                ", args=" + argToString(args) +
                ", estimateGas=" + estimateGas +
                '}';
    }
}

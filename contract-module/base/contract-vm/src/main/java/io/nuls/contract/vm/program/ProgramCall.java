/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.contract.vm.program;

import io.nuls.kernel.utils.AddressTool;

import java.math.BigInteger;
import java.util.Arrays;

import static io.nuls.contract.util.ContractUtil.argToString;

public class ProgramCall {

    /**
     * 当前块编号
     */
    private long number;

    /**
     * 调用者
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
     * 方法名
     */
    private String methodName;

    /**
     * 方法签名，如果方法名不重复，可以不传
     */
    private String methodDesc;

    /**
     * 参数列表
     */
    private String[][] args;

    /**
     * 是否估计Gas
     */
    private boolean estimateGas;

    private boolean internalCall;

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
        this.args = twoDimensionalArray(args);
    }

    public static String[][] twoDimensionalArray(String[] args) {
        if (args == null) {
            return null;
        } else {
            String[][] two = new String[args.length][0];
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg != null) {
                    two[i] = new String[]{arg};
                }
            }
            return two;
        }
    }

    public ProgramCall() {
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

    public boolean isEstimateGas() {
        return estimateGas;
    }

    public void setEstimateGas(boolean estimateGas) {
        this.estimateGas = estimateGas;
    }

    public boolean isInternalCall() {
        return internalCall;
    }

    public void setInternalCall(boolean internalCall) {
        this.internalCall = internalCall;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProgramCall that = (ProgramCall) o;

        if (number != that.number) {
            return false;
        }
        if (gasLimit != that.gasLimit) {
            return false;
        }
        if (price != that.price) {
            return false;
        }
        if (estimateGas != that.estimateGas) {
            return false;
        }
        if (!Arrays.equals(sender, that.sender)) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }
        if (!Arrays.equals(contractAddress, that.contractAddress)) {
            return false;
        }
        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) {
            return false;
        }
        if (methodDesc != null ? !methodDesc.equals(that.methodDesc) : that.methodDesc != null) {
            return false;
        }
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
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        result = 31 * result + (methodDesc != null ? methodDesc.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(args);
        result = 31 * result + (estimateGas ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProgramCall{" +
                "number=" + number +
                ", sender=" + (sender != null ? AddressTool.getStringAddressByBytes(sender) : sender) +
                ", value=" + value +
                ", gasLimit=" + gasLimit +
                ", price=" + price +
                ", contractAddress=" + (contractAddress != null ? AddressTool.getStringAddressByBytes(contractAddress) : contractAddress) +
                ", methodName=" + methodName +
                ", methodDesc=" + methodDesc +
                ", args=" + argToString(args) +
                ", estimateGas=" + estimateGas +
                '}';
    }
}

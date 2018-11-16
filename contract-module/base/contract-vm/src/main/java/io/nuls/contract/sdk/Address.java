package io.nuls.contract.sdk;

import java.math.BigInteger;

public class Address {

    private final String address;

    public Address(String address) {
        valid(address);
        this.address = address;
    }

    /**
     * 获取该地址的余额（只能获取合约地址余额）
     *
     * @return BigInteger
     */
    public native BigInteger balance();

    /**
     * 合约向该地址转账
     *
     * @param value 转账金额（多少Na）
     */
    public native void transfer(BigInteger value);

    /**
     * 调用该地址的合约方法
     *
     * @param methodName 方法名
     * @param methodDesc 方法签名
     * @param args       参数
     * @param value      附带的货币量（多少Na）
     */
    public native void call(String methodName, String methodDesc, String[][] args, BigInteger value);

    /**
     * 调用该地址的合约方法并带有返回值(String)
     *
     * @param methodName 方法名
     * @param methodDesc 方法签名
     * @param args       参数
     * @param value      附带的货币量（多少Na）
     * @return 调用合约后的返回值
     */
    public native String callWithReturnValue(String methodName, String methodDesc, String[][] args, BigInteger value);

    /**
     * 验证地址
     *
     * @param address 地址
     */
    private native void valid(String address);

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Address address1 = (Address) o;
        return address != null ? address.equals(address1.address) : address1.address == null;
    }

    @Override
    public int hashCode() {
        return address != null ? address.hashCode() : 0;
    }

    @Override
    public String toString() {
        return address;
    }

}

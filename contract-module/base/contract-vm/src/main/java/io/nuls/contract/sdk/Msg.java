package io.nuls.contract.sdk;

import java.math.BigInteger;

public class Msg {

    /**
     * 剩余Gas
     * remaining gas
     *
     * @return 剩余gas
     */
    public static native long gasleft();

    /**
     * 合约发送者地址
     * sender of the contract
     *
     * @return 消息发送者地址
     */
    public static native Address sender();

    /**
     * 合约发送者转入合约地址的Nuls数量，单位是Na，1Nuls=1亿Na
     * The number of Nuls transferred by the contract sender to the contract address, the unit is Na, 1Nuls = 1 billion Na
     *
     * @return
     */
    public static native BigInteger value();

    /**
     * Gas价格
     * gas price
     *
     * @return Gas价格
     */
    public static native long gasprice();

    /**
     * 合约地址
     * contract address
     *
     * @return 合约地址
     */
    public static native Address address();

}

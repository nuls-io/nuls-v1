/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package testcontract.multytransfer;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Block;
import io.nuls.contract.sdk.Contract;
import io.nuls.contract.sdk.Msg;
import io.nuls.contract.sdk.annotation.Payable;

import java.math.BigInteger;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/7/26
 */
public class TestMultyTransfer implements Contract {
    private String name = "TestMultyTransfer";
    private final String symbol = "TT";
    private final int decimals = 18;

    @Override
    @Payable
    public void _payable() {

    }

    public TestMultyTransfer() {
        name += " - AW158U";
        allInfo = "";
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getDecimals() {
        return decimals;
    }

    public String balance() {
        return Msg.address().balance().toString();
    }

    public String single() {
        Address address_1 = new Address("NsdtgQGAxXPh53oZxAWjVpnHnK7mu4wY");
        address_1.transfer(BigInteger.valueOf(10000000));
        return "balance: " + Msg.address().balance().toString();
    }

    @Payable
    public String multy() {
        //Utils.revert();
        Address address_1 = new Address("NsdtgQGAxXPh53oZxAWjVpnHnK7mu4wY");
        address_1.transfer(BigInteger.valueOf(10000001));
        //address_1.transfer(BigInteger.valueOf(10000000));
        Address address_2 = new Address("Nse5ZkiQs1Q1pdEtRKwXk6Nk6ooYu2L2");
        address_2.transfer(BigInteger.valueOf(20000000));
        Address address_3 = new Address("NsdtydTVWskMc7GkZzbsq2FoChqKFwMf");
        address_3.transfer(BigInteger.valueOf(30000000));
        //address_3.transfer(BigInteger.valueOf(30000000));
        return "balance: " + Msg.address().balance().toString();
    }

    private String allInfo;
    public String allInfo() {
        append("\nsender: " + Msg.sender().toString());
        append("\naddress: " + Msg.address().toString());
        append("\nvalue: " + Msg.value());
        append("\ngasleft: " + Msg.gasleft());
        append("\ngasprice: " + Msg.gasprice());
        append("\n\n\n");
        append("\nbestBlockHash: " + Block.blockhash(Block.number()));
        append("\ncoinbase: " + Block.coinbase());
        append("\nheight: " + Block.number());
        append("\ntimestamp: " + Block.timestamp());
        append("\n");
        return allInfo;
    }

    private void append(String appender) {
        allInfo += appender;
    }

}

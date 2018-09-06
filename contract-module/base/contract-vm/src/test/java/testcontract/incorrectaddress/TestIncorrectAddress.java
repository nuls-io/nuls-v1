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
package testcontract.incorrectaddress;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Contract;
import io.nuls.contract.sdk.Msg;

import java.math.BigInteger;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/8/4
 */
public class TestIncorrectAddress implements Contract {

    private String name = "TestIncorrectAddress";

    public String getName() {
        return name;
    }

    public String setName(String name) {
        this.name = name;
        return name;
    }

    public String getBalance() {
        return Msg.address().balance().toString();
    }


    public String multy() {
        Address address_1 = new Address("NsdtgQGAxXPh53oZxAWjVpnHnK7mu4wY");
        address_1.transfer(BigInteger.valueOf(10000001));
        Address address_2 = new Address("Nse5ZkiQs1Q1pdEtRKwXk6Nk6ooYu2L2");
        address_2.transfer(BigInteger.valueOf(20000000));
        // incorrect address
        Address address_3 = new Address("NadtydTVWskMc7GkZzbsq2Fommmmmmmm");
        address_3.transfer(BigInteger.valueOf(30000000));
        return "balance: " + Msg.address().balance().toString();
    }
}

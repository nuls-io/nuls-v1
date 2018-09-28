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
package testcontract.simple;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Contract;
import io.nuls.contract.sdk.Msg;
import io.nuls.contract.sdk.Utils;
import io.nuls.contract.sdk.annotation.View;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/7/4
 */
public class TestContract implements Contract {
    private String name = "TestToken";
    private final String symbol = "TT";
    private final int decimals = 18;

    private Map<String, String> map = new HashMap<>();

    @View
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

    public String transfer() {
        Address address = new Address("NsdtgQGAxXPh53oZxAWjVpnHnK7mu4wY");
        address.transfer(BigInteger.valueOf(10000000));
        return "AW158U";
    }

    //@View
    //public int randomNumberInt() {
    //    return Utils.pseudoRandom(1, 16);
    //}
    //
    //@View
    //public int randomNumberString() {
    //    return Utils.pseudoRandom("a", 16);
    //}
    //
    //@View
    //public int randomNumberWithIntAndCap(int cap) {
    //    return Utils.pseudoRandom(1, cap);
    //}
    //
    //@View
    //public int randomNumberWithStringAndCap(int cap) {
    //    return Utils.pseudoRandom("a", cap);
    //}

    public TestContract() {
        name += " - AW158U";
        map.put("123", "123a");
        map.put("124", "124a");
        map.put("125", "125a");
        map.put("126", "126a");
        map.put("127", "127a");
    }

    //public String setName(String name, int cap) {
    //    this.name += name + Utils.pseudoRandom("a", cap);
    //    return this.name;
    //}

    @View
    public String map() {
        String result = "";
        //for(Map.Entry<String, String> entry : map.entrySet()) {
        //    result += entry.getKey() + ": " + entry.getValue() + " \n";
        //}
        for(String key : map.keySet()) {
            result += key + ": " + map.get(key) + " \n";
        }
        return result;
    }

    @View
    public String asd(String b, String[] a) {
        String result = "";
        if(a != null) {
            for(String aaa : a) {
                result += aaa;
            }
        }
        return result + b;
    }

    @View
    public String qwe(String b, String a, String c) {
        String result = "";
        return result + b + a +c;
    }

}

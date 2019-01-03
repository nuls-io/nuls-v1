/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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
package testcontract.test;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Contract;
import io.nuls.contract.sdk.Msg;
import io.nuls.contract.sdk.Utils;
import io.nuls.contract.sdk.annotation.View;

import java.math.BigInteger;
import java.util.*;

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
        String result = Msg.address().balance().toString();
        result += ", SHA3Hash is " + Utils.sha3(result);
        return result;
    }

    public String transfer() {
        Address address = new Address("NsdtgQGAxXPh53oZxAWjVpnHnK7mu4wY");
        address.transfer(BigInteger.valueOf(10000000));
        return "AW158U";
    }

    public TestContract() {
        name += " - AW158U";
        map.put("123", "123a");
        map.put("124", "124a");
        map.put("125", "125a");
        map.put("126", "126a");
        map.put("127", "127a");
    }

    @View
    public String linkedHashMap_EntrySet() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("123", "123a");
        map.put("124", "124a");
        map.put("125", "125a");
        map.put("126", "126a");
        map.put("127", "127a");

        String result = "";
        Set<Map.Entry<String, String>> entries = map.entrySet();
        for(Map.Entry<String, String> entry : entries) {
            result += entry.getKey() + ": " + entry.getValue() + " \n";
        }

        result += ", SHA3Hash is " + Utils.sha3(result);
        return result;
    }

    @View
    public String linkedHashMap_values() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("123", "123a");
        map.put("124", "124a");
        map.put("125", "125a");
        map.put("126", "126a");
        map.put("127", "127a");

        String result = "";
        Collection<String> values = map.values();
        String next = values.iterator().next();
        result += next + " <- next \n";
        for(String ss : values) {
            result += ss + " \n";
        }
        result += ", SHA3Hash is " + Utils.sha3(result);
        return result;
    }

    @View
    public String hashMap_EntrySet() {
        String result = "";
        Set<Map.Entry<String, String>> entries = map.entrySet();
        for(Map.Entry<String, String> entry : entries) {
            result += entry.getKey() + ": " + entry.getValue() + " \n";
        }

        result += ", SHA3Hash is " + Utils.sha3(result);
        return result;
    }

    @View
    public String hashMap_Set() {
        String result = "";
        Set<String> strings = map.keySet();
        for(String key : strings) {
            result += key + ": " + map.get(key) + " \n";
        }

        result += ", SHA3Hash is " + Utils.sha3(result);
        return result;
    }

    @View
    public String hashSet() {
        String result = "";
        Set<String> strings = new HashSet<>();
        strings.add("123");
        strings.add("aaa");
        strings.add("bbb");
        for(String key : strings) {
            result += key + " \n";
        }
        try {
            result += strings.contains("aaa");
        } catch (Exception e) {
            e.printStackTrace();
        }
        result += ", SHA3Hash is " + Utils.sha3(result);

        return result;
    }

    @View
    public String list() {
        String result = "";
        List<String> strings = new ArrayList<>();
        strings.add("123");
        strings.add("aaa");
        strings.add("bbb");
        for(String key : strings) {
            result += key + " \n";
        }
        try {
            result += strings.contains("aaa");
        } catch (Exception e) {
            e.printStackTrace();
        }

        result += ", SHA3Hash is " + Utils.sha3(result);
        return result;
    }

    @View
    public String listAddress() {
        String result = "";
        List<Address> addressList = new ArrayList<>();
        addressList.add(new Address("Nse9XfuzZQn7jofrV5uABiNLQwFadV2K"));
        addressList.add(new Address("Nse5x9foSzFjuwkwZLSvSjAHHLVf3MKJ"));
        addressList.add(new Address("Nsdz8mKKFMehRDVRZFyXNuuenugUYM7M"));
        for(Address key : addressList) {
            result += key + " \n";
        }
        try {
            result += addressList.contains(new Address("Nse9XfuzZQn7jofrV5uABiNLQwFadV2K"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        result += ", SHA3Hash is " + Utils.sha3(result);
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
        result += ", SHA3Hash is " + Utils.sha3(result);
        return result + b;
    }

    @View
    public String qwe(String b, String a, String c) {
        String result = "";
        result += ", SHA3Hash is " + Utils.sha3(result);
        return result + b + a +c;
    }

}

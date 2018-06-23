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

package io.nuls.account.ledger.rpc;

import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TransferTest {

    public static List<String> getAddressList() {
        List<String> list = new ArrayList<>();
        list.add("2Cc2gqxcsJyM4tbJxqaHTj22HESAeXD");
        list.add("2CYfymtFuVCuYrQLzJ3gBvdZptvz4zj");
        list.add("2CZXYx5qszeCcqYXCwBbvkXZXiK1gk4");
        list.add("2CiHFDCksjv5aCJkz3fSLqtBLucjmMH");
        list.add("2CXRy7Utqpd1RNuhYEVkLYTcRv5JDLS");
        list.add("2CWdSPCo4t1PYHsRCMqs9Vfdy1hEYn5");
        list.add("2CXwBnL1TTqJ7KdjrEMvTsJoFQw6ui6");
        list.add("2CY6uMkQXDyzS8qYBBCwCRFNw2QMjLE");
        list.add("2CcLYrTCre75a2gzcToNYnYA8VgXmjX");
        list.add("2CZXSrhjHp9vKigCFv2SveS4bo1DHfc");
        list.add("2CdxbVs9j8DWKjEztvLafWo8myqqm1k");
        list.add("2CjQ8Mq6Es5gsDhkDbry4pwXXc5DcCW");
        list.add("2Cc8Q5rSFHcDSoQfUEqYcG5HSHRhfzm");
        list.add("2CfwtEqXuhLFWg2EtV6gxqdpmLpfoMJ");
        list.add("2CbJFyiDxkdmTVAtJFU5cqrSNcuw355");
        list.add("2CdKrRBh7bkTcj7TyLJpNS83zi1HKzr");
        list.add("2CirE7j1m2LxeweeH3EiZZZjHcGHCT4");
        list.add("2Cef5TNpVdzsAveDUXUZCT96KEvET5z");
        list.add("2CeH8wjuLk2fVdQD3Q4GyXXEEBxT3Sg");
        list.add("2CgA1Y795Yv8PSo7QhdSHUiXkGiofNs");
        list.add("2CaMJfVt7ouBfGkrAT3ud8pprUymUJb");
        list.add("2CXkbp52hTfx4YKFp2uuXUb2p6uSTc1");
        list.add("2CdD7nHtapywL8sdu43c72YpHaKHrKr");
        list.add("2CfvjcESYnkq8fZZFLtJXSjNa2ehUox");
        list.add("2Cb4oMyPSybkDyPXWsqnpGcoPoAZLmY");
        list.add("2CZ22WuJpByGXKE9Xhw3sXYFK45zg1X");
        list.add("2CbHbC2QEK87hddZAFcLXegwCg5Pq1J");
        list.add("2CWx9PZb4uVLAJX1xMjS4iAndYe3mcG");
        list.add("2Cbp16QYb7tzpHL9MvA1cQbPCkfSbP4");
        list.add("2CcD8oStg6cYCj9SWE9y39j2KNJoGr8");
        list.add("2CivRco72vWqHgPTn2meNuzScP2aQXL");
        list.add("2CiTdr35bjC35ERDFDM2tL6qBxKMAtz");
        list.add("2CV5QF5r5XDzj9WWdmHPJrc8xm5JYAB");
        list.add("2CW4fFepEVpp7DXy9G4jPFPvNMugL7Q");
        list.add("2CkBNLW8Y2X7vdX3hxFBHR3DqsFuWGU");
        list.add("2CfiBnL55wsphYjjPVJHqFiQH7M3oSQ");
        list.add("2CZuUGpZgvRzxzZ4TZNbrp9PrXs51Tn");
        list.add("2CiCmGsSsnfb8Kc8zHafmkG6588yx5u");
        list.add("2CYHLVWGLBxGkijXq2N8JnHRrTLysjQ");
        list.add("2CWWtH7uEA836Mvbd9VzMVZdRiRrdGy");
        list.add("2CeuVcXeibNjDjungWABCRzDa9qtcru");
        list.add("2Cg5j3A3ejCxYsmfUwXfVQePVhFhwHg");
        list.add("2CegCbx9jWCncSHK7M3Jeu4fWu5ZRs5");
        list.add("2CeasNhdfxvVkyCL7rKNK7rCFprUQmG");
        list.add("2Cay675epEHA8YFwYmPEgrQRAhMSj4h");
        list.add("2CiUZjo9yzgEDa81KSZaiGHqfD5UmLt");
        list.add("2CheTVzJ2L37h1w8MRvzafostSzJGsT");
        list.add("2CfkLhjEXkPGQtm3x6oU1wEnjpHYkPu");
        list.add("2Ch5ai5evu94E6Ew2SBiQWaYXAhuMUz");
        list.add("2CaCfVbPGhd2VAiiXTpLxUfJPajrxci");
        list.add("2ChgP29U9SKQFezCen4LELXery6bz1Y");
        list.add("2CkJPfPa2gogLTK5acgrRzEkPHjrDXG");
        list.add("2CZokSNjXUmTKY4yzvtxTQvSBM7iJFh");
        list.add("2CjgMy8zTJUzCWFPDgQR61xfDWBuZLF");
        list.add("2CcTfQgAbMJ6piTnYogNQCC9K1C6mgs");
        list.add("2CjEStoSAngTSdeR7VYbYy9Gj2eTQ9i");
        list.add("2CiB66gXTunGfpvApQavJBKM8oNWo48");
        list.add("2CfjoHuqH9SoUZRW7m19HLFjppaeuX4");
        list.add("2CWDPkrGKvqDY9GtW4GrSH5j8TAg5bB");
        list.add("2CcFe6QLTHLzQvYK1mKi3DqGmyjPXro");
        list.add("2CZ9ekWTuVY41ycJJdn2LDqdTpzNG6r");
        list.add("2Cdieo8MQJL7KMQ51hoSynDJfSR2tk5");
        list.add("2CWVZ9m1kbFKvcB2UwgQGJTGpWTGjVp");
        list.add("2CjLaiMjPmstrAZiV4WnxnPCt7UYAUy");
        list.add("2CdA5WvEmragTZKzmH8QW7WBNvKFis3");
        list.add("2Cg9wn7nuUH9WbKoKfTDsXvWsPHfhBu");
        list.add("2CXRbbbRumpyLbrC8SX96ugqK1WygFr");
        list.add("2CbM3M1KeA3nJHwrNJxvDjf1N3V6Mpm");
        list.add("2CYKSuDFiNrA387LAF526eUeUKw67vC");
        list.add("2CVAxGcxCZv6rwq2v7vUPpe931V3muf");
        list.add("2Cj61FNPY9B8wex1kW95RgApMS4MmTg");
        list.add("2CdPSMVbEnSHErVAxfstxmQctBHDaaQ");
        list.add("2Cc5u8feD9ifQErApAvj7aLaUeg4EQm");
        list.add("2CeEMobjcDf3irnapm2xqdSzPQg724z");
        list.add("2CcFx5iv17Tgymp33HP5fdZeMgL5nDD");
        list.add("2CfzQntRknL5brgKDnpiz1Ayp8zsLYC");
        list.add("2Cbr9JrJvCKZPFo2XPvoDa1aTm6pK81");
        list.add("2ChczYCL6LW3cRj8PtCgmJVoXCrzRJP");
        list.add("2Ce1EyCijdkSKHPSMRu7WXiw8E3Yuhp");
        list.add("2CeXu6QPpQYe9RTZSkkucVFCtMwNRss");
        list.add("2Cba9QaakwYX6eHtQVZgmobBQ7fuzpi");
        list.add("2CZJNsbzzBHsmHvy6MEgxJM4MLoATve");
        list.add("2Ca4f5TN2ZTZY1gWgnmcgwMqXcdgMb3");
        list.add("2Ci6FJsZVzjqN8hyiWpVLh3MbpMfi57");
        list.add("2Ce29AuYXgR1c7UALYxRAp8ZXLJuHU5");
        list.add("2CXzL7yuWFrGD3aJae9tGEE5Hgzy66C");
        list.add("2CfMavgXm2zumM7Jf9oNVdZT1erzvHY");
        list.add("2Cczq6oKSVcaFXkLzds2RM7cBNb5nW9");
        list.add("2CWi6SL7QZMuPEEsRpXrWLirsAbR4aC");
        list.add("2CkSBB7ns5Q3yFUUPfkoWcnAUwcDVwv");
        list.add("2CZPgN5VU81WbhZJrCBXtgT3PXMkUAi");
        list.add("2Cdt9yhbXpCDLuU2gQaQH5M5b3umM3a");
        list.add("2CjuDKiWy9QpaT4VLGY8rGAAGSuwpfv");
        list.add("2CYcaZYkgcpMKHX7TtLjpkyRotbs55R");
        list.add("2Cext6pmhnHoN41Hf3XuZQxmt1cPW8B");
        list.add("2CVyo48t6bqtktuTXYLG41vbn2ZdhbR");
        list.add("2CbzPf8PkZkTTrViAqXp598kfBt24id");
        list.add("2CaCVQjNQFuJCofmcqyPK6GnWJwjimK");
        list.add("2CgTcje5EePMoHsLkTsfVG3uy2hZ61v");
        list.add("2Cc4fBADknrbKLLK7Qkg35W6GH3ur7E");
        return list;
    }

   private static int successCount = 0;
    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            doit();
        }
    }

    private static void doit() {
        List<String> addressList = getAddressList();

        for (String toAddress : addressList) {
            String address = "2CiB4JFCXiPZpUm9JcyQ1nVexy2qx19";
//            String toAddress = "2Cg7BLHWBSxMhq3FpjR9BrkyxXp4m4j";
            long amount = 201800L;
            String password = "";
            String remark = "test";

            String param = "{\"address\": \"" + address + "\", \"toAddress\": \"" + toAddress + "\", \"password\": \"" + password + "\", \"amount\": \"" + amount + "\", \"remark\": \"" + remark + "\"}";

            String url = "http://127.0.0.1:8001/api/accountledger/transfer";


            for (int i = 0; i < 100; i++) {
                String res = post(url, param, "utf-8");
                if (res.indexOf("true") != -1) {
                    successCount++;
                }
                System.out.println(successCount + "  " + res);
            }
        }

    }

    public static String post(String url, final String param, String encoding) {
        StringBuffer sb = new StringBuffer();
        OutputStream os = null;
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        // 默认编码UTF-8
        if (StringUtils.isNull(encoding)) {
            encoding = "UTF-8";
        }
        try {
            URL u = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");

            connection.connect();

            os = connection.getOutputStream();
            os.write(param.getBytes(encoding));
            os.flush();
            is = connection.getInputStream();
            isr = new InputStreamReader(is, encoding);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (Exception ex) {
            System.err.println(ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
        }
        return sb.toString();
    }
}

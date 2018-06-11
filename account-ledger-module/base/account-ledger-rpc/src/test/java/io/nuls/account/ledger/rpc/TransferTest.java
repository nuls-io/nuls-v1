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
        list.add("2CctQ8XM39gQ2mouopKsDCHn88c7tpD");
        list.add("2CjCJDAz8nF35jJnSoxDJywsTrEjJH4");
        list.add("2CeQMfJiqUBggBxZ11kHhpphDVmYN1Q");
        list.add("2Ceiom4NiGSvJRPa1p5i7knt5p7LZCW");
        list.add("2Ck8YZw9ACiVrt332zx5ajqZnySyYJd");
        list.add("2CizEBDY1vj2XeZBLrfrBEAeis8To7K");
        list.add("2CYZyS5cW6mJ6v4683tYPpSR1u6uo4u");
        list.add("2CYfVGi3DBU5xkVLX2N8kPzz5Qra1fh");
        list.add("2CVYTtK3Vxp9USnspXPct51XVe5v3rt");
        list.add("2Cfh9o6QNvDyWg6z3BuxYQCFfwrcNCs");
        list.add("2CZeUHESMsDsLmrzguhWYJ4jPo9BC6Z");
        list.add("2Cc7s5Yi955VQUkShW6r5ShBaeNpCLJ");
        list.add("2Cap3NibYDo2gKouRxQDjPYQ7jAcxzL");
        list.add("2CbNQHajzyRLEUQo6fPemLuvBTH8KQJ");
        list.add("2CbTHr9xAfpiCefHg4acq6KHzBkQnNb");
        list.add("2Cj11bw7UXmCuzJsmVmEzEPkghUARKw");
        list.add("2CW5u9wQ2rebiKxgC76KjkADFkauRoQ");
        list.add("2Cbw4NuMcFcZk5XDFApAZoX8WHyCsrA");
        list.add("2CjCiRYaVmUvUzhWqPfXC5dea4r42QX");
        list.add("2CZrHnYB9zz4p9duv7NwW9eX4hhmBGC");
        list.add("2Cisr69bruAJHJAhDrtCseRbFPEEjpx");
        list.add("2CcK1hGa49qviuKj2MqkDFQ9LNzj2UJ");
        list.add("2CdrLKXPiFfrQeiNDeSSzmmzkCTibxS");
        list.add("2CaJ4zpr5XZhRPGiokxjWia85R5JEFG");
        list.add("2CXEiWcAD4N4DP4X4rFwEKhVdSKTcPa");
        list.add("2CbN8EscBqpaVtZQSCErPrmn4oMonRY");
        list.add("2CZjofq4VwdW45qhjrVi4RQjwqdehbT");
        list.add("2CV91Vy7wX2os1KxndnZPJRcf89yvRJ");
        list.add("2CXD96kvzEUkLp6RxnPXnBmKDxzMBxV");
        list.add("2CYvtBo2VioGCJW9hoRSnLaZwuTL3Tv");
        list.add("2CWihk3mfvA1bDtCQQmBSGwTBdMNxXY");
        list.add("2CiejAG8pffMGCSUdgcZ4m9aATJnyYi");
        list.add("2Cd89a3XMqZRRDfNfsyfkJnoF8NnKbu");
        list.add("2CfZhnjp8Y7iHFMNr6cnzwAf2NL22hc");
        list.add("2Cf7y6x2dFUKHFnATWCGogV4zuqSLxC");
        list.add("2CggHBMeX7RFxk9BAHo5LXhw6mvN6ZR");
        list.add("2Cdwm6ms7qaTFwHMqhmwpWWoWqFpJMN");
        list.add("2CWUB7ogF8dUi4Crs1CEn5GpsYGk5rt");
        list.add("2CkeT7Lbyf8PUjfhHGaBLV3zBN1hFtt");
        list.add("2CbqaF5i9vjN4dXvcsT8XNf7hVnzNaF");
        list.add("2CYvZK56SSvabxFtjMtuBbk8fPRR9BM");
        list.add("2CfyeJcdaz5TS42UVm6ibiQqENeSt4m");
        list.add("2CZEMFWYnBzsEJcaxA2KqUndNB1MkgB");
        list.add("2ChsRGhX7bLr1LXsexxJzQfjdvNG5Xi");
        list.add("2Cb1y1KHeY3rczHcsTiitwWQMMm4UKt");
        list.add("2CkHq3Nky2Suc97UBSDzeLCXDush1ms");
        list.add("2CkdfDmShKtuRZSGUFkzGL8Mo1cGvmw");
        list.add("2CjjT4JZAfbKqx3Lkpw64zyAopeokCA");
        list.add("2CWFg9PFeHifDY8sN99MpTvr7VuaKqe");
        list.add("2CkdUc4415VpdUVoYc9WEuMdamahx7e");
        list.add("2CgZq27V4puG3U2ENizLPWZSon8e9it");
        list.add("2CavN7G9nf5eQEsSUjpJoK2dimhQGVf");
        list.add("2Cju4HED8MELoWURunAQDEaj4JXL2q7");
        list.add("2CfDSyo7AjMsYvvyUA9e6gDbgWoVCBD");
        list.add("2Ck2NvDkmbiHrvE2vR1DPJgEbjNUvED");
        list.add("2CgTF6m9MasTdJomoBwfdDuHxYtpaji");
        list.add("2CdxCYysDSWdbF1bx6toMzZoaNnWS9m");
        list.add("2CjFiBTF2V9GEXQz5gv9s4XFRQJqJHe");
        list.add("2CecqMkHSWod6KY8aoF4xAvBvfF4hwQ");
        list.add("2CdouMobict2bdfzyuu3GFnUsrMipuu");
        list.add("2CcqiwTJsHKXeY6KFutjytGyabcnvuQ");
        list.add("2Cjw82SnoAeq8Qk7grsVMZuTS8AdRrz");
        list.add("2ChXBKPhGdcwjGfVpUKjYWYUFDGbeHW");
        list.add("2Cjw3qxMqHMraDLKAP3YCVGPtPU2wUa");
        list.add("2CdTRMAg1frtdFKUc1U3N6ipUwJgue1");
        list.add("2Ca9C75PvPQkZbPEdZnfjp8XR1HF6E2");
        list.add("2CXhT42q9oibS2cw5iSsotWaXhbXHdp");
        list.add("2CiLCk6fAWmmY9bg4XdF8MAJMGPuPjY");
        list.add("2CcukhJCGjjs4sMuHwzHh3AkkEcss2c");
        list.add("2CjQMfd37t3qFjowapHSmcTUk5rxrSi");
        list.add("2CYhPTiLCHpHjyFYZ9wrf2HwgQ98bUT");
        list.add("2Cgw5Xs5Z9SFQo4Pxn5aj1GVXaKrHFT");
        list.add("2CYwCqbtoqgPS9Pn93qZdn3AgSkeERe");
        list.add("2Ch5eeyrB2hbqRWhmN849f5iVuEphbm");
        list.add("2CVXTDwmGTLn1v6t6cEPCm2Tp8dYW62");
        list.add("2CjSd9BEy5bjkgZCP8aiTBXLc7Bkyi7");
        list.add("2CjaV2X8kMXMq5CHQraMSmJ8x6b6hXY");
        list.add("2CUzZHQCGE7aiPCaD1n54xJkC9BrdKh");
        list.add("2CfAry1qVbtwtGucSDJTHfr35EpmUuy");
        list.add("2CgbPidjuNuPPDU4ZoALTCEegRHFG84");
        list.add("2Cc12jfe6gRNGSbFdUCjaJassgpPuwJ");
        list.add("2CatCdtqaeBfADyMGKkbb2hr3HpfFtY");
        list.add("2ChmSpB1gjppQPfkq44T5QsefUDMJSz");
        list.add("2CfquoNg9nfDh74GcioDXsvPbtTjCLv");
        list.add("2CX7j1Zrx7htWgXenD9k3EakNyMHEuD");
        list.add("2CfYshHRZSHuK7hypmHPL2qKpJ2ZaKX");
        list.add("2CZRpRPsP2cWY1CLjBgjAQqneD8W8Rf");
        list.add("2CZnsiR2q7yZw2dYVmktwMZaVW5E6qk");
        list.add("2Cc54TepK6JvuXx9McyFxsdULMbLrW1");
        list.add("2Chni3ha4J7kP6AX9x73r88r4SGwXnZ");
        list.add("2CYtxJPuzybSfbh1rvnKWex5CGJ4u7v");
        list.add("2Ca7f7aB9AVqsSTUWannPcgoTjA1ZpB");
        list.add("2CaVYGJeWSLEgbVPCEYBjroyv61HcCw");
        list.add("2CXUy4fDcC3mrM3u2fvHVQraCcL62BQ");
        list.add("2CZ4vSNrkJynMrT5ccejVEgv9o1njoN");
        list.add("2CXyVt1FXCZMeYHFNMFmjgS85vsZzRf");
        list.add("2CbhPAK5CJBK3YT1fTeJQATr9d8mAeZ");
        list.add("2CVsx51dNjfwKfpdFL44kJsAGTwEqki");
        list.add("2CZ8yRoVjzzU1ntEVDmXJhLD2dRmNMF");
        list.add("2CgBHyMDaK1pKW1AXVbA3cwvL21kaWt");
        return list;
    }


    public static void main(String[] args) {
        List<String> addressList = getAddressList();
        for (String toAddress : addressList) {
            String address = "2CXJEuoXZMajeTEgL6TgiSxTRRMwiMM";
//            String toAddress = "2Cg7BLHWBSxMhq3FpjR9BrkyxXp4m4j";
            long amount = 2000200000000L;
            String password = "";
            String remark = "test";

            String param = "{\"address\": \"" + address + "\", \"toAddress\": \"" + toAddress + "\", \"password\": \"" + password + "\", \"amount\": \"" + amount + "\", \"remark\": \"" + remark + "\"}";

            String url = "http://127.0.0.1:8001/api/accountledger/transfer";

            int successCount = 0;

            for (int i = 0; i < 1; i++) {
                String res = post(url, param, "utf-8");

                if (res.indexOf("true") != -1) {
                    successCount++;
                }
                System.out.println(successCount + "  " + res);

//            try {
//                Thread.sleep(100L);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
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

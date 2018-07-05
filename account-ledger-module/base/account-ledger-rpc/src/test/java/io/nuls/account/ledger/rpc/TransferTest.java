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
        list.add("Nse1V1YQJ4CFoNAdHGw2BajsAwocMTX5");
        list.add("NsdzuhCP6vE7HV985GJBw1LGPPQWyN4B");
        list.add("Nse3dZ84MUttFXu2XCf9ViGSRdb7VLvf");
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
            String address = "Nse9XfuzZQn7jofrV5uABiNLQwFadV2K";
//            String toAddress = "2Cg7BLHWBSxMhq3FpjR9BrkyxXp4m4j";
            long amount = 2018000L;
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

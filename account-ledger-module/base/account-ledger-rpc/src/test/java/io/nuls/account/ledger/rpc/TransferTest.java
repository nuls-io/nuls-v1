package io.nuls.account.ledger.rpc;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.util.JSONPObject;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import org.glassfish.jersey.server.JSONP;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class TransferTest {

    public static void main(String[] args) {

        String address = "2CYqLxMgdSK5XaK9yBv43g6iEtpiAKJ";
        String toAddress = "2Cjsa6wpKyowbVjM9oBrSeo7muEuu4w";
        long amount = 100000000;
        String password = "nuls123456";
        String remark = "test";

        String param = "{\"address\": \"" + address + "\", \"toAddress\": \"" + toAddress + "\", \"password\": \"" + password + "\", \"amount\": \"" + amount + "\", \"remark\": \"" + remark + "\"}";

        String url = "http://127.0.0.1:8001/accountledger/transfer";

        int successCount = 0;

        for(int i = 0 ; i < 10000 ; i ++) {
            String res = post(url, param, "utf-8");

            if(res.indexOf("true") != -1) {
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

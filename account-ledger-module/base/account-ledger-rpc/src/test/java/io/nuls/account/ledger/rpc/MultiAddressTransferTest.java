package io.nuls.account.ledger.rpc;

import io.nuls.account.ledger.BaseTest;
import io.nuls.core.tools.json.JSONUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiAddressTransferTest extends BaseTest {


    static long t,t1,t2,t3,t4;

    public static void main(String[] args) {
        MultiAddressTransferTest test = new MultiAddressTransferTest();
        List<Map> list = test.genOrLoadAddress();

        System.out.println(list.size());

        long time = System.currentTimeMillis();

        for(int i = 0 ; i < 10 ; i ++) {
            for (Map info : list) {
                Map map = test.sendOfflineTx(info, "Nse9Jxd1VdLWEoZxe3fWkXxus8TKgJyd");
                if (map == null || !(boolean) map.get("success")) {
                    System.err.println("失败3：" + map);
                    continue;
                }
                info.put("value", info.get("balance"));
                info.put("txHash", ((Map) map.get("data")).get("value"));
                info.put("index", 1);
            }
        }

        System.out.println("结果：");
        System.out.println("总耗时：" + (System.currentTimeMillis() - time));
        System.out.println("t1 ：" + t1 / 1000000);
        System.out.println("t2 ：" + t2 / 1000000);
        System.out.println("t3 ：" + t3 / 1000000);

        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("./address.txt"));
            oos.writeObject(list);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map sendOfflineTx(Map info, String toAddress) {

        t = System.nanoTime();
        Map map = createTx(info, toAddress);
        t1 += (System.nanoTime() - t);
        t = System.nanoTime();

        if(map == null || !(boolean) map.get("success")) {
            System.err.println("失败1：" + map);
            return map;
        }
        map = singTx(info, map);
        t2 += (System.nanoTime() - t);
        t = System.nanoTime();

        if(map == null || !(boolean) map.get("success")) {
            System.err.println("失败2：" + map);
            return map;
        }
        map = broadcastTx(map);

        t3 += (System.nanoTime() - t);

        return map;
    }

    private Map broadcastTx(Map map) {
        String hex = (String)((Map)map.get("data")).get("value");

        String param = "{\"txHex\":\"" + hex + "\"}";

        String url = "http://127.0.0.1:8001/api/accountledger/transaction/broadcast";
        String res = post(url, param, "utf-8");
        try {
            Map result = JSONUtils.json2map(res);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map singTx(Map info, Map map) {

        String hex = (String)((Map)map.get("data")).get("value");
        String privateKey = (String) info.get("priKey");

        String param = "{\"txHex\":\"" + hex + "\", \"address\": \""+ info.get("address") + "\",\"priKey\": \"" + privateKey + "\", \"password\": \"\"}";

        String url = "http://127.0.0.1:8001/api/accountledger/transaction/sign";
        String res = post(url, param, "utf-8");
        try {
            Map result = JSONUtils.json2map(res);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map createTx(Map info, String toAddress) {
        String address = (String) info.get("address");
        long value = (long) info.get("value");
        long balance = (value - 1100000L);

        String param = "{\"inputs\": [{\"fromHash\":\"" + info.get("txHash") + "\", \"fromIndex\": "+ info.get("index") + ",\"address\": \"" + address + "\", \"value\": " + value + ", \"lockTime\":0}], \"outputs\": [{\"address\":\"" + toAddress + "\", \"value\":1000000,\"lockTime\": 0},{\"address\":\""+ address +"\", \"value\":" + balance + ",\"lockTime\": 0}],\"remark\":\"\"}";
        String url = "http://127.0.0.1:8001/api/accountledger/transaction";
        String res = post(url, param, "utf-8");
        try {
            info.put("balance", balance);
            Map result = JSONUtils.json2map(res);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map send(String fromAddress, String toAddress, long amount, String password, String remark) {
        String param = "{\"address\": \"" + fromAddress + "\", \"toAddress\": \"" + toAddress + "\", \"password\": \"" + password + "\", \"amount\": \"" + amount + "\", \"remark\": \"" + remark + "\"}";
        String url = "http://127.0.0.1:8001/api/accountledger/transfer";
        String res = post(url, param, "utf-8");
        try {
            return JSONUtils.json2map(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Map> genOrLoadAddress() {
        List<Map> addressList = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("./address.txt"));
            try {
                addressList = (List<Map>) ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            ois.close();
        } catch (FileNotFoundException fe) {

            addressList = new ArrayList<>();

            String param = "{\"count\": 100, \"password\": \"\"}";
            String url = "http://127.0.0.1:8001/api/account/offline";

            int count = 0;
            long amount = 1000000000L;
            for (int i = 0; i < 100; i++) {
                String res = post(url, param, "utf-8");

                try {
                    Map map = JSONUtils.json2map(res);

                    List<Map> list = ((List<Map>) ((Map) map.get("data")).get("list"));

                    for(Map m : list) {
                        count++;
                        Map result = send("Nse3nddic6tgPUpV3UwhenT4fKFPTHMA", (String) m.get("address"), amount, "", "");
                        System.out.println("第 " + count + " 条发送结果：" + result);
                        String txHash = (String)((Map)result.get("data")).get("value");
                        m.put("txHash", txHash);
                        m.put("index", 0);
                        m.put("value", amount);
                        Thread.sleep(10L);
                    }
                    addressList.addAll(list);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("./address.txt"));
                oos.writeObject(addressList);
                oos.flush();
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return addressList;
    }
}

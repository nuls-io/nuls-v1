package io.nuls.transfer;

import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/7/8
 */
public class TestMain {

    public static RestFulUtils restFul;

    public static void main(String[] args) {
        RestFulUtils.getInstance().setServerUri("http://192.168.1.106:8001/api");
        restFul = RestFulUtils.getInstance();
        RpcClientResult result = restFul.get("/contract/result/0020ce2b820d15ecbe1c8c22526611336fd425522340c6246f0364dd1e784da8ed0b", null);
        if (result.isFailed()) {
            System.out.println("query fail");
        }
        Map<String, Object> map = ((Map) result.getData());
        System.out.println(map);
    }

    static void main0() {
        RestFulUtils.getInstance().setServerUri("http://127.0.0.1:8001/api");
        restFul = RestFulUtils.getInstance();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("password", null);
        parameters.put("count", 100);
        RpcClientResult result = restFul.post("/account", parameters);
        if (result.isFailed()) {
            System.out.println("create fail");
        }
        Map<String, Object> map = ((Map) result.getData());
        List<String> list = (List<String>) map.get("list");
        TransferTest transferTest = new TransferTest("Nse4R5LhHSpRQqqocDmBnavi3B2HysCM", list);
        for (int i = 0; i < 1; i++){
            Thread thread = new Thread(transferTest,"Charlie-" + i);
            thread.start();
        }
    }
}

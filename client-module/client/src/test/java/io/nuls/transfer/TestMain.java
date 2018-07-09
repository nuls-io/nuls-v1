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

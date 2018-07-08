package io.nuls.transfer;

import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author: Charlie
 * @date: 2018/7/8
 */
public class TransferTest implements Runnable {
    private String from;
    private List<String> to;
    private RestFulUtils restFul = TestMain.restFul;

    public TransferTest(String from,List<String> to){
        this.from = from;
        this.to = to;
    }

    @Override
    public void run() {
        int count = 0;
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (String toAdd : to) {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("address", from);
                parameters.put("toAddress", toAdd);
                parameters.put("password", null);
                parameters.put("amount", 300000000);
                parameters.put("remark", Thread.currentThread().getName());
                RpcClientResult result = restFul.post("/accountledger/transfer", parameters);
                if (result.isFailed()) {
                    System.out.println(Thread.currentThread().getName() + " - transfer fail : " + (++count));
                } else {
                    System.out.println(Thread.currentThread().getName() + " - transfer success : " + (++count));
                }
            }
        }
    }
}

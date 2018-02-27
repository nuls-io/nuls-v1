import io.nuls.consensus.service.intf.ConsensusService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Niels Wang
 * @date: 2018/2/27
 */
public class ConsensusServiceExample {

    public void joinConsensus(){
        ConsensusService service = null;
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("deposit",2000);
        paramsMap.put("agentAddress","asdflauegjlasjdflasjg");
        service.startConsensus("address",null,paramsMap);
    }

    public void createDelegate(){
        ConsensusService service = null;
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("deposit",20000);//保证金
        paramsMap.put("agentAddress","asdflauegjlasjdflasjg");//出块地址
        paramsMap.put("introduction","永不掉线，性能超级好");//代理说明
        paramsMap.put("commissionRate","12");//代理挖矿佣金比例
        service.startConsensus("address",null,paramsMap);
    }
}

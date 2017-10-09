package io.nuls.global;

import io.nuls.mq.intf.QueueService;
import io.nuls.task.ModuleManager;

import java.util.HashMap;
import java.util.Map;

public class NulsContext {

    private NulsContext() {
        // single
    }

    private static final NulsContext nc = new NulsContext();
    private static final Map<String, Object> intfMap = new HashMap<>();
    private static ModuleManager moduleManager;



    public static final NulsContext getInstance() {
        return nc;
    }

    public <T> T getService(String serviceName, Class<T> tclass) {
        return (T) intfMap.get(serviceName);
    }

    public void putService(String serviceName,Object service){
        if(intfMap.keySet().contains(serviceName)){

        }
    }


    //    @Autowired
    private QueueService queueService;

    /**
     * get The Queue intf instance
     *
     * @return
     */
    public QueueService getQueueService() {
        return queueService;
    }

}

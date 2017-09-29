package io.nuls.global;

import io.nuls.mq.intf.QueueService;
import io.nuls.rpcserver.intf.RpcServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class NulsContext {

    private static ApplicationContext applicationContext;

//    @Autowired
    private QueueService queueService;

    /**
     * get The Queue intf instance
     * @return
     */
    public QueueService getQueueService() {
        return queueService;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        NulsContext.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {

        return applicationContext;
    }
}

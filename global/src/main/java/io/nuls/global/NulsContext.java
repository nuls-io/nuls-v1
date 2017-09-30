package io.nuls.global;

import io.nuls.mq.intf.QueueService;
import io.nuls.rpcserver.intf.RpcServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class NulsContext {

    private static ClassPathXmlApplicationContext applicationContext;

//    @Autowired
    private QueueService queueService;

    /**
     * get The Queue intf instance
     * @return
     */
    public QueueService getQueueService() {
        return queueService;
    }

    public static void setApplicationContext(ClassPathXmlApplicationContext applicationContext) {
        NulsContext.applicationContext = applicationContext;
    }

    public static ClassPathXmlApplicationContext getApplicationContext() {

        return applicationContext;
    }
}

package io.nuls;

import io.nuls.mq.intf.QueueService;
import io.nuls.rpcserver.intf.RpcServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InchainContext {

    @Autowired
    private QueueService queueService;

    /**
     * get The Queue service instance
     * @return
     */
    public QueueService getQueueService() {
        return queueService;
    }
}

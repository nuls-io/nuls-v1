package io.nuls.mq.module.impl;


import io.nuls.core.thread.manager.NulsThreadFactory;
import io.nuls.core.thread.manager.ThreadManager;
import io.nuls.mq.MqConstant;
import io.nuls.mq.entity.StatInfo;
import io.nuls.mq.manager.QueueManager;
import io.nuls.mq.module.AbstractMQModule;
import io.nuls.mq.service.impl.QueueServiceImpl;
import io.nuls.mq.thread.StatusLogThread;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Niels
 * @date 2017/9/27
 */
public class MQModuleBootstrap extends AbstractMQModule {

    public MQModuleBootstrap() {
        super();
    }

    @Override
    public void init() {
        // todo auto-generated method stub(niels)
    }

    @Override
    public void start() {
        ScheduledExecutorService service =ThreadManager.createScheduledThreadPool(new NulsThreadFactory(this.getModuleId(),"queueStatusLogPool"));
        service.scheduleAtFixedRate(new StatusLogThread(), 0, QueueManager.getLatelySecond(), TimeUnit.SECONDS);
        this.registerService(service);
        this.registerService(QueueServiceImpl.getInstance());
        QueueManager.setRunning(true);
    }

    @Override
    public void shutdown() {
        QueueManager.setRunning(false);
    }

    @Override
    public void destroy() {
        shutdown();
    }

    @Override
    public String getInfo() {
        StringBuilder str = new StringBuilder();
        str.append("moduleName:");
        str.append(getModuleName());
        str.append(",moduleStatus:");
        str.append(getStatus());
        str.append(",ThreadCount:");
//        List<BaseNulsThread> threadList = this.getThreadList();
//        str.append(threadList.size());
//        str.append("\nThreadInfo:\n");
//        for (BaseNulsThread t : threadList) {
//            str.append(t.getInfo());
//        }
        str.append("QueueInfo:\n");
        List<StatInfo> list = QueueManager.getAllStatInfo();
        for (StatInfo si : list) {
            str.append(si.toString());
        }
        return str.toString();
    }

    @Override
    public int getVersion() {
        return MqConstant.MQ_MODULE_VERSION;
    }
}

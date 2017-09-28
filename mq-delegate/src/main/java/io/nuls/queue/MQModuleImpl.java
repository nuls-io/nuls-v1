package io.nuls.queue;

import io.nuls.mq.MQModule;
import io.nuls.mq.intf.StatInfo;
import io.nuls.queue.impl.manager.QueueManager;
import io.nuls.task.InchainThread;
import io.nuls.task.ModuleManager;
import io.nuls.task.ModuleStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Niels on 2017/9/27.
 * nuls.io
 */
@Service("mqModule")
public class MQModuleImpl extends MQModule {

    private ScheduledExecutorService service;

    @Autowired
    private ModuleManager moduleManager;

    @Override
    public void start() {
        this.setStatus(ModuleStatus.STARTING);
        InchainThread t1 = new InchainThread(this, "queueStatusLogThread") {
            @Override
            public void run() {
                QueueManager.logQueueStatus();
            }
        };
        //启动速度统计任务
        service = new ScheduledThreadPoolExecutor(1);
        service.scheduleAtFixedRate(t1, 0, QueueManager.getLatelySecond(), TimeUnit.SECONDS);
        QueueManager.setRunning(true);
        this.setStatus(ModuleStatus.RUNNING);
    }

    @Override
    public void shutdown() {
        this.setStatus(ModuleStatus.STOPPING);
        QueueManager.setRunning(false);
        service.shutdown();
        this.setStatus(ModuleStatus.STOPED);
    }

    @Override
    public String getInfo() {
        StringBuilder str = new StringBuilder();
        str.append("moduleName:");
        str.append(getModuleName());
        str.append(",moduleStatus:");
        str.append(getStatus());
        str.append(",ThreadCount:");
        Map<String, InchainThread> threadMap = moduleManager.getThreadsByModule(getModuleName());
        str.append(threadMap.size());
        str.append("ThreadInfo:\n");
        for (InchainThread t : threadMap.values()) {
            str.append(t.getInfo());
        }
        str.append("QueueInfo:\n");
        List<StatInfo> list = QueueManager.getAllStatInfo();
        for(StatInfo si :list){
            str.append(si.toString());
        }
        return str.toString();
    }
}

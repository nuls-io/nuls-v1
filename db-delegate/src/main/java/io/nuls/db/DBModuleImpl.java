package io.nuls.db;

import io.nuls.global.NulsContext;
import io.nuls.task.ModuleManager;
import io.nuls.task.ModuleStatus;
import io.nuls.task.NulsThread;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by zoro on 2017/9/27.
 * nuls.io
 */
public class DBModuleImpl extends DBModule {

    private ScheduledExecutorService service;

    @Override
    public void init(Map<String, String> initParams) {

    }

    @Override
    public void start() {
        this.setStatus(ModuleStatus.STARTING);
        NulsThread t1 = new NulsThread(this, "queueStatusLogThread") {
            @Override
            public void run() {
            }
        };
        //启动速度统计任务
        service = new ScheduledThreadPoolExecutor(1);
//        service.scheduleAtFixedRate(t1, 0, QueueManager.getLatelySecond(), TimeUnit.SECONDS);
//        QueueManager.setRunning(true);
        this.setStatus(ModuleStatus.RUNNING);
    }

    @Override
    public void shutdown() {
        this.setStatus(ModuleStatus.STOPPING);
//        QueueManager.setRunning(false);
        service.shutdown();
        this.setStatus(ModuleStatus.STOPED);
    }

    @Override
    public void desdroy(){
        shutdown();
        NulsContext.getInstance().remService(service);
        NulsContext.getInstance().getModuleManager().remModule(this.getModuleName());
        setStatus(ModuleStatus.UNINITED);
    }

    @Override
    public String getInfo() {
        StringBuilder str = new StringBuilder();
        str.append("moduleName:");
        str.append(getModuleName());
        str.append(",moduleStatus:");
        str.append(getStatus());
        str.append(",ThreadCount:");
        Map<String, NulsThread> threadMap = NulsContext.getInstance().getModuleManager().getThreadsByModule(getModuleName());
        str.append(threadMap.size());
        str.append("ThreadInfo:\n");
        for (NulsThread t : threadMap.values()) {
            str.append(t.getInfo());
        }
//        str.append("QueueInfo:\n");
//        List<StatInfo> list = QueueManager.getAllStatInfo();
//        for(StatInfo si :list){
//            str.append(si.toString());
//        }
        return str.toString();
    }


    private boolean hasType(String dataBaseType) {
        switch (dataBaseType) {
            case "h2":
                return true;

        }
        return false;
    }

}

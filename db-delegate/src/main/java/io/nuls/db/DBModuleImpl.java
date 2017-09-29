package io.nuls.db;

import io.nuls.db.entity.Block;
import io.nuls.db.intf.IBlockStore;
import io.nuls.global.NulsContext;
import io.nuls.task.ModuleManager;
import io.nuls.task.ModuleStatus;
import io.nuls.task.NulsThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by zoro on 2017/9/27.
 * nuls.io
 */
@Service("dbModule")
public class DBModuleImpl extends DBModule {

    private ScheduledExecutorService service;

    @Autowired
    private ModuleManager moduleManager;

    @Override
    public void init(Map<String, String> initParams) {
        String databaseType = null;
        if(initParams.get("databaseType") != null) {
            databaseType = initParams.get("databaseType");
        }
        if (!StringUtils.isEmpty(databaseType) && hasType(databaseType)) {
            String path = "classpath:/database-" + databaseType + ".xml";
            NulsContext.setApplicationContext(new ClassPathXmlApplicationContext(new String[]{path}, true, NulsContext.getApplicationContext()));
        }
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
    public String getInfo() {
        StringBuilder str = new StringBuilder();
        str.append("moduleName:");
        str.append(getModuleName());
        str.append(",moduleStatus:");
        str.append(getStatus());
        str.append(",ThreadCount:");
        Map<String, NulsThread> threadMap = moduleManager.getThreadsByModule(getModuleName());
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

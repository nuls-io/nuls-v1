package io.nuls.db;

import io.nuls.db.intf.IBlockStore;
import io.nuls.global.NulsContext;
import io.nuls.task.ModuleManager;
import io.nuls.task.ModuleStatus;
import io.nuls.task.NulsThread;
import io.nuls.util.constant.ErrorCode;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by zoro on 2017/9/27.
 * nuls.io
 */
public class DBModuleImpl extends DBModule {

    private ScheduledExecutorService dbExecutor;

    private SqlSessionFactory sqlSessionFactory;

    private IBlockStore iBlockStore;

    @Override
    public void start(){
        NulsThread t1 = new NulsThread(this, "queueStatusLogThread") {
            @Override
            public void run() {
            }
        };
        //启动速度统计任务
        dbExecutor = new ScheduledThreadPoolExecutor(1);
        this.registerService(dbExecutor);
//        service.scheduleAtFixedRate(t1, 0, QueueManager.getLatelySecond(), TimeUnit.SECONDS);
//        QueueManager.setRunning(true);
        try {
            initSqlSessionFactory();
        }catch (Exception e) {
            throw new DBException(ErrorCode.DB_SQLSESSION_INIT_FAIL);
        }

    }


    private void initSqlSessionFactory()throws IOException{
        String resource = "mybatis/mybatis-config.xml";
        InputStream in = Resources.getResourceAsStream(resource);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(in);
    }


    private void initService() {

    }

    @Override
    public void shutdown() {
//        QueueManager.setRunning(false);
        dbExecutor.shutdown();
    }

    @Override
    public void destroy(){
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
        List<NulsThread> threadList = this.getThreadList();
        str.append(threadList.size());
        str.append("ThreadInfo:\n");
        for (NulsThread t : threadList) {
            str.append(t.getInfo());
        }
//        str.append("QueueInfo:\n");
//        List<StatInfo> list = QueueManager.getAllStatInfo();
//        for(StatInfo si :list){
//            str.append(si.toString());
//        }
        return str.toString();
    }

    @Override
    public String getVersion() {
        return null;
    }

    public SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }
}

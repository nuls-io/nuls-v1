package io.nuls.db;

import io.nuls.db.dao.mybatis.session.NulsSqlSessionFactory;
import io.nuls.db.dao.mybatis.session.NulsSqlSessionFactoryBuilder;
import io.nuls.db.impl.BlockStoreImpl;
import io.nuls.db.intf.IBlockStore;
import io.nuls.global.NulsContext;
import io.nuls.task.ModuleManager;
import io.nuls.task.ModuleStatus;
import io.nuls.task.NulsThread;
import io.nuls.util.aop.AopUtils;
import io.nuls.util.aop.NulsMethodFilter;
import io.nuls.util.constant.ErrorCode;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.h2.tools.RunScript;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by zhouwei on 2017/9/27.
 * nuls.io
 */
public class DBModuleImpl extends DBModule {

    private ScheduledExecutorService dbExecutor;

    private NulsSqlSessionFactory sqlSessionFactory;



    @Override
    public void start(){

        try {
            initSqlSessionFactory();
            //initDataBaseTables();
            initService();
        }catch (Exception e) {
            e.printStackTrace();
            throw new DBException(ErrorCode.DB_SQLSESSION_INIT_FAIL);
        }

    }


    private void initSqlSessionFactory()throws IOException{
        String resource = "mybatis/mybatis-config.xml";
        InputStream in = Resources.getResourceAsStream(resource);
        sqlSessionFactory = new NulsSqlSessionFactoryBuilder().build(in);
    }

    private void initDataBaseTables() throws Exception {
        String schemaFile = getClass().getResource("/sql/schema-h2.sql").toURI().toString();
        InputStream in = null;

        try {
            Properties conf = new Properties();
            in = this.getClass().getResourceAsStream("/db_config.properties");
            conf.load(in);

            String user = conf.getProperty("druid.username");
            String password = conf.getProperty("druid.password");

            String url = "-url jdbc:h2:file:../data/h2";

            RunScript.execute(url,user,password,schemaFile,null,true);
        }finally {
            if(in != null) {
                in.close();
            }
        }
    }

    private void initService() {
        Class[] classes = new Class[]{NulsSqlSessionFactory.class};
        Object[] params = new Object[]{sqlSessionFactory};
        BlockStoreImpl blockStore = AopUtils.createProxy(BlockStoreImpl.class, classes, params, new NulsMethodFilter() {
            @Override
            public void before(Object obj, Method method, Object[] args) {

            }

            @Override
            public void after(Object obj, Method method, Object[] args, Object result) {

            }

            @Override
            public void exception(Object obj, Method method, Object[] args, Exception e) {

            }
        });
        this.setiBlockStore(blockStore);
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

    public NulsSqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }

    public void setSqlSessionFactory(NulsSqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }


}

package io.nuls.db.module.impl;

import com.alibaba.druid.pool.DruidDataSource;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.utils.aop.AopUtils;
import io.nuls.core.utils.log.Log;
import io.nuls.db.constant.DBConstant;
import io.nuls.db.dao.*;
import io.nuls.db.dao.filter.DBMethodFilter;
import io.nuls.db.dao.impl.mybatis.*;
import io.nuls.db.dao.impl.mybatis.session.SessionManager;
import io.nuls.db.exception.DBException;
import io.nuls.db.module.AbstractDBModule;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * @author vivi
 * @date 2017/9/27
 */
public class MybatisDBModuleImpl extends AbstractDBModule {

    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void start() {
        try {
            initSqlSessionFactory();
            initService();
        } catch (Exception e) {
            Log.error(e);
            throw new DBException(ErrorCode.DB_MODULE_START_FAIL);
        }

    }


    private void initSqlSessionFactory() throws IOException, SQLException {
        String resource = "mybatis/mybatis-config.xml";
        InputStream in = Resources.getResourceAsStream(resource);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(in);
        SessionManager.sqlSessionFactory = sqlSessionFactory;
    }


    private void initService() {
        DBMethodFilter dbMethodFilter = new DBMethodFilter();

        this.registerService(BlockDao.class, AopUtils.createProxy(BlockDaoImpl.class, dbMethodFilter));
        this.registerService(AliasDao.class, AopUtils.createProxy(AliasDaoImpl.class, dbMethodFilter));
        this.registerService(AccountDao.class, AopUtils.createProxy(AccountDaoImpl.class, dbMethodFilter));
        this.registerService(ConsensusAccountDao.class, AopUtils.createProxy(ConsensusAccountDaoImpl.class, dbMethodFilter));
        this.registerService(PeerDao.class, AopUtils.createProxy(PeerDaoImpl.class, dbMethodFilter));
        this.registerService(PeerGroupDao.class, AopUtils.createProxy(PeerGroupDaoImpl.class, dbMethodFilter));
        this.registerService(PeerGroupRelationDao.class, AopUtils.createProxy(PeerGroupRelationDaoImpl.class, dbMethodFilter));
        this.registerService(TransactionDao.class, AopUtils.createProxy(TransactionDaoImpl.class, dbMethodFilter));
        this.registerService(TransactionLocalDao.class, AopUtils.createProxy(TransactionLocalDaoImpl.class, dbMethodFilter));
        this.registerService(TxAccountRelationDao.class, AopUtils.createProxy(TxAccountRelationDaoImpl.class, dbMethodFilter));
        this.registerService(UtxoOutputDao.class, AopUtils.createProxy(UtxoOutputDaoImpl.class, dbMethodFilter));
        this.registerService(UtxoInputDao.class, AopUtils.createProxy(UtxoInputDaoImpl.class, dbMethodFilter));
        this.registerService(SubChainDao.class, AopUtils.createProxy(SubChainDaoImpl.class, dbMethodFilter));
    }

    @Override
    public void shutdown() {
//        QueueManager.setRunning(false);
//        dbExecutor.shutdown();
        if (sqlSessionFactory != null) {
            DruidDataSource druidDataSource = (DruidDataSource) sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
            druidDataSource.close();
            sqlSessionFactory = null;
        }
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
//        str.append("ThreadInfo:\n");
//        for (BaseNulsThread t : threadList) {
//            str.append(t.getInfo());
//        }
//        str.append("QueueInfo:\n");
//        List<StatInfo> list = QueueManager.getAllStatInfo();
//        for(StatInfo si :list){
//            str.append(si.toString());
//        }
        return str.toString();
    }

    @Override
    public int getVersion() {
        return DBConstant.DB_MODULE_VERSION;
    }

    public SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

}

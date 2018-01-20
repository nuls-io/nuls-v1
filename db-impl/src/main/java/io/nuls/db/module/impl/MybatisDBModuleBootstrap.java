/**
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.db.module.impl;

import com.alibaba.druid.pool.DruidDataSource;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.utils.aop.AopUtils;
import io.nuls.core.utils.log.Log;
import io.nuls.db.constant.DBConstant;
import io.nuls.db.dao.*;
import io.nuls.db.dao.filter.TransactionalAopFilterImpl;
import io.nuls.db.dao.impl.mybatis.*;
import io.nuls.db.dao.impl.mybatis.session.SessionManager;
import io.nuls.db.exception.DBException;
import io.nuls.db.module.AbstractDBModule;
import io.nuls.db.transactional.TransactionalAopFilter;
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
public class MybatisDBModuleBootstrap extends AbstractDBModule {

    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void init() {
        // todo auto-generated method stub(niels)

    }

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
        TransactionalAopFilterImpl transactionalFilter = new TransactionalAopFilterImpl();
        this.registerService(TransactionalAopFilter.class,transactionalFilter);
        this.registerService(BlockHeaderService.class, AopUtils.createProxy(BlockDaoImpl.class, transactionalFilter));
        this.registerService(AliasDataService.class, AopUtils.createProxy(AliasDaoImpl.class, transactionalFilter));
        this.registerService(AccountDataService.class, AopUtils.createProxy(AccountDaoImpl.class, transactionalFilter));
        this.registerService(DelegateDataService.class, AopUtils.createProxy(DelegateDaoImpl.class, transactionalFilter));
        this.registerService(DelegateAccountDataService.class, AopUtils.createProxy(DelegateAccountDaoImpl.class, transactionalFilter));
        this.registerService(NodeDataService.class, AopUtils.createProxy(NodeDaoImpl.class, transactionalFilter));
        this.registerService(NodeGroupDataService.class, AopUtils.createProxy(NodeGroupDaoImpl.class, transactionalFilter));
        this.registerService(NodeGroupRelationDataService.class, AopUtils.createProxy(NodeGroupRelationDaoImpl.class, transactionalFilter));
        this.registerService(TransactionDataService.class, AopUtils.createProxy(TransactionDaoImpl.class, transactionalFilter));
        this.registerService(TransactionLocalDataService.class, AopUtils.createProxy(TransactionLocalDaoImpl.class, transactionalFilter));
        this.registerService(TxAccountRelationDataService.class, AopUtils.createProxy(TxAccountRelationDaoImpl.class, transactionalFilter));
        this.registerService(UtxoOutputDataService.class, AopUtils.createProxy(UtxoOutputDaoImpl.class, transactionalFilter));
        this.registerService(UtxoInputDataService.class, AopUtils.createProxy(UtxoInputDaoImpl.class, transactionalFilter));
        this.registerService(SubChainDataService.class, AopUtils.createProxy(SubChainDaoImpl.class, transactionalFilter));
        this.registerService(AccountAliasDataService.class, AopUtils.createProxy(AccountTxDaoImpl.class, transactionalFilter));
        this.registerService(UtxoTransactionDataService.class, AopUtils.createProxy(UtxoTransactionDaoImpl.class, transactionalFilter));
    }

    @Override
    public void shutdown() {
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

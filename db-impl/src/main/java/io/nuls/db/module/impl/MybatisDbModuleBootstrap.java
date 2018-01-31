/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
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
import io.nuls.core.utils.log.Log;
import io.nuls.db.constant.DBConstant;
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
public class MybatisDbModuleBootstrap extends AbstractDBModule {

    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void init() {
        initService();
        try {
            initSqlSessionFactory();
        } catch (Exception e) {
            Log.error(e);
            throw new DBException(ErrorCode.DB_MODULE_START_FAIL);
        }
    }

    @Override
    public void start() {

    }


    private void initSqlSessionFactory() throws IOException, SQLException {
        String resource = "mybatis/mybatis-config.xml";
        InputStream in = Resources.getResourceAsStream(resource);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(in);
        SessionManager.setSqlSessionFactory(sqlSessionFactory);
    }


    private void initService() {
        this.registerService(BlockDaoImpl.class);
        this.registerService(AliasDaoImpl.class);
        this.registerService(AccountDaoImpl.class);
        this.registerService(DelegateDaoImpl.class);
        this.registerService(DelegateAccountDaoImpl.class);
        this.registerService(NodeDaoImpl.class);
        this.registerService(NodeGroupDaoImpl.class);
        this.registerService(NodeGroupRelationDaoImpl.class);
        this.registerService(TransactionDaoImpl.class);
        this.registerService(TransactionLocalDaoImpl.class);
        this.registerService(TxAccountRelationDaoImpl.class);
        this.registerService(UtxoOutputDaoImpl.class);
        this.registerService(UtxoInputDaoImpl.class);
        this.registerService(SubChainDaoImpl.class);
        this.registerService(AccountTxDaoImpl.class);
        this.registerService(UtxoTransactionDaoImpl.class);
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

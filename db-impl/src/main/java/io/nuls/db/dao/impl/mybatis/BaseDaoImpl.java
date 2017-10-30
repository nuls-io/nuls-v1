package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.BaseDao;
import io.nuls.db.dao.impl.mybatis.session.SessionAnnotation;
import io.nuls.db.dao.impl.mybatis.session.SessionManager;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * Created by zhouwei on 2017/10/24.
 */
public class BaseDaoImpl{

    private SqlSession getSession() {
        return SessionManager.getSession();
    }

    @SessionAnnotation
    protected <T> T getMapper(Class<T> type) {
        return getSession().getMapper(type);
    }
}

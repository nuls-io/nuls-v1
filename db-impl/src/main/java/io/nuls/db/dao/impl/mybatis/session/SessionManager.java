package io.nuls.db.dao.impl.mybatis.session;

import io.nuls.core.constant.ErrorCode;
import io.nuls.db.exception.DBException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * Created by zhouwei on 2017/10/25.
 */
public class SessionManager {

    public static SqlSessionFactory sqlSessionFactory;

    private static ThreadLocal<SqlSession> sessionHolder = new ThreadLocal<>();

    public static SqlSession getSession() {
        if (sqlSessionFactory == null) {
            throw new DBException(ErrorCode.DB_SAVE_CANNOT_NULL);
        }
        return sessionHolder.get();
    }

    public static void setConnection(SqlSession session) {
        sessionHolder.set(session);
    }

    public static void removeSession() {
        sessionHolder.remove();
    }
}

package io.nuls.db.dao.impl.mybatis.session;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.exception.DBException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouwei
 * @date 2017/10/25
 */
public class SessionManager {

    public static SqlSessionFactory sqlSessionFactory;

    private static ThreadLocal<Map<String, SqlSession>> sessionHolder = new ThreadLocal<>();
    private static ThreadLocal<Map<String, Boolean>> txHolder = new ThreadLocal<>();
    private static ThreadLocal<String> idHolder = new ThreadLocal<>();

    public static String getId() {
        return idHolder.get();
    }

    public static void setId(String id) {
        idHolder.set(id);
    }

    public static SqlSession getSession(){
        return getSession(idHolder.get());
    }

    public static SqlSession getSession(String id) {
        if (sqlSessionFactory == null) {
            throw new DBException(ErrorCode.DB_SAVE_CANNOT_NULL);
        }
        Map<String, SqlSession> map = sessionHolder.get();
        if (null == map || !map.containsKey(id)) {
            return null;
        }
        return map.get(id);
    }

    public static void setConnection(String id, SqlSession session) {
        Map<String, SqlSession> map = sessionHolder.get();
        if (null == map) {
            map = new HashMap<>();
        }
        if (null == session) {
            map.remove(id);
        } else {
            map.put(id, session);
        }
        sessionHolder.set(map);
    }

    public static void startTransaction(String id) {
        Map<String, Boolean> map = txHolder.get();
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(id, true);
        txHolder.set(map);
    }

    public static void endTransaction(String id) {
        Map<String, Boolean> map = txHolder.get();
        if (map == null) {
            map = new HashMap<>();
        }
        map.remove(id);
        txHolder.set(map);
    }

    public static boolean getTxState(String id) {
        if(StringUtils.isBlank(id)) {
            return false;
        }
        Map<String, Boolean> map = txHolder.get();
        if (null == map || !map.containsKey(id)) {
            return false;
        }
        return map.get(id);
    }
}

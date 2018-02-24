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
package io.nuls.db.dao.impl.mybatis.session;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.utils.log.Log;
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

    private static SqlSessionFactory sqlSessionFactory;

    private static ThreadLocal<Map<String, SqlSession>> sessionHolder = new ThreadLocal<>();
    private static ThreadLocal<Map<String, Boolean>> txHolder = new ThreadLocal<>();
    private static ThreadLocal<String> idHolder = new ThreadLocal<>();

    public static String getId() {
        return idHolder.get();
    }

    public static void setId(String id) {
        idHolder.remove();
        idHolder.set(id);
    }

    public static SqlSession openSession(boolean autoCommit) {
        if (sqlSessionFactory == null) {
            throw new DBException(ErrorCode.DB_SAVE_CANNOT_NULL);
        }
        return sqlSessionFactory.openSession(autoCommit);
    }

    public static SqlSession getSession() {
        String id = idHolder.get();
        SqlSession session = getSession(id);
        return session;
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
        sessionHolder.remove();
        sessionHolder.set(map);
    }

    public static void startTransaction(String id) {
        Map<String, Boolean> map = txHolder.get();
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(id, true);
        txHolder.remove();
        txHolder.set(map);
    }

    public static void endTransaction(String id) {
        Map<String, Boolean> map = txHolder.get();
        if (map == null) {
            map = new HashMap<>();
        }
        map.remove(id);
        txHolder.remove();
        txHolder.set(map);
    }

    public static boolean getTxState(String id) {
        if (StringUtils.isBlank(id)) {
            return false;
        }
        Map<String, Boolean> map = txHolder.get();
        if (null == map || !map.containsKey(id)) {
            return false;
        }
        return map.get(id);
    }

    public static void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        SessionManager.sqlSessionFactory = sqlSessionFactory;
    }
}

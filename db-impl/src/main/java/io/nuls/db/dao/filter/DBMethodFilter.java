package io.nuls.db.dao.filter;


import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.impl.mybatis.session.PROPAGATION;
import io.nuls.db.dao.impl.mybatis.session.SessionAnnotation;
import io.nuls.db.dao.impl.mybatis.session.SessionManager;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Method;

/**
 * @author zhouwei
 * @date 2017/10/13
 */
public class DBMethodFilter implements MethodInterceptor {

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (!method.getName().equals("hashCode") && !method.getName().equals("toString")) {
            System.out.println("----------" + method.getName());
        }

        String lastId = SessionManager.getId();
        String id = lastId;
        Object result;
        boolean isSessionBeginning = false;
        boolean required = false;
        if (method.isAnnotationPresent(SessionAnnotation.class)) {
            SessionAnnotation annotation = method.getAnnotation(SessionAnnotation.class);
            if (annotation.value() == PROPAGATION.REQUIRED && !SessionManager.getTxState(id)) {
                required = true;
                id = StringUtils.getNewUUID();
            } else if (annotation.value() == PROPAGATION.INDEPENDENT) {
                id = StringUtils.getNewUUID();
            }
        }

        SqlSession session = SessionManager.getSession(id);
        if (session == null) {
            isSessionBeginning = true;
            session = SessionManager.sqlSessionFactory.openSession(false);

            SessionManager.setConnection(id, session);
            SessionManager.setId(id);
        } else {
            isSessionBeginning = false;
        }
        try {
            SessionManager.startTransaction(id);
            result = methodProxy.invokeSuper(obj, args);
            if (required) {
                session.commit();
                SessionManager.endTransaction(id);
            }
        } catch (Exception e) {
            session.rollback();
            SessionManager.endTransaction(id);
            throw e;
        } finally {
            if (isSessionBeginning) {
                SessionManager.setConnection(id, null);
                SessionManager.setId(lastId);
                session.close();
            }
        }
        return result;
    }


    private boolean isFilterMethod(Method method) {
        return false;
    }
}

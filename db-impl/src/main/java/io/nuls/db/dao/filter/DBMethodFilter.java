package io.nuls.db.dao.filter;


import io.nuls.db.dao.impl.mybatis.session.PROPAGATION;
import io.nuls.db.dao.impl.mybatis.session.SessionAnnotation;
import io.nuls.db.dao.impl.mybatis.session.SessionManager;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Method;

/**
 *
 * @author zhouwei
 * @date 2017/10/13
 */
public class DBMethodFilter implements MethodInterceptor {

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Object result;
        //not use session
        if (isFilterMethod(method)) {
            return methodProxy.invokeSuper(obj, args);
        }

        // defult = new
        boolean newSession = true;
        if (method.isAnnotationPresent(SessionAnnotation.class)) {
            SessionAnnotation annotation = method.getAnnotation(SessionAnnotation.class);
            if (annotation.value() == PROPAGATION.REQUIRED) {
                newSession = false;
            }
        }

        if (newSession) {
            SqlSession session = SessionManager.sqlSessionFactory.openSession(false);
            SessionManager.setConnection(session);
            try {
                result = methodProxy.invokeSuper(obj, args);
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw e;
            } finally {
                SessionManager.setConnection(null);
                session.close();
            }
            return result;
        } else {
            //required
            boolean isSessionBeginning = false;
            SqlSession session = SessionManager.getSession();
            if (session == null) {
                isSessionBeginning = true;
                session = SessionManager.sqlSessionFactory.openSession(false);
                SessionManager.setConnection(session);
            }
            try {
                result = methodProxy.invokeSuper(obj, args);
                if (isSessionBeginning) {
                    session.commit();
                }
            } catch (Exception e) {
                if(isSessionBeginning) {
                    session.rollback();
                }
                throw e;

            } finally {
                if (isSessionBeginning) {
                    session.close();
                    SessionManager.setConnection(null);
                }
            }
            return result;
        }
    }

    private boolean isFilterMethod(Method method) {
        return false;
    }
}

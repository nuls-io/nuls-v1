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
package io.nuls.db.dao.filter;


import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.transactional.TransactionalAopFilter;
import io.nuls.db.transactional.annotation.PROPAGATION;
import io.nuls.db.transactional.annotation.TransactionalAnnotation;
import io.nuls.db.dao.impl.mybatis.session.SessionManager;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Method;

/**
 * @author zhouwei
 * @date 2017/10/13
 */
public class TransactionalAopFilterImpl implements TransactionalAopFilter {

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        String lastId = SessionManager.getId();
        String id = lastId;
        Object result;
        boolean isSessionBeginning = false;
        boolean isCommit = false;
        if (method.isAnnotationPresent(TransactionalAnnotation.class)) {
            TransactionalAnnotation annotation = method.getAnnotation(TransactionalAnnotation.class);
            if (annotation.value() == PROPAGATION.REQUIRED && !SessionManager.getTxState(id)) {
                isCommit = true;
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
            if (isCommit) {
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

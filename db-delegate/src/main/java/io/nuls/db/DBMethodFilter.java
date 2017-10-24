package io.nuls.db;

import io.nuls.db.dao.mybatis.session.NulsSessionMap;
import io.nuls.db.dao.mybatis.session.NulsSqlSession;
import io.nuls.db.impl.BaseStore;
import io.nuls.util.aop.NulsMethodFilter;

import java.lang.reflect.Method;

/**
 * Created by zhouwei on 2017/10/13.
 */
public class DBMethodFilter implements NulsMethodFilter {
    @Override
    public void before(Object obj, Method method, Object[] args) {
        BaseStore store = (BaseStore)obj;
        if(!NulsSessionMap.exist(Thread.currentThread())) {
            NulsSqlSession sqlSession = store.getSqlSessionFactory().openSession();
            sqlSession.setOpenSessionClass(obj);
            NulsSessionMap.put(Thread.currentThread(), sqlSession);
        }
    }

    @Override
    public void after(Object obj, Method method, Object[] args, Object result) {
        if(NulsSessionMap.exist(Thread.currentThread())) {
            BaseStore store = (BaseStore)obj;
            NulsSqlSession sqlSession = NulsSessionMap.get(Thread.currentThread());
            if(sqlSession != null) {
                if(store.equals(sqlSession.getOpenSessionClass())) {
                    sqlSession.commit();
                    NulsSessionMap.remove(Thread.currentThread());
                }
            }
        }
    }

    @Override
    public void exception(Object obj, Method method, Object[] args, Exception e) {
        if(NulsSessionMap.exist(Thread.currentThread())) {
            BaseStore store = (BaseStore)obj;
            NulsSqlSession sqlSession = NulsSessionMap.get(Thread.currentThread());
            if(sqlSession != null) {
                if(store.equals(sqlSession.getOpenSessionClass())) {
                    sqlSession.rollback();
                    NulsSessionMap.remove(Thread.currentThread());
                }
            }
        }
    }
}

package io.nuls.db.dao.filter;


import io.nuls.core.utils.aop.NulsMethodFilter;

import java.lang.reflect.Method;

/**
 * Created by win10 on 2017/10/13.
 */
public class DBMethodFilter implements NulsMethodFilter {
    @Override
    public void before(Object obj, Method method, Object[] args) {

    }

    @Override
    public void after(Object obj, Method method, Object[] args, Object result) {

    }

    @Override
    public void exception(Object obj, Method method, Object[] args, Exception e) {

    }
}

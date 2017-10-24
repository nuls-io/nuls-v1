package io.nuls.db.dao;

import java.util.List;

/**
 * Created by win10 on 2017/9/29.
 */
public interface BaseDao<T,K> {

    int save(T t);

    int saveBatch(List<T> list);

    int update(T t, boolean selective);

    T getByKey(K k);

    int deleteByKey(K k);

}

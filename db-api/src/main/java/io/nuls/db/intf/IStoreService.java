package io.nuls.db.intf;

import java.util.List;

/**
 * Created by win10 on 2017/9/26.
 */
public interface IStoreService<T,K> {


    int save(T t);

    T getByKey(K key);

    List<T> getList();

    int update(T t);

    int remove(T t);

    int truncate();

    long count();
}
package io.nuls.db.intf;

import java.util.List;

/**
 * Created by win10 on 2017/9/29.
 */
public interface IStore<T,K> {

    int save(T t);

    int saveBatch(List<T> list);

    int update(T t, boolean selective);

    T getByKey(K k);

    int deleteByKey(K k);

}

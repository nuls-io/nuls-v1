package io.nuls.db.intf;

/**
 * Created by win10 on 2017/9/26.
 */
public interface IStoreService<T,K> {


    void save(T t);

    T getByKey(K key);
}

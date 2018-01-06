package io.nuls.db.dao;

import java.util.List;
import java.util.Map;

/**
 * @author zhouwei
 * @date 2017/9/29
 */
public interface BaseDataService<K, T> {

    /**
     * 单个数据存储
     *
     * @param t 持久化数据
     * @return
     */
    int save(T t);

    /**
     * 批量存储
     *
     * @param list 持久化数据集合
     * @return
     */
    int save(List<T> list);

    /**
     * 修改数据
     *
     * @param t 修改后持久化数据
     * @return
     */
    int update(T t);

    int update(List<T> list);

    /**
     * 通过键值获取数据
     *
     * @param k 数据的键值
     * @return
     */
    T get(K k);

    /**
     * 删除持久化数据
     *
     * @param k 数据的键值
     * @return
     */
    int delete(K k);

    /**
     * @return all
     */
    List<T> getList();

    /**
     * @param params
     * @return
     */
    List<T> getList(Map<String, Object> params);

    /**
     * @return
     */
    Long getCount();

}

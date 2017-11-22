package io.nuls.db.dao;

import java.util.List;

/**
 *
 * @author zhouwei
 * @date 2017/9/29
 */
public interface BaseDao<K,T> {

    /**
     * 单个数据存储
     * @param t 持久化数据
     * @return
     */
    int save(T t);

    /**
     * 批量存储
     * @param list 持久化数据集合
     * @return
     */
    int saveBatch(List<T> list);

    /**
     * 修改数据
     * @param t 修改后持久化数据
     * @return
     */
    int update(T t);

    /**
     * 修改数据，为Null值的字段不做修改
     * @param t 修改后持久化数据
     * @return
     */
    int updateSelective(T t);

    /**
     * 通过键值获取数据
     * @param k 数据的键值
     * @return
     */
    T getByKey(K k);

    /**
     * 删除持久化数据
     * @param k 数据的键值
     * @return
     */
    int deleteByKey(K k);

    List<T> listAll();

    Long count();

}

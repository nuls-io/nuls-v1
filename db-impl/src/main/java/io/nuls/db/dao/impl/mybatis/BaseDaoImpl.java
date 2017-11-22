package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.BaseDao;
import io.nuls.db.dao.impl.mybatis.session.SessionAnnotation;
import io.nuls.db.dao.impl.mybatis.session.SessionManager;
import io.nuls.db.dao.intf.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

/**
 * Created by NielsWang on 2017/10/24.
 * T : the type of Mapper interface
 * K : the type of primary key of Object
 * V : the type of Object
 */
public class BaseDaoImpl<T extends BaseMapper<K, V>, K, V> implements BaseDao<K, V> {
    private Class<T> mapperClass;

    public BaseDaoImpl(Class<T> mapperClass) {
        this.mapperClass = mapperClass;
    }

    private SqlSession getSession() {
        return SessionManager.getSession();
    }

    @SessionAnnotation
    protected T getMapper() {
        return getSession().getMapper(mapperClass);
    }

    @Override
    public int save(V o) {
        return getMapper().insert(o);
    }

    @Override
    public int saveBatch(List<V> list) {
        return getMapper().batchInsert(list);
    }

    @Override
    public int update(V o) {
        return this.getMapper().updateByPrimaryKey(o);
    }

    @Override
    public int updateSelective(V o) {
        return this.getMapper().updateByPrimaryKeySelective(o);
    }

    @Override
    public V getByKey(K key) {
        return this.getMapper().selectByPrimaryKey(key);
    }

    @Override
    public int deleteByKey(K key) {
        return this.getMapper().deleteByPrimaryKey(key);
    }

    @Override
    public List<V> listAll() {
        return this.getMapper().selectAll();
    }
}

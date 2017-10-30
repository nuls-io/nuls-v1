package io.nuls.db.dao.impl.mybatis.common;


import io.nuls.db.dao.impl.mybatis.util.Searchable;

import java.io.Serializable;
import java.util.List;


/**
 * mybatis基础查询类，封装了对象的基本查询方法,
 * 以下方法sql实现方式，都需自行写在mapper.xml里
 * @author zhouwei
 *
 */
public interface BaseMapper<M, ID extends Serializable> {
	
	int insert(M m);

    int insertSelective(M m);

    int insertBatch(List<M> list);
    
    int updateByPrimaryKey(M m);
    
    int updateByPrimaryKeySelective(M m);

    int updateBySearchable(Searchable searchable);
    
    int deleteByPrimaryKey(ID id);

    int deleteBySearchable(Searchable searchable);
    
    M selectByPrimaryKey(ID id);

    M selectBySearchable(Searchable searchable);
    
    long count(Searchable searchable);
    
    List<M> selectList(Searchable searchable);
    
    int existsByKey(ID id);

    long existsBySearchable(Searchable searchable);

    List<M> findAll();

}

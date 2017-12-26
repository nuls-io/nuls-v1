package io.nuls.db.dao.impl.mybatis.mapper;

import io.nuls.db.dao.impl.mybatis.common.BaseMapper;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.TransactionLocalPo;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/20
 */
public interface TransactionLocalMapper  extends BaseMapper<String,TransactionLocalPo> {
    List<TransactionLocalPo> selectByAddress(Searchable searchable);
}
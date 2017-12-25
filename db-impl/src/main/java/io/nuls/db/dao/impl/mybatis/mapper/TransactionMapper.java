package io.nuls.db.dao.impl.mybatis.mapper;

import io.nuls.db.dao.impl.mybatis.common.BaseMapper;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.TransactionPo;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/20
 */
public interface TransactionMapper  extends BaseMapper<String,TransactionPo> {
    List<TransactionPo> selectByAddress(Searchable searchable);
}
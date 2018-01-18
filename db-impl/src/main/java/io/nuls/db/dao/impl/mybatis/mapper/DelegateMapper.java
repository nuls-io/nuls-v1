package io.nuls.db.dao.impl.mybatis.mapper;

import io.nuls.db.dao.impl.mybatis.common.BaseMapper;
import io.nuls.db.entity.DelegatePo;

/**
 * @author Niels
 * @date 2017/11/20
 */
public interface DelegateMapper extends BaseMapper<String,DelegatePo> {

    int deleteByAgentAddress(String address);

    int updateSelectiveByAgentAddress(DelegatePo po);
}
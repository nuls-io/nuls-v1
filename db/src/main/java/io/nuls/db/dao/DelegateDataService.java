package io.nuls.db.dao;

import io.nuls.db.entity.DelegatePo;

/**
 *
 * @author Niels
 * @date 2017/11/20
 */
public interface DelegateDataService extends BaseDataService< String,DelegatePo> {


    int deleteByAgentAddress(String address);

    int updateSelective(DelegatePo po);

    int updateSelectiveByAgentAddress(DelegatePo po);
}

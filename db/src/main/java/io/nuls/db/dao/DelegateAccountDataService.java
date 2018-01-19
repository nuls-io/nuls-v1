package io.nuls.db.dao;

import io.nuls.db.entity.DelegateAccountPo;

/**
 *
 * @author Niels
 * @date 2017/11/20
 */
public interface DelegateAccountDataService extends BaseDataService< String,DelegateAccountPo> {


    int updateSelective(DelegateAccountPo po);
}

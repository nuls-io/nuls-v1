package io.nuls.db.dao;

import io.nuls.db.entity.UtxoInputPo;

import java.util.List;

/**
 *
 * @author Niels
 * @date 2017/11/20
 */
public interface UtxoInputDao extends BaseDao< String,UtxoInputPo> {

    List<UtxoInputPo> getTxInputs(String txHash);
}

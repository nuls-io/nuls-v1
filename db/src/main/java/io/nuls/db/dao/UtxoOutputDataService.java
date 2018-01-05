package io.nuls.db.dao;

import io.nuls.db.entity.UtxoOutputPo;

import java.util.List;

/**
 *
 * @author Niels
 * @date 2017/11/20
 */
public interface UtxoOutputDataService extends BaseDataService< String,UtxoOutputPo> {

    List<UtxoOutputPo> getTxOutputs(String txHash);

    List<UtxoOutputPo> getAccountOutputs(String address, byte status);
}

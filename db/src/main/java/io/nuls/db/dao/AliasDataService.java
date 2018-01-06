package io.nuls.db.dao;

import io.nuls.db.entity.AliasPo;

/**
 * @author vivi
 * @date 2017/12/13.
 */
public interface AliasDataService extends BaseDataService<String, AliasPo> {

    AliasPo getByAddress(String address);
}

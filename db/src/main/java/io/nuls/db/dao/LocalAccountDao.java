package io.nuls.db.dao;

import io.nuls.db.entity.LocalAccountPo;

import java.util.List;

/**
 * Created by Niels on 2017/11/15.
 */
public interface LocalAccountDao extends BaseDao<LocalAccountPo, String> {

    public LocalAccountPo getLocalAccount(String id);

    public List<LocalAccountPo> getLocalAccountList();

    Boolean exist(String address);

    boolean setAlias(String id, String alias);
}

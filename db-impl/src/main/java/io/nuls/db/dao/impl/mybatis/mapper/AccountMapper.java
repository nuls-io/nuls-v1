package io.nuls.db.dao.impl.mybatis.mapper;

import io.nuls.db.dao.impl.mybatis.common.BaseMapper;
import io.nuls.db.entity.AccountPo;

/**
 * Created by Niels on 2017/11/20.
 */
public interface AccountMapper extends BaseMapper<AccountPo, String> {
    int truncate();
}

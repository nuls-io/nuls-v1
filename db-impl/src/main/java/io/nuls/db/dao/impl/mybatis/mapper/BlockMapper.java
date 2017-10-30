package io.nuls.db.dao.impl.mybatis.mapper;

import io.nuls.db.dao.impl.mybatis.common.BaseMapper;
import io.nuls.db.entity.BlockPo;

public interface BlockMapper extends BaseMapper<BlockPo, String> {

    int truncate();
}
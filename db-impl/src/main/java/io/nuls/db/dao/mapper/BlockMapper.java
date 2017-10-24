package io.nuls.db.dao.mapper;

import io.nuls.db.entity.BlockPo;
import io.nuls.db.mybatis.common.BaseMapper;

public interface BlockMapper extends BaseMapper<BlockPo, String> {

    int truncate();
}
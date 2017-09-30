package io.nuls.db.dao.mybatis;

import io.nuls.db.dao.mybatis.base.BaseMapper;
import io.nuls.db.dao.mybatis.base.MyBatisMapper;
import io.nuls.db.entity.Block;
import org.springframework.stereotype.Component;

@MyBatisMapper
public interface BlockMapper extends BaseMapper<Block, String>{

    int truncate();
}
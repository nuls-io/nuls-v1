package io.nuls.db.dao.mybatis;

import io.nuls.db.entity.Block;

public interface BlockMapper {
    int deleteByPrimaryKey(String hash);

    int insert(Block record);

    int insertSelective(Block record);

    Block selectByPrimaryKey(String hash);

    int updateByPrimaryKeySelective(Block record);

    int updateByPrimaryKey(Block record);
}
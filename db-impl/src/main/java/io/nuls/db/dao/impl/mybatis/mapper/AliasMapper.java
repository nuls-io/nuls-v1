package io.nuls.db.dao.impl.mybatis.mapper;

import io.nuls.db.dao.impl.mybatis.common.BaseMapper;
import io.nuls.db.entity.AliasPo;
import org.apache.ibatis.annotations.Param;

public interface AliasMapper extends BaseMapper<String, AliasPo> {

    AliasPo getByAddress(@Param("address")String adderss);
}
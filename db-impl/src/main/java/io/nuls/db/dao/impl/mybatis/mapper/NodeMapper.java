package io.nuls.db.dao.impl.mybatis.mapper;

import io.nuls.db.dao.impl.mybatis.common.BaseMapper;
import io.nuls.db.entity.NodePo;

/**
 * @author Niels
 * @date 2017/11/20
 */
public interface NodeMapper  extends BaseMapper<String,NodePo> {

    int updateByIp(NodePo po);

    int deleteByIp(NodePo po);
}
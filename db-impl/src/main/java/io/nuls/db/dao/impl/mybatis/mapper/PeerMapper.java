package io.nuls.db.dao.impl.mybatis.mapper;

import io.nuls.db.dao.impl.mybatis.common.BaseMapper;
import io.nuls.db.entity.PeerPo;

/**
 * @author Niels
 * @date 2017/11/20
 */
public interface PeerMapper  extends BaseMapper<String,PeerPo> {

    int updateByIp(PeerPo po);

    int deleteByIp(PeerPo po);
}
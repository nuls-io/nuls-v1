package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.ConsensusAccountDao;
import io.nuls.db.dao.impl.mybatis.mapper.ConsensusAccountMapper;
import io.nuls.db.entity.ConsensusAccountPo;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class ConsensusAccountDaoImpl extends BaseDaoImpl<ConsensusAccountMapper, String, ConsensusAccountPo> implements ConsensusAccountDao {
    public ConsensusAccountDaoImpl() {
        super(ConsensusAccountMapper.class);
    }
}

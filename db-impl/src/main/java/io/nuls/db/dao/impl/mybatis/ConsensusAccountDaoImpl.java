package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.ConsensusAccountDao;
import io.nuls.db.dao.impl.mybatis.mapper.ConsensusAccountMapper;
import io.nuls.db.dao.impl.mybatis.params.ConsensusAccountSearchParams;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.ConsensusAccountPo;

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class ConsensusAccountDaoImpl extends BaseDaoImpl<ConsensusAccountMapper, String, ConsensusAccountPo> implements ConsensusAccountDao {
    public ConsensusAccountDaoImpl() {
        super(ConsensusAccountMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        return new ConsensusAccountSearchParams(params);
    }
}

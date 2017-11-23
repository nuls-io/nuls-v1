package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.PeerDao;
import io.nuls.db.dao.impl.mybatis.mapper.PeerMapper;
import io.nuls.db.dao.impl.mybatis.params.PeerSearchParams;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.PeerPo;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class PeerDaoImpl extends BaseDaoImpl<PeerMapper, String, PeerPo> implements PeerDao {
    public PeerDaoImpl() {
        super(PeerMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        return new PeerSearchParams(params);
    }
}

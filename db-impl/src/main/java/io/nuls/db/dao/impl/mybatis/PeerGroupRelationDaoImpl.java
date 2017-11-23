package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.PeerGroupRelationDao;
import io.nuls.db.dao.impl.mybatis.mapper.PeerGroupRelationMapper;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.PeerGroupRelationPo;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class PeerGroupRelationDaoImpl extends BaseDaoImpl<PeerGroupRelationMapper, String, PeerGroupRelationPo> implements PeerGroupRelationDao {
    public PeerGroupRelationDaoImpl() {
        super(PeerGroupRelationMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        //todo
        return null;
    }
}

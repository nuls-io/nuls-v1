package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.PeerGroupDao;
import io.nuls.db.dao.impl.mybatis.mapper.PeerGroupMapper;
import io.nuls.db.dao.impl.mybatis.mapper.PeerMapper;
import io.nuls.db.dao.impl.mybatis.params.PeerSearchParams;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.PeerGroupPo;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class PeerGroupDaoImpl extends BaseDaoImpl<PeerGroupMapper, String, PeerGroupPo> implements PeerGroupDao {
    public PeerGroupDaoImpl() {
        super(PeerGroupMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        //todo
        return null;
    }
}

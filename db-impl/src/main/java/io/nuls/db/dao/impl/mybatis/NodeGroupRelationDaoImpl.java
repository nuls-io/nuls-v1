package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.NodeGroupRelationDataService;
import io.nuls.db.dao.impl.mybatis.mapper.NodeGroupRelationMapper;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.NodeGroupRelationPo;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class NodeGroupRelationDaoImpl extends BaseDaoImpl<NodeGroupRelationMapper, String, NodeGroupRelationPo> implements NodeGroupRelationDataService {
    public NodeGroupRelationDaoImpl() {
        super(NodeGroupRelationMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        //todo
        return null;
    }
}

package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.NodeGroupDataService;
import io.nuls.db.dao.impl.mybatis.mapper.NodeGroupMapper;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.NodeGroupPo;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class NodeGroupDaoImpl extends BaseDaoImpl<NodeGroupMapper, String, NodeGroupPo> implements NodeGroupDataService {
    public NodeGroupDaoImpl() {
        super(NodeGroupMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        //todo
        return null;
    }
}

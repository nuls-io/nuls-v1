package io.nuls.db.dao;

import io.nuls.db.entity.NodePo;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Niels
 * @date 2017/11/20
 */
public interface NodeDataService extends BaseDataService< String,NodePo> {


    public List<NodePo> getRandomNodePoList(int size, Set<String> keys);

    public void saveChange(NodePo po);
}

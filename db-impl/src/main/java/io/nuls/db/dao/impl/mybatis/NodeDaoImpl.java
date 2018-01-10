package io.nuls.db.dao.impl.mybatis;

import com.github.pagehelper.PageHelper;
import io.nuls.core.utils.date.TimeService;
import io.nuls.db.dao.NodeDataService;
import io.nuls.db.dao.impl.mybatis.mapper.NodeMapper;
import io.nuls.db.dao.impl.mybatis.params.NodeSearchParams;
import io.nuls.db.transactional.annotation.TransactionalAnnotation;
import io.nuls.db.dao.impl.mybatis.util.SearchOperator;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.NodePo;

import java.util.*;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class NodeDaoImpl extends BaseDaoImpl<NodeMapper, String, NodePo> implements NodeDataService {
    public NodeDaoImpl() {
        super(NodeMapper.class);
    }


    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        return new NodeSearchParams(params);
    }

    @Override
    public List<NodePo> getRandomNodePoList(int size, Set<String> keys) {
        Searchable searchable = new Searchable();
        PageHelper.startPage(1, 100);
        PageHelper.orderBy("last_fail_time asc");
        if (!keys.isEmpty()) {
            searchable.addCondition("id", SearchOperator.notIn, keys);
        }
        searchable.addCondition("status", SearchOperator.eq, 0);
        searchable.addCondition("last_fail_time", SearchOperator.lt, TimeService.currentTimeMillis() - TimeService.ONE_HOUR);
        List<NodePo> list = getMapper().selectList(searchable);
        if (list.size() <= size) {
            return list;
        } else {
            Collections.shuffle(list);
        }

        return list.subList(0, size - 1);
    }

    @Override
    @TransactionalAnnotation
    public void saveChange(NodePo po) {
        try {
            Searchable searchable = new Searchable();
            searchable.addCondition("ip", SearchOperator.eq, po.getIp());
            searchable.addCondition("port", SearchOperator.eq, po.getPort());
            if (getMapper().selectCount(searchable) > 0) {
                if (po.getFailCount() >= 3) {
                    getMapper().deleteByIp(po);
                } else {
                    getMapper().updateByIp(po);
                }
            } else {
                getMapper().insert(po);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.db.dao.impl.mybatis;

import com.github.pagehelper.PageHelper;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.NodeDataService;
import io.nuls.db.dao.impl.mybatis.mapper.NodeMapper;
import io.nuls.db.dao.impl.mybatis.params.NodeSearchParams;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.db.dao.impl.mybatis.util.SearchOperator;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.NodePo;
import io.nuls.db.transactional.annotation.PROPAGATION;

import java.util.*;

/**
 * @author Niels
 * @date 2017/11/22
 */
@DbSession(transactional = PROPAGATION.NONE)
public class NodeDaoImpl extends BaseDaoImpl<NodeMapper, String, NodePo> implements NodeDataService {
    public NodeDaoImpl() {
        super(NodeMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        return new NodeSearchParams(params);
    }

    @Override
    public List<NodePo> getNodePoList(int size, Set<String> keys) {
        Searchable searchable = new Searchable();
        PageHelper.startPage(1, size);
        PageHelper.orderBy("last_fail_time asc");
        if (!keys.isEmpty()) {
            List<String> keyList = new ArrayList<>(keys);
            searchable.addCondition("id", SearchOperator.notIn, keyList);
        }
        searchable.addCondition("status", SearchOperator.eq, 0);
        searchable.addCondition("last_fail_time", SearchOperator.lt, TimeService.currentTimeMillis() - TimeService.ONE_HOUR);
        List<NodePo> list = getMapper().selectList(searchable);
        return list;
    }

    @Override
    @DbSession
    public void saveChange(NodePo po) {
        try {
            Searchable searchable = new Searchable();
            searchable.addCondition("id", SearchOperator.eq, po.getId());
            if (getMapper().selectCount(searchable) > 0) {
                getMapper().updateByPrimaryKey(po);
            } else {
                getMapper().insert(po);
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }

    @Override
    @DbSession
    public void removeNode(NodePo po) {
        NodePo nodePo = getMapper().selectByPrimaryKey(po.getId());
        if(nodePo != null && nodePo.getStatus() == NodePo.BLACK) {
            return;
        }
        if (nodePo != null) {
            if (po.getStatus() == NodePo.BLACK || po.getFailCount() <= 1) {
                getMapper().updateByPrimaryKey(po);
            } else {
                getMapper().deleteByPrimaryKey(po.getId());
            }
        }
    }
}

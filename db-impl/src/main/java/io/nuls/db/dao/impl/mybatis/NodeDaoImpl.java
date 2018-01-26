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
                    getMapper().deleteByPrimaryKey(po.getId());
                } else {
                    getMapper().updateByPrimaryKey(po);
                }
            } else {
                getMapper().insert(po);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

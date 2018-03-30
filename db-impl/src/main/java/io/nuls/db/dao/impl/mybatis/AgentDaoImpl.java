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

import io.nuls.db.dao.AgentDataService;
import io.nuls.db.dao.impl.mybatis.mapper.AgentMapper;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.db.transactional.annotation.PROPAGATION;

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
@DbSession(transactional = PROPAGATION.NONE)
public class AgentDaoImpl extends BaseDaoImpl<AgentMapper, String, AgentPo> implements AgentDataService {
    public AgentDaoImpl() {
        super(AgentMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        return null;
    }

    @Override
    public int updateSelective(AgentPo po) {
        return getMapper().updateByPrimaryKeySelective(po);
    }
    @Override
    public List<AgentPo> getAllList(long blockHeight){
        return this.getMapper().getAllList(blockHeight);
    }
    @Override
    public int deleteById(String id, long blockHeight){
        AgentPo agentPo = new AgentPo();
        agentPo.setId(id);
        agentPo.setDelHeight(blockHeight);
        return this.getMapper().deleteByPrimaryKey(agentPo);
    }

}

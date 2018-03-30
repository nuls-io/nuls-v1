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

import io.nuls.db.dao.DepositDataService;
import io.nuls.db.dao.impl.mybatis.mapper.DepositMapper;
import io.nuls.db.dao.impl.mybatis.params.DepositSearchParams;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.DepositPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.db.transactional.annotation.PROPAGATION;

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
@DbSession(transactional = PROPAGATION.NONE)
public class DepositDaoImpl extends BaseDaoImpl<DepositMapper, String, DepositPo> implements DepositDataService {
    public DepositDaoImpl() {
        super(DepositMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        return new DepositSearchParams(params);
    }

    @Override
    public int deleteById(DepositPo po) {
        return this.getMapper().deleteByPrimaryKey(po);
    }

    @Override
    public int deleteByAgentHash(DepositPo po) {
        return this.getMapper().deleteByAgentHash(po);
    }

    @Override
    public int updateSelective(DepositPo po) {
        return this.getMapper().updateByPrimaryKeySelective(po);
    }

    @Override
    public int updateSelectiveByAgentHash(DepositPo po) {
        return this.getMapper().updateSelectiveByAgentHash(po);
    }

    @Override
    public List<DepositPo> getAllList(long blockHeight) {
        return this.getMapper().getAllList(blockHeight);
    }
}

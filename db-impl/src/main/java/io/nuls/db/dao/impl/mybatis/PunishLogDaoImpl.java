/*
 *
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
 *
 */
package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.PunishLogDataService;
import io.nuls.db.dao.impl.mybatis.mapper.PunishLogMapper;
import io.nuls.db.dao.impl.mybatis.util.SearchOperator;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.PunishLogPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.db.transactional.annotation.PROPAGATION;

import java.util.Map;

/**
 * @author vivi
 * @date 2017/12/13.
 */
@DbSession(transactional = PROPAGATION.NONE)
public class PunishLogDaoImpl extends BaseDaoImpl<PunishLogMapper, String, PunishLogPo> implements PunishLogDataService {

    public PunishLogDaoImpl() {
        super(PunishLogMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        if (null == params || params.isEmpty()) {
            return null;
        }

        Integer type = (Integer) params.get("type");
        if (null == type) {
            return null;
        }

        Searchable searchable = new Searchable();
        searchable.addCondition("type", SearchOperator.eq, type);
        return searchable;
    }

    @Override
    public int deleteByHeight(long height) {
        return getMapper().deleteByHeight(height);
    }

    @Override
    public long getCountByRounds(String agentAddress, long startRoundIndex, long endRoundIndex, long startHeight, int punishType) {
        Searchable searchable = new Searchable();
        searchable.addCondition("address", SearchOperator.eq, agentAddress);
        searchable.addCondition("type", SearchOperator.eq, punishType);
        searchable.addCondition("round_index", SearchOperator.gte, startRoundIndex);
        searchable.addCondition("round_index", SearchOperator.lte, endRoundIndex);
        searchable.addCondition("height", SearchOperator.lte, startHeight);
        return this.getMapper().selectCount(searchable);
    }
}

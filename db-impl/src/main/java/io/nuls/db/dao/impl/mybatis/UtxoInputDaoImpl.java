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

import io.nuls.db.dao.UtxoInputDataService;
import io.nuls.db.dao.impl.mybatis.mapper.UtxoInputMapper;
import io.nuls.db.dao.impl.mybatis.util.SearchOperator;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.UtxoInputPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.db.transactional.annotation.PROPAGATION;

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
@DbSession(transactional = PROPAGATION.NONE)
public class UtxoInputDaoImpl extends BaseDaoImpl<UtxoInputMapper, Map<String,Object>, UtxoInputPo> implements UtxoInputDataService {
    public UtxoInputDaoImpl() {
        super(UtxoInputMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        //todo
        return null;
    }

    @Override
    public List<UtxoInputPo> getTxInputs(String txHash) {
        Searchable searchable = new Searchable();
        searchable.addCondition("tx_hash", SearchOperator.eq, txHash);
        return getMapper().selectList(searchable);
    }

    @Override
    public UtxoInputPo getByFromHash(String fromHash, int fromIndex) {
        Searchable searchable = new Searchable();
        searchable.addCondition("from_hash", SearchOperator.eq, fromHash);
        searchable.addCondition("from_index", SearchOperator.eq, fromIndex);
        return getMapper().selectBySearchable(searchable);
    }

    @Override
    public void deleteByHash(String txHash) {
        Searchable searchable = new Searchable();
        searchable.addCondition("tx_hash", SearchOperator.eq, txHash);
        getMapper().deleteBySearchable(searchable);
    }
}

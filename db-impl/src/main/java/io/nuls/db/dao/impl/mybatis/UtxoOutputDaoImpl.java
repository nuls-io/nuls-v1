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
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.db.dao.UtxoOutputDataService;
import io.nuls.db.dao.impl.mybatis.mapper.UtxoOutputMapper;
import io.nuls.db.dao.impl.mybatis.util.SearchOperator;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.UtxoOutputPo;

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class UtxoOutputDaoImpl extends BaseDaoImpl<UtxoOutputMapper, Map<String, Object>, UtxoOutputPo> implements UtxoOutputDataService {
    public UtxoOutputDaoImpl() {
        super(UtxoOutputMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        //todo
        return null;
    }

    @Override
    public List<UtxoOutputPo> getTxOutputs(String txHash) {
        Searchable searchable = new Searchable();
        searchable.addCondition("tx_hash", SearchOperator.eq, txHash);
        return getMapper().selectList(searchable);
    }

    @Override
    public List<UtxoOutputPo> getAccountOutputs(String address, byte status) {
        Searchable searchable = new Searchable();
        searchable.addCondition("status", SearchOperator.eq, status);
        searchable.addCondition("address", SearchOperator.eq, address);
        PageHelper.orderBy("value asc");
        return getMapper().selectList(searchable);
    }

    @Override
    public List<UtxoOutputPo> getAllUnSpend() {
        Searchable searchable = new Searchable();
        searchable.addCondition("status", SearchOperator.ne, 2);
        PageHelper.orderBy("address asc, status asc, value asc");
        return getMapper().selectList(searchable);
    }

    @Override
    public List<UtxoOutputPo> getAccountUnSpend(String address) {
        Searchable searchable = new Searchable();
        searchable.addCondition("status", SearchOperator.ne, 2);
        searchable.addCondition("address", SearchOperator.eq, address);
        PageHelper.orderBy("status asc, value asc");
        return getMapper().selectList(searchable);
    }

    @Override
    public int updateStatus(List<UtxoOutputPo> list) {
        int result = 0;
        for (int i = 0; i < list.size(); i++) {
            int value = getMapper().updateStatus(list.get(i));
            if (value != 1) {
                throw new NulsRuntimeException(ErrorCode.UTXO_STATUS_CHANGE);
            }
            result += value;
        }
        return result;
    }

}

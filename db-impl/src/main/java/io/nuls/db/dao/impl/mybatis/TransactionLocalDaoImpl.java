/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
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
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.TransactionLocalDataService;
import io.nuls.db.dao.impl.mybatis.mapper.TransactionLocalMapper;
import io.nuls.db.dao.impl.mybatis.util.SearchOperator;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.TransactionLocalPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.db.transactional.annotation.PROPAGATION;

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
@DbSession(transactional = PROPAGATION.NONE)
public class TransactionLocalDaoImpl extends BaseDaoImpl<TransactionLocalMapper, String, TransactionLocalPo> implements TransactionLocalDataService {
    public TransactionLocalDaoImpl() {
        super(TransactionLocalMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        //todo
        return null;
    }

    @Override
    public List<TransactionLocalPo> getTxs(Long blockHeight) {
        Searchable searchable = new Searchable();
        searchable.addCondition("block_height", SearchOperator.eq, blockHeight);
        PageHelper.orderBy("block_height asc, create_time asc");
        return getMapper().selectList(searchable);
    }

    @Override
    public List<TransactionLocalPo> getTxs(Long startHeight, Long endHeight) {
        Searchable searchable = new Searchable();
        searchable.addCondition("block_height", SearchOperator.gte, startHeight);
        searchable.addCondition("block_height", SearchOperator.lte, endHeight);
        PageHelper.orderBy("block_height asc, create_time asc");
        return getMapper().selectList(searchable);
    }

    @Override
    public List<TransactionLocalPo> getTxs(String blockHash) {
        Searchable searchable = new Searchable();
        searchable.addCondition("block_hash", SearchOperator.eq, blockHash);
        PageHelper.orderBy("block_height asc, create_time asc");
        return getMapper().selectList(searchable);
    }

    @Override
    public List<TransactionLocalPo> getTxs(byte[] blockHash) {
        String hash = Hex.encode(blockHash);
        return getTxs(hash);
    }

    @Override
    public List<TransactionLocalPo> getTxs(String address, int type, Integer start, Integer limit) {
        Searchable searchable = new Searchable();
        if (type != 0) {
            searchable.addCondition("a.type", SearchOperator.eq, type);
        }
        if (StringUtils.isNotBlank(address)) {
            searchable.addCondition("d.address", SearchOperator.eq, address);
        }

        if (start != null && limit != null) {
            PageHelper.offsetPage(start, limit);
        }
        PageHelper.orderBy("block_height asc, create_time asc");
        return getMapper().selectByAddress(searchable);
    }

    @Override
    public List<TransactionLocalPo> getTxs(String address, int type) {
        return getTxs(address, type, null, null);
    }

}

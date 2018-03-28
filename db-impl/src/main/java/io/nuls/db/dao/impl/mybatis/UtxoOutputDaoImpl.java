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
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.date.DateUtil;
import io.nuls.core.utils.date.TimeService;
import io.nuls.db.dao.UtxoOutputDataService;
import io.nuls.db.dao.impl.mybatis.mapper.UtxoOutputMapper;
import io.nuls.db.dao.impl.mybatis.util.Condition;
import io.nuls.db.dao.impl.mybatis.util.SearchOperator;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.db.transactional.annotation.PROPAGATION;

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
@DbSession(transactional = PROPAGATION.NONE)
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
    public List<UtxoOutputPo> getAccountOutputs(int txType, String address, Long beginTime, Long endTime) {
        Searchable searchable = new Searchable();
        searchable.addCondition("a.type", SearchOperator.eq, txType);
        searchable.addCondition("b.address", SearchOperator.eq, address);
        if (beginTime != null) {
            searchable.addCondition("a.create_time", SearchOperator.gte, beginTime);
        }
        if (endTime != null) {
            searchable.addCondition("a.create_time", SearchOperator.lte, endTime);
        }
        return getMapper().selectAccountOutput(searchable);
    }

    @Override
    public List<UtxoOutputPo> getAllUnSpend() {
        Searchable searchable = new Searchable();
        searchable.addCondition("status", SearchOperator.ne, 2);
        PageHelper.orderBy("address asc, status asc, value asc");
        return getMapper().selectList(searchable);
    }

    @Override
    public long getLockUtxoCount(String address, Long beginTime, Long bestHeight, Long genesisTime) {
        Searchable searchable = new Searchable();

        Condition condition;
        condition = new Condition("lock_time", SearchOperator.gt, bestHeight);
        condition.setPrefix("((");
        searchable.addCondition(condition);

        condition = new Condition("lock_time", SearchOperator.lt, genesisTime);
        condition.setEndfix(")");
        searchable.addCondition(condition);

        condition = new Condition(Condition.OR, "lock_time", SearchOperator.gt, beginTime);
        searchable.addCondition(condition);

        condition = new Condition(Condition.OR, "status", SearchOperator.eq, UtxoOutputPo.LOCKED);
        condition.setEndfix(")");
        searchable.addCondition(condition);

        searchable.addCondition("address", SearchOperator.eq, address);
        return getMapper().selectCount(searchable);
    }

    @Override
    public List<UtxoOutputPo> getLockUtxo(String address, Long beginTime,
                                          Long bestHeight, Long genesisTime,
                                          Integer start, Integer limit) {
        Searchable searchable = new Searchable();
        Condition condition;
        condition = new Condition("lock_time", SearchOperator.gt, bestHeight);
        condition.setPrefix("((");
        searchable.addCondition(condition);

        condition = new Condition("lock_time", SearchOperator.lt, genesisTime);
        condition.setEndfix(")");
        searchable.addCondition(condition);

        condition = new Condition(Condition.OR, "lock_time", SearchOperator.gt, beginTime);
        searchable.addCondition(condition);

        condition = new Condition(Condition.OR, "status", SearchOperator.eq, UtxoOutputPo.LOCKED);
        condition.setEndfix(")");
        searchable.addCondition(condition);
        searchable.addCondition("b.address", SearchOperator.eq, address);
        PageHelper.offsetPage(start, limit);

        return getMapper().selectAccountOutput(searchable);
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

    @Override
    public int updateStatus(UtxoOutputPo po) {
        return getMapper().updateStatus(po);
    }

    @Override
    public void deleteByHash(String txHash) {
        Searchable searchable = new Searchable();
        searchable.addCondition("tx_hash", SearchOperator.eq, txHash);
        getMapper().deleteBySearchable(searchable);
    }

    @Override
    public long getRewardByBlockHeight(long height) {
        Searchable searchable = new Searchable();
        searchable.addCondition("a.type", SearchOperator.eq, TransactionConstant.TX_TYPE_COIN_BASE);
        searchable.addCondition("a.block_height", SearchOperator.eq, height);
        return getMapper().getCoinBaseReward(searchable);
    }

    @Override
    public long getLastDayTimeReward() {
        Searchable searchable = new Searchable();
        long lastDayTime = TimeService.currentTimeMillis() - DateUtil.DATE_TIME;
        searchable.addCondition("a.type", SearchOperator.eq, TransactionConstant.TX_TYPE_COIN_BASE);
        searchable.addCondition("a.create_time", SearchOperator.gt, lastDayTime);
        return getMapper().getCoinBaseReward(searchable);
    }

    @Override
    public long getAccountReward(String address, long lastTime) {
        Searchable searchable = new Searchable();
        searchable.addCondition("a.type", SearchOperator.eq, TransactionConstant.TX_TYPE_COIN_BASE);
        searchable.addCondition("b.address", SearchOperator.eq, address);
        if (lastTime > 0) {
            searchable.addCondition("a.create_time", SearchOperator.gt, lastTime);
        }
        return getMapper().getCoinBaseReward(searchable);
    }

    @Override
    public long getAgentReward(String address, int type) {
        Searchable searchable = new Searchable();
        searchable.addCondition("c.type", SearchOperator.eq, TransactionConstant.TX_TYPE_COIN_BASE);
        if (type == 1) {
            searchable.addCondition("a.agent_address", SearchOperator.eq, address);
        } else {
            searchable.addCondition("a.packing_address", SearchOperator.eq, address);
        }
        return getMapper().getAgentReward(searchable);
    }

    @Override
    public void unlockTxOutput(String txHash) {
        getMapper().unlockTxOutput(txHash);
    }

    @Override
    public void lockTxOutput(String txHash) {
        getMapper().lockTxOutput(txHash);
    }


}

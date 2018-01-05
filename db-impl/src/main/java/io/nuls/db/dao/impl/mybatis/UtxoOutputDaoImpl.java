package io.nuls.db.dao.impl.mybatis;

import com.github.pagehelper.PageHelper;
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
public class UtxoOutputDaoImpl extends BaseDaoImpl<UtxoOutputMapper, String, UtxoOutputPo> implements UtxoOutputDataService {
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
}

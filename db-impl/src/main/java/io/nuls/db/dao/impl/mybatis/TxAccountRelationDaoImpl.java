package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.TxAccountRelationDataService;
import io.nuls.db.dao.impl.mybatis.mapper.TxAccountRelationMapper;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.TxAccountRelationPo;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class TxAccountRelationDaoImpl extends BaseDaoImpl<TxAccountRelationMapper, String, TxAccountRelationPo> implements TxAccountRelationDataService {
    public TxAccountRelationDaoImpl() {
        super(TxAccountRelationMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        //todo
        return null;
    }
}

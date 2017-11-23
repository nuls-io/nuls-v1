package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.UtxoInputDao;
import io.nuls.db.dao.impl.mybatis.mapper.UtxoInputMapper;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.UtxoInputPo;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class UtxoInputDaoImpl extends BaseDaoImpl<UtxoInputMapper, String, UtxoInputPo> implements UtxoInputDao {
    public UtxoInputDaoImpl() {
        super(UtxoInputMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        //todo
        return null;
    }
}

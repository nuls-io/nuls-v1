package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.UtxoOutputDao;
import io.nuls.db.dao.impl.mybatis.mapper.UtxoOutputMapper;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.UtxoOutputPo;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class UtxoOutputDaoImpl extends BaseDaoImpl<UtxoOutputMapper, String, UtxoOutputPo> implements UtxoOutputDao {
    public UtxoOutputDaoImpl() {
        super(UtxoOutputMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        //todo
        return null;
    }
}

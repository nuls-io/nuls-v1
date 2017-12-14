package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.AliasDao;
import io.nuls.db.dao.impl.mybatis.mapper.AliasMapper;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.AliasPo;

import java.util.Map;

/**
 * @author vivi
 * @date 2017/12/13.
 */
public class AliasDaoImpl extends BaseDaoImpl<AliasMapper, String, AliasPo> implements AliasDao {

    public AliasDaoImpl() {
        super(AliasMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        return null;
    }

    @Override
    public AliasPo getByAddress(String address) {

        return getMapper().getByAddress(address);
    }
}

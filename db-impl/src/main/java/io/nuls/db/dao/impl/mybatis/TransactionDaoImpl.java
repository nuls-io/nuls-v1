package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.TransactionDao;
import io.nuls.db.dao.impl.mybatis.mapper.TransactionMapper;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.TransactionPo;

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class TransactionDaoImpl extends BaseDaoImpl<TransactionMapper, String, TransactionPo> implements TransactionDao {
    public TransactionDaoImpl() {
        super(TransactionMapper.class);
    }

    @Override
    protected Searchable getSearchable(Map<String, Object> params) {
        //todo
        return null;
    }

    @Override
    public List<TransactionPo> getTxs(Long blockHeight) {
        //todo
        return null;
    }

    @Override
    public List<TransactionPo> getTxs(String blockHash) {
        //todo
        return null;
    }

    @Override
    public List<TransactionPo> getTxs(byte[] blockHash) {
        //todo
        return null;
    }

    @Override
    public List<TransactionPo> getTxs(String address, int type, int pageNum, int pageSize, boolean isLocal) {
        //todo
        return null;
    }

    @Override
    public List<TransactionPo> listTranscation(int limit, String address) {
        //todo
        return null;
    }

    @Override
    public List<TransactionPo> listTransaction(long blockHeight, String address) {
        //todo
        return null;
    }
}

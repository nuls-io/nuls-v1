package io.nuls.db.dao.impl.mybatis;

import io.nuls.db.dao.TransactionLocalDao;
import io.nuls.db.dao.impl.mybatis.mapper.TransactionLocalMapper;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.TransactionLocalPo;

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class TransactionLocalDaoImpl extends BaseDaoImpl<TransactionLocalMapper, String, TransactionLocalPo> implements TransactionLocalDao {
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
        //todo
        return null;
    }

    @Override
    public List<TransactionLocalPo> getTxs(String blockHash) {
        //todo
        return null;
    }

    @Override
    public List<TransactionLocalPo> getTxs(byte[] blockHash) {
        //todo
        return null;
    }

    @Override
    public List<TransactionLocalPo> getTxs(String address, int type, int pageNum, int pageSize, boolean isLocal) {
        //todo
        return null;
    }

    @Override
    public List<TransactionLocalPo> listTranscation(int limit, String address) {
        //todo
        return null;
    }

    @Override
    public List<TransactionLocalPo> listTransaction(long blockHeight, String address) {
        //todo
        return null;
    }
}

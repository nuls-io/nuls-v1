package io.nuls.db.dao.impl.mybatis;

import com.github.pagehelper.PageHelper;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.TransactionLocalDao;
import io.nuls.db.dao.impl.mybatis.mapper.TransactionLocalMapper;
import io.nuls.db.dao.impl.mybatis.util.SearchOperator;
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
        Searchable searchable = new Searchable();
        searchable.addCondition("block_height", SearchOperator.eq, blockHeight);
        PageHelper.orderBy("create_time asc");
        return getMapper().selectList(searchable);
    }

    @Override
    public List<TransactionLocalPo> getTxs(String blockHash) {
        Searchable searchable = new Searchable();
        searchable.addCondition("block_hash", SearchOperator.eq, blockHash);
        PageHelper.orderBy("create_time asc");
        return getMapper().selectList(searchable);
    }

    @Override
    public List<TransactionLocalPo> getTxs(byte[] blockHash) {
        String hash = Hex.encode(blockHash);
        return getTxs(hash);
    }

    @Override
    public List<TransactionLocalPo> getTxs(String address, int type, int pageNum, int pageSize) {
        Searchable searchable = new Searchable();
        if(type != 0) {
            searchable.addCondition("type", SearchOperator.eq, type);
        }
        if(StringUtils.isNotBlank(address)) {
            searchable.addCondition("address", SearchOperator.eq, address);
        }

        PageHelper.startPage(pageNum, pageSize);
        PageHelper.orderBy("create_time asc");

        return getMapper().selectList(searchable);
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

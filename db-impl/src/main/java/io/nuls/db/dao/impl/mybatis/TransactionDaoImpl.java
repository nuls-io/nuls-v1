package io.nuls.db.dao.impl.mybatis;

import com.github.pagehelper.PageHelper;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.TransactionDataService;
import io.nuls.db.dao.impl.mybatis.mapper.TransactionMapper;
import io.nuls.db.dao.impl.mybatis.util.SearchOperator;
import io.nuls.db.dao.impl.mybatis.util.Searchable;
import io.nuls.db.entity.TransactionPo;

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/22
 */
public class TransactionDaoImpl extends BaseDaoImpl<TransactionMapper, String, TransactionPo> implements TransactionDataService {
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
        Searchable searchable = new Searchable();
        searchable.addCondition("block_height", SearchOperator.eq, blockHeight);
        PageHelper.orderBy("block_height asc, create_time asc");
        return getMapper().selectList(searchable);
    }

    @Override
    public List<TransactionPo> getTxs(Long startHeight, Long endHeight) {
        Searchable searchable = new Searchable();
        searchable.addCondition("block_height", SearchOperator.gte, startHeight);
        searchable.addCondition("block_height", SearchOperator.lte, endHeight);
        PageHelper.orderBy("block_height asc, create_time asc");
        return getMapper().selectList(searchable);
    }

    @Override
    public List<TransactionPo> getTxs(String blockHash) {
        Searchable searchable = new Searchable();
        searchable.addCondition("block_hash", SearchOperator.eq, blockHash);
        PageHelper.orderBy("block_height asc, create_time asc");
        return getMapper().selectList(searchable);
    }

    @Override
    public List<TransactionPo> getTxs(byte[] blockHash) {
        String hash = Hex.encode(blockHash);
        return getTxs(hash);
    }

    @Override
    public List<TransactionPo> getTxs(String address, int type, int pageNum, int pageSize) {
        Searchable searchable = new Searchable();
        if (type != 0) {
            searchable.addCondition("b.type", SearchOperator.eq, type);
        }
        if (StringUtils.isNotBlank(address)) {
            searchable.addCondition("a.address", SearchOperator.eq, address);
        }

        if (pageNum > 0 && pageSize > 0) {
            PageHelper.startPage(pageNum, pageSize);
        }
        PageHelper.orderBy("block_height asc, create_time asc");
        return getMapper().selectByAddress(searchable);
    }

    @Override
    public List<TransactionPo> getTxs(String address, int type) {
       return getTxs(address, type, 0, 0);
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

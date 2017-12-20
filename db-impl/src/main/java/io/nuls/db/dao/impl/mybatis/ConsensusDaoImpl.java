package io.nuls.db.dao.impl.mybatis;

import io.nuls.core.chain.entity.Result;
import io.nuls.core.context.NulsContext;
import io.nuls.db.dao.BlockDao;
import io.nuls.db.dao.ConsensusDao;
import io.nuls.db.dao.TransactionDao;
import io.nuls.db.dao.impl.mybatis.session.SessionAnnotation;
import io.nuls.db.entity.BlockPo;
import io.nuls.db.entity.TransactionPo;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/20
 */
public class ConsensusDaoImpl implements ConsensusDao {

    private BlockDao blockDao = NulsContext.getInstance().getService(BlockDao.class);
    private TransactionDao txDao = NulsContext.getInstance().getService(TransactionDao.class);

    @Override
    @SessionAnnotation
    public Result blockPersistence(BlockPo blockPo, List<TransactionPo> txPoList) {
        blockDao.save(blockPo);
        txDao.saveBatch(txPoList);
        return Result.getSuccess();
    }
}

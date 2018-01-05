package io.nuls.db.dao.impl.mybatis;

import io.nuls.core.chain.entity.Result;
import io.nuls.core.context.NulsContext;
import io.nuls.db.dao.BlockDataService;
import io.nuls.db.dao.ConsensusDataService;
import io.nuls.db.dao.TransactionDataService;
import io.nuls.db.dao.impl.mybatis.session.SessionAnnotation;
import io.nuls.db.entity.BlockPo;
import io.nuls.db.entity.TransactionPo;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/20
 */
public class ConsensusDaoImpl implements ConsensusDataService {

    private BlockDataService blockDao = NulsContext.getInstance().getService(BlockDataService.class);
    private TransactionDataService txDao = NulsContext.getInstance().getService(TransactionDataService.class);

    @Override
    @SessionAnnotation
    public Result blockPersistence(BlockPo blockPo, List<TransactionPo> txPoList) {
        blockDao.save(blockPo);
        txDao.save(txPoList);
        return Result.getSuccess();
    }
}

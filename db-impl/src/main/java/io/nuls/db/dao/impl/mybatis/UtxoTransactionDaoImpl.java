package io.nuls.db.dao.impl.mybatis;

import io.nuls.core.context.NulsContext;
import io.nuls.db.dao.*;
import io.nuls.db.entity.TransactionLocalPo;
import io.nuls.db.entity.TransactionPo;
import io.nuls.db.entity.UtxoInputPo;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.db.util.TransactionPoTool;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vivi
 * @date 2017/12/23.
 */
public class UtxoTransactionDaoImpl implements UtxoTransactionDao {

    private TransactionDao txDao = NulsContext.getInstance().getService(TransactionDao.class);
    private TransactionLocalDao txLocalDao = NulsContext.getInstance().getService(TransactionLocalDao.class);
    private UtxoInputDao inputDao = NulsContext.getInstance().getService(UtxoInputDao.class);
    private UtxoOutputDao outputDao = NulsContext.getInstance().getService(UtxoOutputDao.class);

    @Override
    public TransactionPo gettx(String hash, boolean isLocal) {
        TransactionPo tx;
        if (isLocal) {
            TransactionLocalPo txLocal = txLocalDao.getByKey(hash);
            if (txLocal == null) {
                return null;
            }
            tx = TransactionPoTool.toTx(txLocal);
        } else {
            tx = txDao.getByKey(hash);
        }
        return tx;
    }

    @Override
    public List<TransactionPo> getTxs(Long blockHeight, boolean isLocal) {
        List<TransactionPo> txList;
        if (isLocal) {
            List<TransactionLocalPo> localPoList = txLocalDao.getTxs(blockHeight);
            txList = new ArrayList<>();
            for (TransactionLocalPo localPo : localPoList) {
                TransactionPo tx = TransactionPoTool.toTx(localPo);
                txList.add(tx);
            }
        } else {
            txList = txDao.getTxs(blockHeight);
        }
        return txList;
    }

    @Override
    public List<TransactionPo> getTxs(String blockHash, boolean isLocal) {
        List<TransactionPo> txList;
        if (isLocal) {
            List<TransactionLocalPo> localPoList = txLocalDao.getTxs(blockHash);
            txList = new ArrayList<>();
            for (TransactionLocalPo localPo : localPoList) {
                TransactionPo tx = TransactionPoTool.toTx(localPo);
                txList.add(tx);
            }
        } else {
            txList = txDao.getTxs(blockHash);
        }
        return txList;
    }

    @Override
    public List<TransactionPo> getTxs(byte[] blockHash, boolean isLocal) {
        List<TransactionPo> txList;
        if (isLocal) {
            List<TransactionLocalPo> localPoList = txLocalDao.getTxs(blockHash);
            txList = new ArrayList<>();
            for (TransactionLocalPo localPo : localPoList) {
                TransactionPo tx = TransactionPoTool.toTx(localPo);
                txList.add(tx);
            }
        } else {
            txList = txDao.getTxs(blockHash);
        }
        return txList;
    }

    @Override
    public List<TransactionPo> getTxs(String address, int type, int pageNum, int pageSize, boolean isLocal) {
        List<TransactionPo> txList;
        if (isLocal) {
            List<TransactionLocalPo> localPoList = txLocalDao.getTxs(address, type, pageNum, pageSize);
            txList = new ArrayList<>();
            for (TransactionLocalPo localPo : localPoList) {
                TransactionPo tx = TransactionPoTool.toTx(localPo);
                txList.add(tx);
            }
        } else {
            txList = txDao.getTxs(address, type, pageNum, pageSize);
        }
        return txList;
    }

    @Override
    public List<TransactionPo> listTranscation(int limit, String address, boolean isLocal) {
        return null;
    }

    @Override
    public List<TransactionPo> listTransaction(long blockHeight, String address, boolean isLocal) {
        return null;
    }

    @Override
    public List<UtxoInputPo> getTxInputs(String txhash) {
        return null;
    }

    @Override
    public List<UtxoOutputPo> getTxOutputs(String txhash) {
        return null;
    }

    @Override
    public List<UtxoOutputPo> getUnSpend(String address) {
        return null;
    }

}

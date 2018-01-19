/**
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.db.dao.impl.mybatis;

import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.db.dao.*;
import io.nuls.db.entity.*;
import io.nuls.db.util.TransactionPoTool;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vivi
 * @date 2017/12/23.
 */
public class UtxoTransactionDaoImpl implements UtxoTransactionDataService {

    private TransactionDataService txDao = NulsContext.getInstance().getService(TransactionDataService.class);

    private TransactionLocalDataService txLocalDao = NulsContext.getInstance().getService(TransactionLocalDataService.class);

    private UtxoInputDataService inputDao = NulsContext.getInstance().getService(UtxoInputDataService.class);

    private UtxoOutputDataService outputDao = NulsContext.getInstance().getService(UtxoOutputDataService.class);

    private TxAccountRelationDataService relationDao = NulsContext.getInstance().getService(TxAccountRelationDataService.class);

    private BlockHeaderService blockHeaderDao = NulsContext.getInstance().getService(BlockHeaderService.class);

    @Override
    public TransactionPo gettx(String hash, boolean isLocal) {
        TransactionPo tx;
        if (isLocal) {
            TransactionLocalPo txLocal = txLocalDao.get(hash);
            if (txLocal == null) {
                return null;
            }
            tx = TransactionPoTool.toTx(txLocal);
        } else {
            tx = txDao.get(hash);
        }
        return tx;
    }

    @Override
    public List<TransactionPo> getTxs(long blockHeight, boolean isLocal) {
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
    public List<TransactionPo> getTxs(long startHeight, long endHeight, boolean isLocal) {
        List<TransactionPo> txList;
        if (isLocal) {
            List<TransactionLocalPo> localPoList = txLocalDao.getTxs(startHeight, endHeight);
            txList = new ArrayList<>();
            for (TransactionLocalPo localPo : localPoList) {
                TransactionPo tx = TransactionPoTool.toTx(localPo);
                txList.add(tx);
            }
        } else {
            txList = txDao.getTxs(startHeight, endHeight);
        }
        return txList;
    }

    @Override
    public List<TransactionPo> getTxs(String blockHash, boolean isLocal) {
        BlockHeaderPo header = blockHeaderDao.getHeader(blockHash);
        if (header == null) {
            return null;
        }

        List<TransactionPo> txList;
        if (isLocal) {
            List<TransactionLocalPo> localPoList = txLocalDao.getTxs(header.getHeight());
            txList = new ArrayList<>();
            for (TransactionLocalPo localPo : localPoList) {
                TransactionPo tx = TransactionPoTool.toTx(localPo);
                txList.add(tx);
            }
        } else {
            txList = txDao.getTxs(header.getHeight());
        }
        return txList;
    }

    @Override
    public List<TransactionPo> getTxs(byte[] blockHash, boolean isLocal) {
        String hash = Hex.encode(blockHash);
        return this.getTxs(hash, isLocal);
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
    public List<TransactionPo> getTxs(String address, int type, boolean isLocal) {
        List<TransactionPo> txList;
        if (isLocal) {
            List<TransactionLocalPo> localPoList = txLocalDao.getTxs(address, type);
            txList = new ArrayList<>();
            for (TransactionLocalPo localPo : localPoList) {
                TransactionPo tx = TransactionPoTool.toTx(localPo);
                txList.add(tx);
            }
        } else {
            txList = txDao.getTxs(address, type);
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
    public List<UtxoInputPo> getTxInputs(String txHash) {
        return inputDao.getTxInputs(txHash);
    }

    @Override
    public List<UtxoOutputPo> getTxOutputs(String txHash) {
        return outputDao.getTxOutputs(txHash);
    }

    @Override
    public List<UtxoOutputPo> getAccountOutputs(String address, byte status) {
        return outputDao.getAccountOutputs(address, status);
    }

    @Override
    public List<UtxoOutputPo> getAccountUnSpend(String address) {
        return null;
    }

    @Override
    public int save(TransactionPo po) {
        return txDao.save(po);
    }

    @Override
    public int save(TransactionLocalPo localPo) {
        return txLocalDao.save(localPo);
    }

    @Override
    public int saveTxList(List<TransactionPo> poList) {
        return txDao.save(poList);
    }

    @Override
    public int saveLocalList(List<TransactionLocalPo> poList) {
        return txLocalDao.save(poList);
    }

}

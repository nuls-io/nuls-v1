/*
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
 *
 */
package io.nuls.contract.storage.service.impl;

import io.nuls.contract.entity.tx.ContractTransferTransaction;
import io.nuls.contract.storage.constant.ContractStorageConstant;
import io.nuls.contract.storage.service.ContractTransferTransactionStorageService;
import io.nuls.core.tools.log.Log;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.NulsByteBuffer;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/11
 */
@Component
public class ContractTransferTransactionStorageImpl implements ContractTransferTransactionStorageService, InitializingBean {

    @Autowired
    private DBService dbService;

    @Override
    public void afterPropertiesSet() throws NulsException {
        Result result = dbService.createArea(ContractStorageConstant.DB_NAME_CONTRACT_SPECIAL_TX);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
    }

    @Override
    public Result saveContractTransferTx(NulsDigestData hash, Transaction tx) {
        Result result;
        try {
            result = dbService.put(ContractStorageConstant.DB_NAME_CONTRACT_SPECIAL_TX, hash.serialize(), tx.serialize());
        } catch (Exception e) {
            Log.error("save contract transfer Tx error", e);
            return Result.getFailed();
        }
        return result;
    }

    @Override
    public Result deleteContractTransferTx(NulsDigestData hash) {
        try {
            return dbService.delete(ContractStorageConstant.DB_NAME_CONTRACT_SPECIAL_TX, hash.serialize());
        } catch (Exception e) {
            Log.error("delete contract transfer Tx error", e);
            return Result.getFailed();
        }
    }

    @Override
    public Result<ContractTransferTransaction> getContractTransferTx(NulsDigestData hash) {
        try {
            byte[] txBytes = dbService.get(ContractStorageConstant.DB_NAME_CONTRACT_SPECIAL_TX, hash.serialize());
            if (txBytes == null) {
                return Result.getSuccess();
            }
            ContractTransferTransaction contractTransferTransaction = new ContractTransferTransaction();
            contractTransferTransaction.parse(txBytes, 0);
            return Result.getSuccess().setData(contractTransferTransaction);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed();
        }
    }

}

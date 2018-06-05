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
package io.nuls.account.ledger.storage.service.impl;

import io.nuls.account.ledger.constant.AccountLedgerErrorCode;
import io.nuls.account.ledger.storage.constant.AccountLedgerStorageConstant;
import io.nuls.account.ledger.storage.po.TransactionInfoPo;
import io.nuls.account.ledger.storage.service.TransactionInfoStorageService;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.log.Log;
import io.nuls.db.service.DBService;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.utils.AddressTool;

import javax.naming.PartialResultException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * author Facjas
 * date 2018/5/22.
 */
@Component
public class TransactionInfoStorageServiceImpl implements TransactionInfoStorageService, InitializingBean {
    @Autowired
    private DBService dbService;

    @Override
    public void afterPropertiesSet() throws NulsException {
        Result result = dbService.createArea(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX_INDEX);
        if (result.isFailed()) {
            //TODO
        }
    }

    @Override
    public Result saveTransactionInfo(byte[] infoKey, TransactionInfoPo infoPo) {
        try {
            dbService.put(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX_INDEX, infoKey, infoPo.serialize());
            return Result.getSuccess();
        } catch (Exception e) {
            return Result.getFailed();
        }
    }

    @Override
    public List<TransactionInfoPo> getTransactionInfoListByAddress(byte[] address) throws NulsException {
        List<TransactionInfoPo> infoPoList = new ArrayList<>();
        Set<byte[]> keySet = dbService.keySet(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX_INDEX);
        if (keySet == null || keySet.isEmpty()) {
            return infoPoList;
        }

        byte[] addressKey = new byte[AddressTool.HASH_LENGTH];
        for (byte[] key : keySet) {
            System.arraycopy(key, 0, addressKey, 0, AddressTool.HASH_LENGTH);
            if (java.util.Arrays.equals(addressKey, address)) {
                byte[] values = dbService.get(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX_INDEX, key);
                TransactionInfoPo transactionInfoPo = new TransactionInfoPo();
                transactionInfoPo.parse(values);
                infoPoList.add(transactionInfoPo);
            }
        }
        return infoPoList;
    }

    @Override
    public Result deleteTransactionInfo(byte[] infoKey) {
        return dbService.delete(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX_INDEX, infoKey);
    }
}

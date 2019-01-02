/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

import io.nuls.contract.storage.constant.ContractStorageConstant;
import io.nuls.contract.storage.po.TransactionInfoPo;
import io.nuls.contract.storage.service.ContractTransactionInfoStorageService;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.NulsByteBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/5
 */
@Component
public class ContractTransactionInfoStorageServiceImpl implements ContractTransactionInfoStorageService, InitializingBean {
    @Autowired
    private DBService dbService;

    @Override
    public void afterPropertiesSet() throws NulsException {
        Result result = dbService.createArea(ContractStorageConstant.DB_NAME_CONTRACT_LEDGER_TX_INDEX);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
    }

    @Override
    public Result saveTransactionInfo(byte[] infoKey, TransactionInfoPo infoPo) throws IOException {
        return dbService.put(ContractStorageConstant.DB_NAME_CONTRACT_LEDGER_TX_INDEX, infoKey, infoPo.serialize());
    }

    @Override
    public List<TransactionInfoPo> getTransactionInfoListByAddress(byte[] address) throws NulsException {
        List<TransactionInfoPo> infoPoList = new ArrayList<>();
        List<byte[]> keyList = dbService.keyList(ContractStorageConstant.DB_NAME_CONTRACT_LEDGER_TX_INDEX);
        if (keyList == null || keyList.isEmpty()) {
            return infoPoList;
        }

        byte[] addressKey = new byte[Address.ADDRESS_LENGTH];
        TransactionInfoPo transactionInfoPo;
        byte[] values;
        for (byte[] key : keyList) {
            System.arraycopy(key, 0, addressKey, 0, Address.ADDRESS_LENGTH);
            if (Arrays.equals(addressKey, address)) {
                values = dbService.get(ContractStorageConstant.DB_NAME_CONTRACT_LEDGER_TX_INDEX, key);
                transactionInfoPo = new TransactionInfoPo();
                transactionInfoPo.parse(values, 0);
                infoPoList.add(transactionInfoPo);
            }
        }
        return infoPoList;
    }

    @Override
    public Result deleteTransactionInfo(byte[] infoKey) {
        return dbService.delete(ContractStorageConstant.DB_NAME_CONTRACT_LEDGER_TX_INDEX, infoKey);
    }

    @Override
    public Result<byte[]> getTransactionInfo(byte[] infoKey) {
        byte[] txInfoBytes = dbService.get(ContractStorageConstant.DB_NAME_CONTRACT_LEDGER_TX_INDEX, infoKey);
        Result<byte[]> result = Result.getSuccess();
        result.setData(txInfoBytes);
        return result;
    }
}

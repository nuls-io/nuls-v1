/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.storage.service.impl;

import io.nuls.contract.storage.constant.ContractStorageConstant;
import io.nuls.contract.storage.service.ContractUtxoStorageService;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.model.Entry;
import io.nuls.db.service.BatchOperation;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/5
 */
@Component
public class ContractUtxoStorageServiceImpl implements ContractUtxoStorageService, InitializingBean {

    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    /**
     * 该方法在所有属性被设置之后调用，用于辅助对象初始化
     * This method is invoked after all properties are set, and is used to assist object initialization.
     */
    @Override
    public void afterPropertiesSet() throws NulsException {
        Result result = dbService.createArea(ContractStorageConstant.DB_NAME_CONTRACT_LEDGER_UTXO);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
    }

    @Override
    public List<Entry<byte[], byte[]>> loadAllCoinList() {
        List<Entry<byte[], byte[]>> coinList = dbService.entryList(ContractStorageConstant.DB_NAME_CONTRACT_LEDGER_UTXO);
        return coinList;
    }

    @Override
    public byte[] getUTXO(byte[] key) {
        if(key == null) {
            return null;
        }
        return dbService.get(ContractStorageConstant.DB_NAME_CONTRACT_LEDGER_UTXO, key);
    }

    @Override
    public Result<List<Entry<byte[], byte[]>>> batchSaveAndDeleteUTXO(List<Entry<byte[], byte[]>> utxosToSave, List<byte[]> utxosToDelete) {
        BatchOperation batch = dbService.createWriteBatch(ContractStorageConstant.DB_NAME_CONTRACT_LEDGER_UTXO);
        List<Entry<byte[], byte[]>> deleteUtxoEntryList = new ArrayList<>();
        byte[] deleteUtxo;
        if(utxosToDelete != null) {
            for (byte[] key : utxosToDelete) {
                batch.delete(key);
            }
        }

        if(utxosToSave != null) {
            for(Entry<byte[], byte[]> entry : utxosToSave) {
                batch.put(entry.getKey(), entry.getValue());
            }
        }
        Result batchResult = batch.executeBatch();
        if (batchResult.isFailed()) {
            return batchResult;
        }
        return Result.getSuccess().setData(deleteUtxoEntryList);
    }

    @Override
    public BatchOperation createBatchOperation() {
        return dbService.createWriteBatch(ContractStorageConstant.DB_NAME_CONTRACT_LEDGER_UTXO);
    }

}

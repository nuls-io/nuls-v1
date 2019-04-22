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
package io.nuls.contract.ledger.service.impl;

import io.nuls.account.ledger.constant.AccountLedgerErrorCode;
import io.nuls.contract.ledger.service.ContractTransactionInfoService;
import io.nuls.contract.storage.po.TransactionInfoPo;
import io.nuls.contract.storage.service.ContractTransactionInfoStorageService;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/5
 */
@Service
public class ContractTransactionInfoServiceImpl implements ContractTransactionInfoService {

    @Autowired
    private ContractTransactionInfoStorageService contractTransactionInfoStorageService;

    @Override
    public Result<List<TransactionInfoPo>> getTxInfoList(byte[] address) {
        try {
            List<TransactionInfoPo> infoPoList = contractTransactionInfoStorageService.getTransactionInfoListByAddress(address);
            return Result.getSuccess().setData(infoPoList);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }

    @Override
    public Result<Integer> saveTransactionInfo(TransactionInfoPo infoPo, List<byte[]> addresses) {
        if (infoPo == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }

        if (addresses == null || addresses.size() == 0) {
            return Result.getSuccess().setData(0);
        }

        List<byte[]> savedKeyList = new ArrayList<>();

        try {
            byte[] txHashBytes = infoPo.getTxHash().serialize();
            int txHashLength = infoPo.getTxHash().size();
            byte[] infoKey;
            for (int i = 0; i < addresses.size(); i++) {
                infoKey = new byte[Address.ADDRESS_LENGTH + txHashLength];
                System.arraycopy(addresses.get(i), 0, infoKey, 0, Address.ADDRESS_LENGTH);
                System.arraycopy(txHashBytes, 0, infoKey, Address.ADDRESS_LENGTH, txHashLength);
                contractTransactionInfoStorageService.saveTransactionInfo(infoKey, infoPo);
                savedKeyList.add(infoKey);
            }
        } catch (IOException e) {
            for (int i = 0; i < savedKeyList.size(); i++) {
                contractTransactionInfoStorageService.deleteTransactionInfo(savedKeyList.get(i));
            }
            return Result.getFailed(AccountLedgerErrorCode.IO_ERROR);
        }
        return Result.getSuccess().setData(new Integer(addresses.size()));
    }

    @Override
    public boolean isDbExistTransactionInfo(TransactionInfoPo infoPo, byte[] address) {
        try {
            byte[] txHashBytes = infoPo.getTxHash().serialize();
            int txHashLength = infoPo.getTxHash().size();
            byte[] infoKey = new byte[Address.ADDRESS_LENGTH + txHashLength];
            System.arraycopy(address, 0, infoKey, 0, Address.ADDRESS_LENGTH);
            System.arraycopy(txHashBytes, 0, infoKey, Address.ADDRESS_LENGTH, txHashLength);
            Result<byte[]> txInfoBytesResult = contractTransactionInfoStorageService.getTransactionInfo(infoKey);
            if(txInfoBytesResult.getData() == null) {
                return false;
            } else {
                return true;
            }
        } catch (IOException e) {
            Log.error(e);
            return false;
        }
    }

    @Override
    public Result deleteTransactionInfo(TransactionInfoPo infoPo, List<byte[]> addresses) {
        byte[] infoBytes = null;
        if (infoPo == null || addresses == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }

        int addressCount = addresses.size();

        byte[] txHashBytes;
        try {
            txHashBytes = infoPo.getTxHash().serialize();
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }
        for (int i = 0; i < addressCount; i++) {
            contractTransactionInfoStorageService.deleteTransactionInfo(
                    ArraysTool.concatenate(addresses.get(i), txHashBytes));
        }
        return Result.getSuccess().setData(addressCount);
    }
}

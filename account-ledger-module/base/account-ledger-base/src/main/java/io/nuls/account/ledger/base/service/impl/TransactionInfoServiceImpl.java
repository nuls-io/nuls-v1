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
package io.nuls.account.ledger.base.service.impl;

import io.nuls.account.ledger.base.service.TransactionInfoService;
import io.nuls.account.ledger.base.util.TxInfoComparator;
import io.nuls.account.ledger.constant.AccountLedgerErrorCode;
import io.nuls.account.ledger.model.TransactionInfo;
import io.nuls.account.ledger.storage.po.TransactionInfoPo;
import io.nuls.account.ledger.storage.service.TransactionInfoStorageService;
import io.nuls.account.ledger.storage.service.impl.TransactionInfoStorageServiceImpl;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.utils.AddressTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * author Facjas
 * date 2018/5/27.
 */
@Component
public class TransactionInfoServiceImpl implements TransactionInfoService {
    @Autowired
    TransactionInfoStorageService transactionInfoStorageService;

    @Override
    public Result<List<TransactionInfo>> getTxInfoList(byte[] address) {
        try {
            List<TransactionInfoPo> infoPoList = transactionInfoStorageService.getTransactionInfoListByAddress(address);
            List<TransactionInfo> infoList = new ArrayList<>();
            for (TransactionInfoPo po : infoPoList) {
                if (po.getTxType() == ConsensusConstant.TX_TYPE_RED_PUNISH || po.getTxType() == ConsensusConstant.TX_TYPE_YELLOW_PUNISH) {
                    continue;
                }
                infoList.add(po.toTransactionInfo());
            }

            Collections.sort(infoList, TxInfoComparator.getInstance());
            return Result.getSuccess().setData(infoList);
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
            return Result.getSuccess().setData(new Integer(0));
        }

        List<byte[]> savedKeyList = new ArrayList<>();

        try {
            for (int i = 0; i < addresses.size(); i++) {
                byte[] infoKey = new byte[AddressTool.HASH_LENGTH + infoPo.getTxHash().size()];
                System.arraycopy(addresses.get(i), 0, infoKey, 0, AddressTool.HASH_LENGTH);
                System.arraycopy(infoPo.getTxHash().serialize(), 0, infoKey, AddressTool.HASH_LENGTH, infoPo.getTxHash().size());
                transactionInfoStorageService.saveTransactionInfo(infoKey, infoPo);
                savedKeyList.add(infoKey);
            }
        } catch (IOException e) {
            for (int i = 0; i < savedKeyList.size(); i++) {
                transactionInfoStorageService.deleteTransactionInfo(savedKeyList.get(i));
            }
            return Result.getFailed(AccountLedgerErrorCode.IO_ERROR);
        }
        return Result.getSuccess().setData(new Integer(addresses.size()));
    }

    @Override
    public Result deleteTransactionInfo(TransactionInfoPo infoPo) {
        byte[] infoBytes = null;
        if (infoPo == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }

        try {
            infoBytes = infoPo.serialize();
        } catch (IOException e) {
            return Result.getFailed(AccountLedgerErrorCode.IO_ERROR);
        }

        if (ArraysTool.isEmptyOrNull(infoBytes)) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }

        byte[] addresses = infoPo.getAddresses();
        if (addresses.length % AddressTool.HASH_LENGTH != 0) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }

        int addressCount = addresses.length / AddressTool.HASH_LENGTH;

        for (int i = 0; i < addressCount; i++) {

            byte[] infoKey = new byte[AddressTool.HASH_LENGTH + infoPo.getTxHash().size()];
            System.arraycopy(addresses, i * AddressTool.HASH_LENGTH, infoKey, 0, AddressTool.HASH_LENGTH);
            try {
                System.arraycopy(infoPo.getTxHash().serialize(), 0, infoKey, AddressTool.HASH_LENGTH, infoPo.getTxHash().size());
            } catch (IOException e) {
                Log.info(e.getMessage());
            }
            transactionInfoStorageService.deleteTransactionInfo(infoKey);
        }

        return Result.getSuccess().setData(new Integer(addressCount));
    }
}

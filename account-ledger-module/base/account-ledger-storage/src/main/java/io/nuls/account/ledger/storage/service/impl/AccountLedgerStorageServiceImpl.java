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
import io.nuls.account.ledger.storage.service.AccountLedgerStorageService;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.db.service.BatchOperation;
import io.nuls.db.service.DBService;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.*;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.VarInt;
import io.nuls.ledger.service.LedgerService;
import org.spongycastle.util.Arrays;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Facjas
 * @date 2018/5/10.
 */
@Component
public class AccountLedgerStorageServiceImpl implements AccountLedgerStorageService, InitializingBean {
    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    @Autowired
    private LedgerService ledgerService;

    @Override
    public void afterPropertiesSet() throws NulsException {
        Result result = dbService.createArea(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX);
        if (result.isFailed()) {
            //TODO
        }

        result = dbService.createArea(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_COINDATA);
        if (result.isFailed()) {
            //TODO
        }

        result = dbService.createArea(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX_INDEX);
        if (result.isFailed()) {
            //TODO
        }

    }

    @Override
    public Result saveLocalTx(Transaction tx) {
        if (tx == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        byte[] txHashBytes = new byte[0];
        try {
            txHashBytes = tx.getHash().serialize();
        } catch (IOException e) {
            throw new NulsRuntimeException(e);
        }
        CoinData coinData = tx.getCoinData();
        if (coinData != null) {
            // delete - from
            List<Coin> froms = coinData.getFrom();
            BatchOperation batch = dbService.createWriteBatch(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_COINDATA);
            for (Coin from : froms) {
                byte[] fromSource = from.getOwner();
                byte[] utxoFromSource = new byte[tx.getHash().size()];
                byte[] fromIndex = new byte[fromSource.length - utxoFromSource.length];
                System.arraycopy(fromSource, 0, utxoFromSource, 0, tx.getHash().size());
                System.arraycopy(fromSource, tx.getHash().size(), fromIndex, 0, fromIndex.length);
                Transaction sourceTx = null;
                try {
                    sourceTx = ledgerService.getTx(NulsDigestData.fromDigestHex(Hex.encode(fromSource)));
                } catch (Exception e) {
                    throw new NulsRuntimeException(e);
                }
                if (sourceTx == null) {
                    return Result.getFailed(AccountLedgerErrorCode.SOURCE_TX_NOT_EXSITS);
                }
                byte[] address = sourceTx.getCoinData().getTo().get((int) new VarInt(fromIndex, 0).value).getOwner();
                batch.delete(Arrays.concatenate(address, from.getOwner()));
            }
            // save utxo - to
            List<Coin> tos = coinData.getTo();
            byte[] indexBytes;
            for (int i = 0, length = tos.size(); i < length; i++) {
                try {
                    byte[] outKey = Arrays.concatenate(tos.get(i).getOwner(), tx.getHash().serialize(), new VarInt(i).encode());
                    batch.put(outKey, tos.get(i).serialize());
                } catch (IOException e) {
                    throw new NulsRuntimeException(e);
                }
            }

            Result batchResult = batch.executeBatch();
            if (batchResult.isFailed()) {
                return batchResult;
            }
        }

//        try {
//            result = dbService.put(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX, tx.getHash().serialize(), tx.serialize());
//        } catch (IOException e) {
//            throw new NulsRuntimeException(e);
//        }
        return Result.getSuccess();
    }

    @Override
    public Result deleteLocalTx(Transaction tx) {
        return null;
    }

    @Override
    public Result<Integer> saveLocalTxInfo(TransactionInfoPo infoPo, List<byte[]> addresses) {
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
                dbService.put(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX_INDEX, infoKey, infoPo.serialize());
                savedKeyList.add(infoKey);
            }
        } catch (IOException e) {
            for (int i = 0; i < savedKeyList.size(); i++) {
                dbService.delete(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX_INDEX, savedKeyList.get(i));
            }

            return Result.getFailed(AccountLedgerErrorCode.IO_ERROR);
        }
        return Result.getSuccess().setData(new Integer(addresses.size()));
    }

    @Override
    public Result deleteLocalTxInfo(TransactionInfoPo infoPo) {
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
            dbService.delete(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX_INDEX, infoKey);

        }

        return Result.getSuccess().setData(new Integer(addressCount));
    }

    @Override
    public Transaction getLocalTx(NulsDigestData hash) {
        return null;
    }

    @Override
    public List<Coin> getCoinBytes(byte[] owner) throws NulsException {
        List<Coin> coinList = new ArrayList<>();
        List<byte[]> keyList = dbService.keyList(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_COINDATA);
        byte[] keyOwner = new byte[AddressTool.HASH_LENGTH];
        for (byte[] key : keyList) {
            System.arraycopy(key, 0, keyOwner, 0, AddressTool.HASH_LENGTH);
            if (java.util.Arrays.equals(keyOwner, owner)) {
                Coin coin = new Coin();
                coin.parse(dbService.get(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_COINDATA, key));
                coinList.add(coin);
            }
        }
        return coinList;
    }

    @Override
    public byte[] getTxBytes(byte[] txBytes) {
        return new byte[0];
    }
}

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

import io.nuls.account.ledger.base.service.LocalUtxoService;
import io.nuls.account.ledger.base.util.AccountLegerUtils;
import io.nuls.account.ledger.constant.AccountLedgerErrorCode;
import io.nuls.account.ledger.storage.service.LocalUtxoStorageService;
import io.nuls.account.ledger.storage.service.UnconfirmedTransactionStorageService;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.db.model.Entry;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.*;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.VarInt;
import io.nuls.ledger.service.LedgerService;

import java.io.IOException;
import java.util.*;

/**
 * author Facjas
 * date 2018/5/27.
 */
@Component
public class LocalUtxoServiceImpl implements LocalUtxoService {

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private LocalUtxoStorageService localUtxoStorageService;

    @Autowired
    UnconfirmedTransactionStorageService unconfirmedTransactionStorageService;

    @Override
    public Result saveUtxoForLocalAccount(Transaction tx) {
        List<byte[]> localAddressList = AccountLegerUtils.getLocalAddresses();
        return saveUtxoForAccount(tx, localAddressList);
    }

    @Override
    public Result saveUtxoForAccount(Transaction tx, byte[] addresses) {
        List<byte[]> addressList = new ArrayList<>();
        addressList.add(addresses);
        return saveUtxoForAccount(tx, addressList);
    }

    @Override
    public Result saveUtxoForAccount(Transaction tx, List<byte[]> addressesList) {

        if (tx == null || addressesList == null || addressesList.size() == 0) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }

//        for(byte[] addresses : addressesList) {
//            if (!AddressTool.validAddress(addresses)) {
//                return Result.getFailed(AccountLedgerErrorCode.ADDRESS_ERROR);
//            }
//        }

        CoinData coinData = tx.getCoinData();

        if (coinData != null) {
            // delete - from
            List<Coin> froms = coinData.getFrom();
            List<byte[]> fromsList = new ArrayList<>();
            byte[] fromSource;
            byte[] utxoFromSource;
            byte[] fromIndex;
            Transaction sourceTx;
            Coin fromOfFromCoin;
            for (Coin from : froms) {
                fromSource = from.getOwner();

                fromOfFromCoin = from.getFrom();
                if(fromOfFromCoin == null) {
                    utxoFromSource = new byte[tx.getHash().size()];
                    fromIndex = new byte[fromSource.length - utxoFromSource.length];

                    System.arraycopy(fromSource, 0, utxoFromSource, 0, tx.getHash().size());
                    System.arraycopy(fromSource, tx.getHash().size(), fromIndex, 0, fromIndex.length);

                    try {
                        sourceTx = ledgerService.getTx(NulsDigestData.fromDigestHex(Hex.encode(utxoFromSource)));
                    } catch (Exception e) {
                        continue;
                    }
                    if (sourceTx == null) {
                        return Result.getFailed(AccountLedgerErrorCode.SOURCE_TX_NOT_EXSITS);
                    }
                    int index = (int) new VarInt(fromIndex, 0).value;
                    fromOfFromCoin = sourceTx.getCoinData().getTo().get(index);

                    from.setFrom(fromOfFromCoin);
                }

                if(fromOfFromCoin == null) {
                    Log.warn("from coin not found!");
                    continue;
                }

                byte[] toAddress = fromOfFromCoin.getOwner();

                boolean addressIsMatch = false;
                for(byte[] addresses : addressesList) {
                    if(Arrays.equals(toAddress, addresses)) {
                        addressIsMatch = true;
                        break;
                    }
                }
                if(!addressIsMatch) {
                    continue;
                }

                fromsList.add(fromSource);
            }

            // save utxo - to
            List<Coin> tos = coinData.getTo();
            List<Entry<byte[], byte[]>> toList = new ArrayList<>();
            byte[] txHashBytes = null;
            try {
                txHashBytes = tx.getHash().serialize();
            } catch (IOException e) {
                throw new NulsRuntimeException(e);
            }
            byte[] outKey;
            Coin to;
            byte[] toAddress;
            for (int i = 0, length = tos.size(); i < length; i++) {
                to = tos.get(i);
                toAddress = to.getOwner();

                boolean addressIsMatch = false;
                for(byte[] addresses : addressesList) {
                    if(Arrays.equals(toAddress, addresses)) {
                        addressIsMatch = true;
                        break;
                    }
                }
                if(!addressIsMatch) {
                    continue;
                }

                try {
                    outKey = ArraysTool.concatenate(txHashBytes, new VarInt(i).encode());
                    toList.add(new Entry<>(outKey, to.serialize()));
                } catch (IOException e) {
                    Log.error(e);
                }
            }
            Result result = localUtxoStorageService.batchSaveAndDeleteUTXO(toList, fromsList);
            if (result.isFailed() || result.getData() == null || (int) result.getData() != toList.size() + fromsList.size()) {
                return Result.getFailed();
            }
        }
        return Result.getSuccess();
    }

    @Override
    public Result deleteUtxoOfTransaction(Transaction tx) {
        if (tx == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }

        CoinData coinData = tx.getCoinData();
        if (coinData != null) {
            // delete utxo - to
            List<Coin> tos = coinData.getTo();
            List<byte[]> toList = new ArrayList<>();
            byte[] outKey;
            for (int i = 0, length = tos.size(); i < length; i++) {
                try {
                    if(!AccountLegerUtils.isLocalAccount(tos.get(i).getOwner())) {
                        continue;
                    }
                    outKey = ArraysTool.concatenate(tx.getHash().serialize(), new VarInt(i).encode());
                    toList.add(outKey);
                } catch (IOException e) {
                    throw new NulsRuntimeException(e);
                }
            }

            // save - from
            List<Coin> froms = coinData.getFrom();
            List<Entry<byte[], byte[]>> fromList = new ArrayList<>();

            byte[] fromSource;
            Coin fromOfFromCoin;
            byte[] utxoFromSource;
            byte[] fromIndex;
            Transaction sourceTx;
            byte[] address;
            for (Coin from : froms) {
                fromSource = from.getOwner();

                fromOfFromCoin = from.getFrom();
                if(fromOfFromCoin == null) {
                    utxoFromSource = new byte[tx.getHash().size()];
                    fromIndex = new byte[fromSource.length - utxoFromSource.length];
                    System.arraycopy(fromSource, 0, utxoFromSource, 0, tx.getHash().size());
                    System.arraycopy(fromSource, tx.getHash().size(), fromIndex, 0, fromIndex.length);

                    try {
                        sourceTx = ledgerService.getTx(NulsDigestData.fromDigestHex(Hex.encode(utxoFromSource)));
                    } catch (Exception e) {
                        continue;
                    }
                    if (sourceTx == null) {
                        return Result.getFailed(AccountLedgerErrorCode.SOURCE_TX_NOT_EXSITS);
                    }
                    fromOfFromCoin = sourceTx.getCoinData().getTo().get((int) new VarInt(fromIndex, 0).value);

                    from.setFrom(fromOfFromCoin);
                }

                if(fromOfFromCoin == null) {
                    Log.warn("from coin not found!");
                    continue;
                }

                address = fromOfFromCoin.getOwner();
                if(!AccountLegerUtils.isLocalAccount(address)) {
                    continue;
                }
                try {
                    fromList.add(new Entry<>(fromSource, fromOfFromCoin.serialize()));
                } catch (IOException e) {
                    throw new NulsRuntimeException(e);
                }
            }

            Result result = localUtxoStorageService.batchSaveAndDeleteUTXO(fromList, toList);
            if (result.isFailed() || result.getData() == null || (int) result.getData() != fromList.size() + toList.size()) {
                return Result.getFailed();
            }
        }

        return Result.getSuccess();
    }

    @Override
    public Result<List<byte[]>> unlockCoinData(Transaction tx, long newLockTime) {
        List<byte[]> addresses = new ArrayList<>();
        CoinData coinData = tx.getCoinData();
        if (coinData != null) {
            List<Coin> tos = coinData.getTo();
            Coin to;
            for (int i = 0, length = tos.size(); i < length; i++) {
                to = tos.get(i);
                if (to.getLockTime() == -1L) {
                    Coin needUnLockUtxoNew = new Coin(to.getOwner(), to.getNa(), newLockTime);
                    needUnLockUtxoNew.setFrom(to.getFrom());
                    try {
                        byte[] outKey = ArraysTool.concatenate(tx.getHash().serialize(), new VarInt(i).encode());
                        saveUTXO(outKey, needUnLockUtxoNew.serialize());
                        addresses.add(to.getOwner());
                    } catch (IOException e) {
                        throw new NulsRuntimeException(e);
                    }
                    //todo , think about weather to add a transaction history
                    break;
                }
            }
        }
        return Result.getSuccess().setData(addresses);
    }

    @Override
    public Result<List<byte[]>> rollbackUnlockTxCoinData(Transaction tx) {
        List<byte[]> addresses = new ArrayList<>();
        CoinData coinData = tx.getCoinData();
        if (coinData != null) {
            // lock utxo - to
            List<Coin> tos = coinData.getTo();
            for (int i = 0, length = tos.size(); i < length; i++) {
                Coin to = tos.get(i);
                if (to.getLockTime() == -1L) {
                    try {
                        byte[] outKey = ArraysTool.concatenate(tx.getHash().serialize(), new VarInt(i).encode());
                        saveUTXO(outKey, to.serialize());
                        addresses.add(to.getOwner());
                    } catch (IOException e) {
                        throw new NulsRuntimeException(e);
                    }
                    break;
                }
            }
        }
        return Result.getSuccess().setData(addresses);
    }

    protected void saveUTXO(byte[] outKey, byte[] serialize) {
        localUtxoStorageService.saveUTXO(outKey, serialize);
    }



    @Override
    public Result getUtxo(byte[] owner) {
        if (owner == null) {
            return null;
        }
        Coin coin = localUtxoStorageService.getUtxo(owner);
        if (coin == null) {
            return Result.getFailed();
        }
        return Result.getSuccess().setData(coin);
    }
}

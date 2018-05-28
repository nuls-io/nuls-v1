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
        if (tx == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }

        CoinData coinData = tx.getCoinData();

        if (coinData != null) {
            // delete - from
            List<Coin> froms = coinData.getFrom();
            Set<byte[]> fromsSet = new HashSet<>();
            for (Coin from : froms) {
                byte[] fromSource = from.getOwner();
                byte[] utxoFromSource = new byte[tx.getHash().size()];
                byte[] fromIndex = new byte[fromSource.length - utxoFromSource.length];
                System.arraycopy(fromSource, 0, utxoFromSource, 0, tx.getHash().size());
                System.arraycopy(fromSource, tx.getHash().size(), fromIndex, 0, fromIndex.length);

                Coin fromOfFromCoin = from.getFrom();

                if(fromOfFromCoin == null) {
                    Transaction sourceTx = null;
                    try {
                        sourceTx = ledgerService.getTx(NulsDigestData.fromDigestHex(Hex.encode(fromSource)));
                        if (sourceTx == null) {
                            sourceTx = unconfirmedTransactionStorageService.getUnconfirmedTx(NulsDigestData.fromDigestHex(Hex.encode(fromSource))).getData();
                        }
                    } catch (Exception e) {
                        throw new NulsRuntimeException(e);
                    }
                    if (sourceTx == null) {
                        return Result.getFailed(AccountLedgerErrorCode.SOURCE_TX_NOT_EXSITS);
                    }
                    fromOfFromCoin = sourceTx.getCoinData().getTo().get((int) new VarInt(fromIndex, 0).value);
                }

                byte[] address = fromOfFromCoin.getOwner();

                if (!AccountLegerUtils.isLocalAccount(address)) {
                    continue;
                }

                fromsSet.add(ArraysTool.joinintTogether(address, from.getOwner()));
            }

            Result result = localUtxoStorageService.batchDeleteUTXO(fromsSet);
            if(!result.isSuccess() || result.getData() == null || ((Integer) result.getData()) != fromsSet.size()) {
                return result;
            }

            // save utxo - to
            List<Coin> tos = coinData.getTo();
            Map<byte[], byte[]> toMap = new HashMap<>();
            for (int i = 0, length = tos.size(); i < length; i++) {
                Coin to = tos.get(i);
                if (!AccountLegerUtils.isLocalAccount(to.getOwner())) {
                    continue;
                }

                try {
                    byte[] outKey = ArraysTool.joinintTogether(tos.get(i).getOwner(), tx.getHash().serialize(), new VarInt(i).encode());

                    toMap.put(outKey, to.serialize());
                } catch (IOException e) {
                    throw new NulsRuntimeException(e);
                }
            }
            localUtxoStorageService.batchSaveUTXO(toMap);
        }
        return Result.getSuccess();
    }

    @Override
    public Result saveUtxoForAccount(Transaction tx, byte[] addresses) {
        if(AddressTool.validAddress(addresses)){
            return Result.getFailed(AccountLedgerErrorCode.ADDRESS_ERROR);
        }
        if (tx == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }

        CoinData coinData = tx.getCoinData();

        if (coinData != null) {
            // delete - from
            List<Coin> froms = coinData.getFrom();
            Set<byte[]> fromsSet = new HashSet<>();
            for (Coin from : froms) {
                byte[] fromSource = from.getOwner();
                byte[] utxoFromSource = new byte[tx.getHash().size()];
                byte[] fromIndex = new byte[fromSource.length - utxoFromSource.length];
                System.arraycopy(fromSource, 0, utxoFromSource, 0, tx.getHash().size());
                System.arraycopy(fromSource, tx.getHash().size(), fromIndex, 0, fromIndex.length);
                Transaction sourceTx = null;
                try {
                    sourceTx = ledgerService.getTx(NulsDigestData.fromDigestHex(Hex.encode(fromSource)));
                    if (sourceTx == null) {
                        sourceTx = unconfirmedTransactionStorageService.getUnconfirmedTx(NulsDigestData.fromDigestHex(Hex.encode(fromSource))).getData();
                    }
                } catch (Exception e) {
                    throw new NulsRuntimeException(e);
                }
                if (sourceTx == null) {
                    return Result.getFailed(AccountLedgerErrorCode.SOURCE_TX_NOT_EXSITS);
                }
                byte[] address = sourceTx.getCoinData().getTo().get((int) new VarInt(fromIndex, 0).value).getOwner();
                fromsSet.add(ArraysTool.joinintTogether(address, from.getOwner()));
            }

            localUtxoStorageService.batchDeleteUTXO(fromsSet);
            // save utxo - to
            List<Coin> tos = coinData.getTo();
            Map<byte[], byte[]> toMap = new HashMap<>();
            for (int i = 0, length = tos.size(); i < length; i++) {
                Coin to = tos.get(i);

                if (!Arrays.equals(to.getOwner(), addresses)) {
                    continue;
                }

                try {
                    byte[] outKey = ArraysTool.joinintTogether(tos.get(i).getOwner(), tx.getHash().serialize(), new VarInt(i).encode());
                    if (to.getLockTime() == -1L) {
                        byte[] toKey = ArraysTool.joinintTogether(tx.getHash().serialize(), new VarInt(i).encode());

                        Coin ledgerCoin = ledgerService.getUtxo(toKey);
                        if (null != ledgerCoin) {
                            to = ledgerCoin;
                        }
                    }
                    toMap.put(outKey, to.serialize());
                } catch (IOException e) {
                    throw new NulsRuntimeException(e);
                }
            }
            localUtxoStorageService.batchSaveUTXO(toMap);
        }
        return Result.getSuccess();
    }

    @Override
    public Result deleteUtxoOfTransaction(Transaction tx) {
        if (tx == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        byte[] txHashBytes = new byte[0];

        CoinData coinData = tx.getCoinData();
        if (coinData != null) {
            // delete utxo - to
            List<Coin> tos = coinData.getTo();
            byte[] indexBytes;
            Set<byte[]> toSet = new HashSet<>();
            for (int i = 0, length = tos.size(); i < length; i++) {
                try {
                    byte[] outKey = ArraysTool.joinintTogether(tos.get(i).getOwner(), tx.getHash().serialize(), new VarInt(i).encode());
                    toSet.add(outKey);
                } catch (IOException e) {
                    throw new NulsRuntimeException(e);
                }
            }
            localUtxoStorageService.batchDeleteUTXO(toSet);

            // save - from
            List<Coin> froms = coinData.getFrom();
            Map<byte[], byte[]> fromMap = new HashMap<>();
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
                    continue;
                }
                if (sourceTx == null) {
                    return Result.getFailed(AccountLedgerErrorCode.SOURCE_TX_NOT_EXSITS);
                }
                byte[] address = sourceTx.getCoinData().getTo().get((int) new VarInt(fromIndex, 0).value).getOwner();
                try {
                    fromMap.put(ArraysTool.joinintTogether(address, from.getOwner()), sourceTx.getCoinData().getTo().get((int) new VarInt(fromIndex, 0).value).serialize());
                } catch (IOException e) {
                    throw new NulsRuntimeException(e);
                }
            }
            localUtxoStorageService.batchSaveUTXO(fromMap);
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
                        byte[] outKey = ArraysTool.joinintTogether(to.getOwner(), tx.getHash().serialize(), new VarInt(i).encode());
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
                        byte[] outKey = ArraysTool.joinintTogether(to.getOwner(), tx.getHash().serialize(), new VarInt(i).encode());
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
        localUtxoStorageService.saveUTXO(outKey,serialize);
    }
}

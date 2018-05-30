/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.ledger.service.impl;

import io.nuls.core.tools.calc.LongUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.map.MapUtil;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.db.service.BatchOperation;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.VarInt;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.service.LedgerService;
import io.nuls.ledger.storage.service.UtxoLedgerTransactionStorageService;
import io.nuls.ledger.storage.service.UtxoLedgerUtxoStorageService;
import io.nuls.ledger.utils.LedgerUtil;
import org.spongycastle.util.Arrays;

import java.io.IOException;
import java.util.*;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/5/8
 */
@Service
public class UtxoLedgerServiceImpl implements LedgerService {

    private final static String CLASS_NAME = UtxoLedgerServiceImpl.class.getName();

    @Autowired
    private UtxoLedgerUtxoStorageService utxoLedgerUtxoStorageService;
    @Autowired
    private UtxoLedgerTransactionStorageService utxoLedgerTransactionStorageService;

    @Override
    public Result saveTx(Transaction tx) throws NulsException {
        if (tx == null) {
            return Result.getFailed(LedgerErrorCode.NULL_PARAMETER);
        }
        try {
            // 保存CoinData
            Result result = saveCoinData(tx);
            if (result.isFailed()) {
                Result rollbackResult = rollbackCoinData(tx);
                if (rollbackResult.isFailed()) {
                    throw new NulsException(LedgerErrorCode.DB_ROLLBACK_ERROR, CLASS_NAME + ".saveTx.rollbackCoinData: data error.");
                }
                return result;
            }
            // 保存交易
            result = utxoLedgerTransactionStorageService.saveTx(tx);
            if (result.isFailed()) {
                Result rollbackResult = rollbackTx(tx);
                if (rollbackResult.isFailed()) {
                    throw new NulsException(LedgerErrorCode.DB_ROLLBACK_ERROR, CLASS_NAME + ".saveTx.rollbackTx: data error.");
                }
            }
            return result;
        } catch (IOException e) {
            Log.error(e);
            Result rollbackResult = rollbackTx(tx);
            if (rollbackResult.isFailed()) {
                throw new NulsException(LedgerErrorCode.DB_ROLLBACK_ERROR, CLASS_NAME + ".saveTx: data error.");
            }
            return Result.getFailed(e.getMessage());
        }
    }

    private Result saveCoinData(Transaction tx) throws IOException {
        byte[] txHashBytes = tx.getHash().serialize();
        BatchOperation batch = utxoLedgerUtxoStorageService.createWriteBatch();
        CoinData coinData = tx.getCoinData();
        //TestLog+
//        Log.info("=============="+tx.getClass().getSimpleName()+"交易：hash-"+tx.getHash().getDigestHex());
        //TestLog-
        if (coinData != null) {
            // 删除utxo已花费 - from
            List<Coin> froms = coinData.getFrom();
            for (Coin from : froms) {
                //TestLog+
//                Coin preFrom = utxoLedgerUtxoStorageService.getUtxo(from.getOwner());
//                if(preFrom != null)
//                    Log.info("=============="+tx.getClass().getSimpleName()+"花费：address-"+ Base58.encode(preFrom.getOwner())+", amount-"+preFrom.getNa().getValue());
                //TestLog-
                batch.delete(from.getOwner());
            }
            // 保存utxo - to
            List<Coin> tos = coinData.getTo();
            byte[] indexBytes;
            for (int i = 0, length = tos.size(); i < length; i++) {
                try {
                    batch.put(Arrays.concatenate(txHashBytes, new VarInt(i).encode()), tos.get(i).serialize());
                } catch (IOException e) {
                    Log.error(e);
                    return Result.getFailed(e.getMessage());
                }
            }
            // 执行批量
            Result batchResult = batch.executeBatch();
            if (batchResult.isFailed()) {
                return batchResult;
            }
        }
        return Result.getSuccess();
    }

    @Override
    public Result rollbackTx(Transaction tx) throws NulsException {
        if (tx == null) {
            return Result.getFailed(LedgerErrorCode.NULL_PARAMETER);
        }
        try {
            // 回滚CoinData
            Result result = rollbackCoinData(tx);
            if (result.isFailed()) {
                Result recoveryResult = saveCoinData(tx);
                if (recoveryResult.isFailed()) {
                    throw new NulsException(LedgerErrorCode.DB_DATA_ERROR, CLASS_NAME + ".rollbackTx.saveCoinData: data error.");
                }
                return result;
            }
            // 回滚交易
            result = utxoLedgerTransactionStorageService.deleteTx(tx);
            if (result.isFailed()) {
                Result recoveryResult = saveTx(tx);
                if (recoveryResult.isFailed()) {
                    throw new NulsException(LedgerErrorCode.DB_DATA_ERROR, CLASS_NAME + ".rollbackTx.saveTx: data error.");
                }
            }
            return result;
        } catch (IOException e) {
            Log.error(e);
            Result rollbackResult = saveTx(tx);
            if (rollbackResult.isFailed()) {
                throw new NulsException(LedgerErrorCode.DB_DATA_ERROR, CLASS_NAME + ".rollbackTx.saveTx: data error.");
            }
            return Result.getFailed(e.getMessage());
        }
    }

    private Result rollbackCoinData(Transaction tx) throws IOException, NulsException {
        byte[] txHashBytes = tx.getHash().serialize();
        BatchOperation batch = utxoLedgerUtxoStorageService.createWriteBatch();
        CoinData coinData = tx.getCoinData();
        if (coinData != null) {
            // 保存utxo已花费 - from
            List<Coin> froms = coinData.getFrom();
            Coin recovery;
            for (Coin from : froms) {
                try {
                    byte[] hashBytes = new byte[NulsDigestData.HASH_LENGTH];
                    System.arraycopy(from.getOwner(), 0, hashBytes, 0, hashBytes.length);
                    NulsDigestData fromTxHash = new NulsDigestData();
                    fromTxHash.parse(hashBytes);

                    byte[] indexBytes = new byte[from.getOwner().length - NulsDigestData.HASH_LENGTH];
                    System.arraycopy(from.getOwner(), NulsDigestData.HASH_LENGTH, indexBytes, 0, indexBytes.length);
                    int fromIndex = (int) new VarInt(indexBytes, 0).value;

                    Transaction fromTx = utxoLedgerTransactionStorageService.getTx(fromTxHash);
                    recovery = fromTx.getCoinData().getTo().get(fromIndex);
                    recovery.setFrom(from.getFrom());
                    batch.put(from.getOwner(), recovery.serialize());
                } catch (IOException e) {
                    Log.error(e);
                    return Result.getFailed(e.getMessage());
                }
            }
            // 删除utxo - to
            List<Coin> tos = coinData.getTo();
            for (int i = 0, length = tos.size(); i < length; i++) {
                batch.delete(Arrays.concatenate(txHashBytes, new VarInt(i).encode()));
            }
            // 执行批量
            Result batchResult = batch.executeBatch();
            if (batchResult.isFailed()) {
                return batchResult;
            }
        }
        return Result.getSuccess();
    }

    @Override
    public Transaction getTx(NulsDigestData hash) {
        if (hash == null) {
            return null;
        }
        return utxoLedgerTransactionStorageService.getTx(hash);
    }

    @Override
    public Transaction getTx(byte[] txHashBytes) {
        if (txHashBytes == null) {
            return null;
        }
        NulsDigestData digestData = new NulsDigestData();
        try {
            digestData.parse(txHashBytes);
        } catch (Exception e) {
            return null;
        }
        return getTx(digestData);
    }

    private boolean checkPublicKeyHash(byte[] address, byte[] pubKeyHash) {
        if (address == null || pubKeyHash == null) {
            return false;
        }
        int pubKeyHashLength = pubKeyHash.length;
        if (address.length != AddressTool.HASH_LENGTH || pubKeyHashLength != 20) {
            return false;
        }
        for (int i = 0; i < pubKeyHashLength; i++) {
            if (pubKeyHash[i] != address[i + 2]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 此txList是待打包的块中的交易，所以toList是下一步的UTXO，应该校验它
     * coinData的交易和txList同处一个块中，txList中的to可能是coinData的from，
     * 也就是可能存在，在同一个块中，下一笔输入就是上一笔的输出，所以需要校验它
     *
     * @param transaction
     * @param txList
     * @return ValidateResult
     */
    @Override
    public ValidateResult verifyCoinData(Transaction transaction, List<Transaction> txList) {
        if (transaction == null || transaction.getCoinData() == null) {
            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.NULL_PARAMETER);
        }
        boolean isEmptyTxList = (txList == null || txList.size() == 0);
        try {
            CoinData coinData = transaction.getCoinData();
            int initialToUtxoCapacity = 0,
            initialFromUtxoCapacity = 0;

            // 与txList相关的校验准备数据
            Map<String, Coin> validateToUtxoMap = null;
            Set<String> validateFromUtxoSet = null;
            if(!isEmptyTxList) {
                CoinData validateCoinData;
                // 计算HashMap容量
                for (Transaction tx : txList) {
                    validateCoinData = tx.getCoinData();
                    if (validateCoinData == null) {
                        continue;
                    }
                    // 此txList是待打包的块中的交易，所以toList是下一步的UTXO，应该校验它
                    initialToUtxoCapacity += validateCoinData.getTo().size();
                    // fromUtxoSet用于校验是否双花，既是待校验交易的fromUtxo是否和txList中的fromUtxo重复，有重复则是双花
                    initialFromUtxoCapacity += validateCoinData.getFrom().size();
                }
                initialToUtxoCapacity = MapUtil.tableSizeFor(initialToUtxoCapacity) << 1;
                initialFromUtxoCapacity = MapUtil.tableSizeFor(initialFromUtxoCapacity) << 1;
                // txList中所有的to存放于HashMap中
                validateToUtxoMap = new HashMap<>(initialToUtxoCapacity);
                // txList中所有的from.owner存放于HashSet中
                validateFromUtxoSet = new HashSet<>(initialFromUtxoCapacity);
                Transaction tx;
                byte[] txHashBytes;
                Coin toOfValidate;
                List<Coin> validateToList;
                List<Coin> validateFromList;
                for (int i = 0, length = txList.size(); i < length; i++) {
                    tx = txList.get(i);
                    validateCoinData = tx.getCoinData();
                    if (validateCoinData == null) {
                        continue;
                    }
                    // 此txList是待打包的块中的交易，所以toList是下一步的UTXO，应该校验它
                    txHashBytes = tx.getHash().serialize();
                    validateToList = validateCoinData.getTo();
                    for (int k = 0, toLength = validateToList.size(); k < toLength; k++) {
                        toOfValidate = validateToList.get(k);
                        validateToUtxoMap.put(asString(Arrays.concatenate(txHashBytes, new VarInt(k).encode())), toOfValidate);
                    }
                    // fromUtxoSet用于校验是否双花，既是待校验交易的fromUtxo是否和txList中的fromUtxo重复，有重复则是双花
                    validateFromList = validateCoinData.getFrom();
                    for (int j = 0, fromLength = validateFromList.size(); j < fromLength; j++) {
                        validateFromUtxoSet.add(asString(validateFromList.get(j).getOwner()));
                    }
                }
            }

            /*
            校验开始
             */

            List<Coin> froms = coinData.getFrom();
            int fromSize = froms.size();
            P2PKHScriptSig p2PKHScriptSig = null;
            // 公钥hash160
            byte[] user = null;
            // 获取交易的公钥及签名脚本
            if (fromSize > 0) {
                try {
                    p2PKHScriptSig = P2PKHScriptSig.createFromBytes(transaction.getScriptSig());
                    user = p2PKHScriptSig.getSignerHash160();
                } catch (NulsException e) {
                    return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.DATA_ERROR);
                }
            }
            // 保存Set用于验证自身双花
            int initialSetCapacity = MapUtil.tableSizeFor(fromSize) << 1;
            HashSet<String> set = new HashSet<>(initialSetCapacity);
            Na fromTotal = Na.ZERO;
            byte[] fromBytes;
            // 保存在数据库中或者txList中的utxo数据
            Coin fromOfFromCoin = null;
            byte[] fromAdressBytes = null;
            for (Coin from : froms) {
                fromBytes = from.getOwner();
                // 验证是否可花费, 校验的coinData的fromUTXO，检查数据库中是否存在此UTXO
                //Log.info("getUTXO: hash-" + LedgerUtil.getTxHash(fromBytes) + ", index-" + LedgerUtil.getIndex(fromBytes));
                fromOfFromCoin = utxoLedgerUtxoStorageService.getUtxo(fromBytes);
                // 检查txList中是否存在此UTXO
                if (!isEmptyTxList && fromOfFromCoin == null ) {
                    fromOfFromCoin = validateToUtxoMap.get(asString(fromBytes));
                }
                if (null == fromOfFromCoin) {
                    // 如果既不存在于txList的to中(如果txList不为空)，又不存在于数据库中，那么这是一笔问题数据，进一步检查是否存在这笔交易，交易有就是双花，没有就是孤儿交易，则返回失败
                    if (null != utxoLedgerTransactionStorageService.getTxBytes(LedgerUtil.getTxHashBytes(fromBytes))) {
                        return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.LEDGER_DOUBLE_SPENT, "no UTXO in DB&&txList, had one tx.");
                    } else {
                        return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.ORPHAN_TX);
                    }
                } else {
                    fromAdressBytes = fromOfFromCoin.getOwner();
                    // 验证地址中的公钥hash160和交易中的公钥hash160是否相等，不相等则说明这笔utxo不属于交易发出者
                    if (!checkPublicKeyHash(fromAdressBytes, user)) {
                        Log.warn("public key hash160 check error.");
                        return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.INVALID_INPUT);
                    }
                }
                // 验证非解锁类型的交易及解锁类型的交易
                if (!transaction.isUnlockTx()) {
                    // 验证非解锁类型的交易，验证是否可用，检查是否还在锁定时间内
                    if (!fromOfFromCoin.usable()) {
                        return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.UTXO_UNUSABLE);
                    }
                } else {
                    // 验证解锁类型的交易
                    if (fromOfFromCoin.getLockTime() != -1) {
                        return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.UTXO_STATUS_CHANGE);
                    }
                }

                // 验证自身双花
                if (!set.add(asString(fromBytes))) {
                    return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.LEDGER_DOUBLE_SPENT, "duplicate utxo in itself.");
                }
                // 验证与待确认交易列表中是否有双花，既是待校验交易的fromUtxo是否和txList中的fromUtxo重复，有重复则是双花
                if (!isEmptyTxList && validateFromUtxoSet.contains(asString(fromBytes))) {
                    return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.LEDGER_DOUBLE_SPENT, "duplicate utxo in tx and txList.");
                }
                fromTotal = fromTotal.add(fromOfFromCoin.getNa());
                from.setFrom(fromOfFromCoin);
            }
            List<Coin> tos = coinData.getTo();
            Na toTotal = Na.ZERO;
            for (Coin to : tos) {
                toTotal = toTotal.add(to.getNa());
            }
            // 验证输出不能大于输入
            if (fromTotal.compareTo(toTotal) < 0) {
                return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.INVALID_AMOUNT);
            }

        } catch (Exception e) {
            Log.error(e);
            return ValidateResult.getFailedResult(CLASS_NAME, e.getMessage());
        }
        return ValidateResult.getSuccessResult();
    }

    /**
     * 双花验证不通过的交易需要放入result的data中，一次只验证一对双花的交易
     *
     * @param block
     * @return ValidateResult<List<Transaction>>
     */
    @Override
    public ValidateResult<List<Transaction>> verifyDoubleSpend(Block block) {
        if (block == null) {
            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.NULL_PARAMETER);
        }
        return verifyDoubleSpend(block.getTxs());
    }

    /**
     * 双花验证不通过的交易需要放入result的data中，一次只验证一对双花的交易
     *
     * @param txList
     * @return ValidateResult<List<Transaction>>
     */
    @Override
    public ValidateResult<List<Transaction>> verifyDoubleSpend(List<Transaction> txList) {
        if (txList == null) {
            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.NULL_PARAMETER);
        }
        // 计算HashMap容量
        int initialCapacity = 0;
        CoinData coinData;
        for (Transaction tx : txList) {
            coinData = tx.getCoinData();
            if (coinData == null) {
                continue;
            }
            initialCapacity += tx.getCoinData().getFrom().size();
        }
        initialCapacity = MapUtil.tableSizeFor(initialCapacity) << 1;
        HashMap<String, Transaction> fromMap = new HashMap<>(initialCapacity);
        List<Coin> froms;
        Transaction prePutTx;
        // 判断是否有重复的fromCoin存在，如果存在，则是双花
        for (Transaction tx : txList) {
            coinData = tx.getCoinData();
            if (coinData == null) {
                continue;
            }
            froms = coinData.getFrom();
            for (Coin from : froms) {
                prePutTx = fromMap.put(asString(from.getOwner()), tx);
                // 不为空则代表此coin在map中已存在，则是双花
                if (prePutTx != null) {
                    List<Transaction> resultList = new ArrayList<>(2);
                    resultList.add(prePutTx);
                    resultList.add(tx);
                    ValidateResult validateResult = ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.LEDGER_DOUBLE_SPENT);
                    validateResult.setData(resultList);
                    return validateResult;
                }
            }
        }
        return ValidateResult.getSuccessResult();
    }

    private String asString(byte[] bytes) {
        AssertUtil.canNotEmpty(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Override
    public Result unlockTxCoinData(Transaction tx, long newockTime) throws NulsException {
        if (tx == null || tx.getCoinData() == null) {
            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.NULL_PARAMETER);
        }
        try {
            CoinData coinData = tx.getCoinData();
            List<Coin> tos = coinData.getTo();
            boolean isExistLockUtxo = false;
            Coin needUnLockUtxo = null;
            int needUnLockUtxoIndex = 0;
            for (Coin to : tos) {
                if (to.getLockTime() == -1) {
                    isExistLockUtxo = true;
                    needUnLockUtxo = to;
                    break;
                }
                needUnLockUtxoIndex++;
            }
            if (!isExistLockUtxo) {
                return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.UTXO_STATUS_CHANGE);
            }
            byte[] txHashBytes = txHashBytes = tx.getHash().serialize();
            Coin needUnLockUtxoNew = new Coin(needUnLockUtxo.getOwner(), needUnLockUtxo.getNa(), newockTime);
            needUnLockUtxoNew.setFrom(needUnLockUtxo.getFrom());
            Result result = utxoLedgerUtxoStorageService.saveUtxo(Arrays.concatenate(txHashBytes, new VarInt(needUnLockUtxoIndex).encode()), needUnLockUtxoNew);
            if (result.isFailed()) {
                Result rollbackResult = rollbackUnlockTxCoinData(tx);
                if (rollbackResult.isFailed()) {
                    throw new NulsException(LedgerErrorCode.DB_ROLLBACK_ERROR, CLASS_NAME + ".unlockTxCoinData.saveUtxo: data error.");
                }
            }
            return result;
        } catch (IOException e) {
            Result rollbackResult = rollbackUnlockTxCoinData(tx);
            if (rollbackResult.isFailed()) {
                throw new NulsException(LedgerErrorCode.DB_ROLLBACK_ERROR, CLASS_NAME + ".unlockTxCoinData.rollbackUnlockTxCoinData: data error.");
            }
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }
    }

    @Override
    public Result rollbackUnlockTxCoinData(Transaction tx) throws NulsException {
        if (tx == null || tx.getCoinData() == null) {
            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.NULL_PARAMETER);
        }
        try {
            CoinData coinData = tx.getCoinData();
            List<Coin> tos = coinData.getTo();
            boolean isExistLockUtxo = false;
            Coin needUnLockUtxo = null;
            int needUnLockUtxoIndex = 0;
            for (Coin to : tos) {
                if (to.getLockTime() == -1) {
                    isExistLockUtxo = true;
                    needUnLockUtxo = to;
                    break;
                }
                needUnLockUtxoIndex++;
            }
            if (!isExistLockUtxo) {
                return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.UTXO_STATUS_CHANGE);
            }
            byte[] txHashBytes = tx.getHash().serialize();
            Result result = utxoLedgerUtxoStorageService.saveUtxo(Arrays.concatenate(txHashBytes, new VarInt(needUnLockUtxoIndex).encode()), needUnLockUtxo);
            if (result.isFailed()) {
                throw new NulsException(LedgerErrorCode.DB_ROLLBACK_ERROR, CLASS_NAME + ".rollbackUnlockTxCoinData: data error.");
            }
            return result;
        } catch (IOException e) {
            Log.error(e);
            throw new NulsException(LedgerErrorCode.DB_ROLLBACK_ERROR, CLASS_NAME + ".rollbackUnlockTxCoinData: data error.");
        }
    }

    @Override
    public long getWholeUTXO() {
        long result = 0L;
        List<byte[]> list = utxoLedgerUtxoStorageService.getAllUtxoBytes();
        if (list == null || list.size() == 0) {
            return result;
        }
        Coin coin = null;
        try {
            for (byte[] utxoBytes : list) {
                if (utxoBytes != null) {
                    coin = new Coin();
                    coin.parse(utxoBytes);
                    result = LongUtils.add(result, coin.getNa().getValue());
                }
            }
            return result;
        } catch (NulsException e) {
            Log.error(e);
            return 0L;
        }
    }

    @Override
    public Coin getUtxo(byte[] owner) {
        if (owner == null) {
            return null;
        }
        return utxoLedgerUtxoStorageService.getUtxo(owner);
    }
}

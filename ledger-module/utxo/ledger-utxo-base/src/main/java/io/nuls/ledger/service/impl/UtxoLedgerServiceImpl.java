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
package io.nuls.ledger.service.impl;

import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.service.ContractService;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.calc.LongUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.map.MapUtil;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.db.model.Entry;
import io.nuls.db.service.BatchOperation;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.*;
import io.nuls.kernel.script.P2PHKSignature;
import io.nuls.kernel.script.Script;
import io.nuls.kernel.script.SignatureUtil;
import io.nuls.kernel.script.TransactionSignature;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.SerializeUtils;
import io.nuls.kernel.utils.VarInt;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.service.LedgerService;
import io.nuls.ledger.storage.service.UtxoLedgerTransactionStorageService;
import io.nuls.ledger.storage.service.UtxoLedgerUtxoStorageService;
import io.nuls.ledger.util.LedgerUtil;
import org.spongycastle.util.Arrays;

import java.io.IOException;
import java.util.*;

/**
 * @author: PierreLuo
 */
@Service
public class UtxoLedgerServiceImpl implements LedgerService {

    private final static String CLASS_NAME = UtxoLedgerServiceImpl.class.getName();

    @Autowired
    private UtxoLedgerUtxoStorageService utxoLedgerUtxoStorageService;
    @Autowired
    private UtxoLedgerTransactionStorageService utxoLedgerTransactionStorageService;
    @Autowired
    private ContractService contractService;

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
                    throw new NulsException(rollbackResult.getErrorCode());
                }
                return result;
            }
            // 保存交易
            result = utxoLedgerTransactionStorageService.saveTx(tx);
            if (result.isFailed()) {
                Result rollbackResult = rollbackTx(tx);
                if (rollbackResult.isFailed()) {
                    throw new NulsException(rollbackResult.getErrorCode());
                }
            }
            return result;
        } catch (IOException e) {
            Log.error(e);
            Result rollbackResult = rollbackTx(tx);
            if (rollbackResult.isFailed()) {
                throw new NulsException(rollbackResult.getErrorCode());
            }
            return Result.getFailed(KernelErrorCode.IO_ERROR);
        }
    }

    private Result saveCoinData(Transaction tx) throws IOException {
        CoinData coinData = tx.getCoinData();
        //TestLog+
//        Log.info("=============="+tx.getClass().getSimpleName()+"交易：hash-"+tx.getHash().getDigestHex());
        //TestLog-
        if (coinData != null) {
            BatchOperation batch = utxoLedgerUtxoStorageService.createWriteBatch();
            // 删除utxo已花费 - from
            List<Coin> froms = coinData.getFrom();
            for (Coin from : froms) {
                //TestLog+
//                Coin preFrom = utxoLedgerUtxoStorageService.getUtxo(from.());
//                if (preFrom != null) {
//                    Log.info("花费：height: +" + tx.getBlockHeight() + ", “+txHash-" + tx.getHash() + ", " + Hex.encode(from.()));
//                }
//                Log.info("delete utxo:" + Hex.encode(from.()));
                //TestLog-
                batch.delete(from.getOwner());
            }
            // 保存utxo - to
            byte[] txHashBytes = tx.getHash().serialize();
            List<Coin> tos = coinData.getTo();
            for (int i = 0, length = tos.size(); i < length; i++) {
                try {
                    byte[] owner = Arrays.concatenate(txHashBytes, new VarInt(i).encode());
//                    Log.info("129 save utxo:::" + Hex.encode(owner));
                    batch.put(owner, tos.get(i).serialize());
                } catch (IOException e) {
                    Log.error(e);
                    return Result.getFailed(KernelErrorCode.IO_ERROR);
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
                    throw new NulsException(recoveryResult.getErrorCode());
                }
                return result;
            }
            // 回滚交易
            result = utxoLedgerTransactionStorageService.deleteTx(tx);
            if (result.isFailed()) {
                Result recoveryResult = saveTx(tx);
                if (recoveryResult.isFailed()) {
                    throw new NulsException(recoveryResult.getErrorCode());
                }
            }
            return result;
        } catch (IOException e) {
            Log.error(e);
            Result rollbackResult = saveTx(tx);
            if (rollbackResult.isFailed()) {
                throw new NulsException(rollbackResult.getErrorCode());
            }
            return Result.getFailed(KernelErrorCode.IO_ERROR);
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
                    NulsByteBuffer byteBuffer = new NulsByteBuffer(from.getOwner());

                    NulsDigestData fromTxHash = byteBuffer.readHash();

                    int fromIndex = (int) byteBuffer.readVarInt();

                    Transaction fromTx = utxoLedgerTransactionStorageService.getTx(fromTxHash);
                    recovery = fromTx.getCoinData().getTo().get(fromIndex);
                    recovery.setFrom(from.getFrom());
                    batch.put(from.getOwner(), recovery.serialize());
                } catch (IOException e) {
                    Log.error(e);
                    return Result.getFailed(KernelErrorCode.IO_ERROR);
                }
            }
            // 删除utxo - to
            List<Coin> tos = coinData.getTo();
            for (int i = 0, length = tos.size(); i < length; i++) {
                byte[] owner = Arrays.concatenate(txHashBytes, new VarInt(i).encode());
//                Log.info("批量删除：" + Hex.encode(owner));
                batch.delete(owner);
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
            digestData.parse(txHashBytes, 0);
        } catch (Exception e) {
            return null;
        }
        return getTx(digestData);
    }

    /**
     * 此txList是待打包的块中的交易，所以toList是下一步的UTXO，应该校验它
     * coinData的交易和txList同处一个块中，txList中的to可能是coinData的from，
     * 也就是可能存在，在同一个块中，下一笔输入就是上一笔的输出，所以需要校验它
     *
     * @return ValidateResult
     */
    @Override
    public ValidateResult verifyCoinData(Transaction transaction, Map<String, Coin> temporaryToMap, Set<String> temporaryFromSet) {
        return verifyCoinData(transaction, temporaryToMap, temporaryFromSet, null);
    }

    /**
     * 此txList是待打包的块中的交易，所以toList是下一步的UTXO，应该校验它
     * coinData的交易和txList同处一个块中，txList中的to可能是coinData的from，
     * 也就是可能存在，在同一个块中，下一笔输入就是上一笔的输出，所以需要校验它
     * bestHeight is used when switch chain.
     *
     * @return ValidateResult
     */
    @Override
    public ValidateResult verifyCoinData(Transaction transaction, Map<String, Coin> temporaryToMap, Set<String> temporaryFromSet, Long bestHeight) {

        if (transaction == null || transaction.getCoinData() == null) {
            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.NULL_PARAMETER);
        }

        try {
            /*
            校验开始
             */
            CoinData coinData = transaction.getCoinData();
            List<Coin> froms = coinData.getFrom();
            int fromSize = froms.size();
            TransactionSignature transactionSignature = new TransactionSignature();
            //交易签名反序列化
            if (transaction.needVerifySignature() && fromSize > 0) {
                try {
                    transactionSignature.parse(transaction.getTransactionSignature(), 0);
                } catch (NulsException e) {
                    return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.LEDGER_P2PKH_SCRIPT_ERROR);
                }
            }
            // 保存Set用于验证自身双花
            if (temporaryFromSet == null) {
                temporaryFromSet = new HashSet<>();
            }

            Na fromTotal = Na.ZERO;
            byte[] fromBytes;
            // 保存在数据库中或者txList中的utxo数据
            Coin fromOfFromCoin = null;
            byte[] fromAddressBytes = null;
            /**
             * 存放真实地址（如果为脚本验证的情况fromAddressBytes存的是脚本信息）
             * */
            byte[] realAddressBytes = null;
            for (int i = 0; i < froms.size(); i++) {
                Coin from = froms.get(i);
                fromBytes = from.getOwner();
                // 验证是否可花费, 校验的coinData的fromUTXO，检查数据库中是否存在此UTXO
                fromOfFromCoin = utxoLedgerUtxoStorageService.getUtxo(fromBytes);

                // 检查txList中是否存在此UTXO
                if (temporaryToMap != null && fromOfFromCoin == null) {
                    fromOfFromCoin = temporaryToMap.get(asString(fromBytes));
                }
                if (null == fromOfFromCoin) {
                    // 如果既不存在于txList的to中(如果txList不为空)，又不存在于数据库中，那么这是一笔问题数据，进一步检查是否存在这笔交易，交易有就是双花，没有就是孤儿交易，则返回失败
                    if (null != utxoLedgerTransactionStorageService.getTxBytes(LedgerUtil.getTxHashBytes(fromBytes))) {
                        return ValidateResult.getFailedResult(CLASS_NAME, TransactionErrorCode.TRANSACTION_REPEATED);
                    } else {
                        return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.ORPHAN_TX);
                    }
                } else {
                    fromAddressBytes = fromOfFromCoin.getOwner();
                    realAddressBytes = fromOfFromCoin.getAddress();
                    // pierre add 非合约转账(从合约转出)交易，验证fromAdress是否是合约地址，如果是，则返回失败，非合约转账(从合约转出)交易不能转出合约地址资产
                    if (transaction.getType() != ContractConstant.TX_TYPE_CONTRACT_TRANSFER) {
                        boolean isContractAddress = contractService.isContractAddress(realAddressBytes);
                        if (isContractAddress) {
                            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.DATA_ERROR);
                        }
                    }

                    // 验证地址中的公钥hash160和交易中的公钥hash160是否相等，不相等则说明这笔utxo不属于交易发出者
                    boolean signtureValidFlag = false;
                    if (transaction.needVerifySignature()) {
                        if (transactionSignature != null) {
                            if (fromAddressBytes != null && transactionSignature.getScripts() != null
                                    && transactionSignature.getScripts().size() > 0) {
                                if (fromAddressBytes.length != Address.ADDRESS_LENGTH) {
                                    Script scriptPubkey = new Script(fromAddressBytes);
                                    for (Script scriptSig : transactionSignature.getScripts()) {
                                        signtureValidFlag = scriptSig.correctlyNulsSpends(transaction, 0, scriptPubkey);
                                        if (signtureValidFlag) {
                                            break;
                                        }
                                    }
                                } else {
                                    for (Script scriptSig : transactionSignature.getScripts()) {
                                        Script redeemScript = new Script(scriptSig.getChunks().get(scriptSig.getChunks().size() - 1).data);
                                        Address address = new Address(NulsContext.getInstance().getDefaultChainId(), NulsContext.P2SH_ADDRESS_TYPE, SerializeUtils.sha256hash160(redeemScript.getProgram()));
                                        Script publicScript = SignatureUtil.createOutputScript(address.getAddressBytes());
                                        signtureValidFlag = scriptSig.correctlyNulsSpends(transaction, 0, publicScript);
                                        if (signtureValidFlag) {
                                            break;
                                        }
                                    }
                                }
                            } else {
                                if (transactionSignature.getP2PHKSignatures() != null && transactionSignature.getP2PHKSignatures().size() != 0) {
                                    for (P2PHKSignature signature : transactionSignature.getP2PHKSignatures()) {
                                        signtureValidFlag = AddressTool.checkPublicKeyHash(realAddressBytes, signature.getSignerHash160());
                                        if (signtureValidFlag) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (!signtureValidFlag) {
                            Log.warn("public key hash160 check error.");
                            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.INVALID_INPUT);
                        }
                    }
                    if (java.util.Arrays.equals(realAddressBytes, NulsConstant.BLACK_HOLE_ADDRESS) || java.util.Arrays.equals(realAddressBytes, NulsConstant.BLACK_HOLE_ADDRESS_TEST_NET)) {
                        return ValidateResult.getFailedResult(CLASS_NAME, KernelErrorCode.ADDRESS_IS_BLOCK_HOLE);
                    }
                    if (NulsContext.getInstance().getDefaultChainId() != SerializeUtils.bytes2Short(realAddressBytes)) {
                        return ValidateResult.getFailedResult(CLASS_NAME, KernelErrorCode.ADDRESS_IS_NOT_BELONGS_TO_CHAIN);
                    }
                }
                // 验证非解锁类型的交易及解锁类型的交易
                if (!transaction.isUnlockTx()) {
                    // 验证非解锁类型的交易，验证是否可用，检查是否还在锁定时间内
                    if (bestHeight == null) {
                        if (!fromOfFromCoin.usable()) {
                            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.UTXO_UNUSABLE);
                        }
                    } else {
                        if (!fromOfFromCoin.usable(bestHeight)) {
                            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.UTXO_UNUSABLE);
                        }
                    }
                } else {
                    // 验证解锁类型的交易
                    if (fromOfFromCoin.getLockTime() != -1) {
                        return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.UTXO_STATUS_CHANGE);
                    }
                }

                // 验证与待确认交易列表中是否有双花，既是待校验交易的fromUtxo是否和txList中的fromUtxo重复，有重复则是双花
                if (temporaryFromSet != null && !temporaryFromSet.add(asString(fromBytes))) {
                    if (i > 0) {
                        for (int x = 0; x < i; x++) {
                            Coin theFrom = froms.get(i);
                            temporaryFromSet.remove(asString(theFrom.getOwner()));
                        }
                    }
                    return ValidateResult.getFailedResult(CLASS_NAME, TransactionErrorCode.TRANSACTION_REPEATED);
                }

                // 验证from的锁定时间和金额
                if (!(fromOfFromCoin.getNa().equals(from.getNa()) && fromOfFromCoin.getLockTime() == from.getLockTime())) {
                    return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.DATA_ERROR);
                }

                fromTotal = fromTotal.add(fromOfFromCoin.getNa());
                from.setFrom(fromOfFromCoin);
            }

            List<Coin> tos = coinData.getTo();
            Na toTotal = Na.ZERO;
            byte[] txBytes = transaction.getHash().serialize();
            for (int i = 0; i < tos.size(); i++) {
                Coin to = tos.get(i);

                // 如果不是调用合约的类型，并且合约地址作为nuls接收者，则返回错误，非合约交易不能转入nuls(CoinBase交易不过此验证)
                if (ContractConstant.TX_TYPE_CALL_CONTRACT != transaction.getType()
                        && AddressTool.validContractAddress(to.getOwner())) {
                    Log.error("Ledger verify error: {}.", ContractErrorCode.NON_CONTRACTUAL_TRANSACTION_NO_TRANSFER.getEnMsg());
                    return ValidateResult.getFailedResult(CLASS_NAME, ContractErrorCode.NON_CONTRACTUAL_TRANSACTION_NO_TRANSFER);
                }

                toTotal = toTotal.add(to.getNa());
                if (temporaryToMap != null) {
                    temporaryToMap.put(asString(ArraysTool.concatenate(txBytes, new VarInt(i).encode())), to);
                }
            }
            // 验证输出不能大于输入
            if (fromTotal.compareTo(toTotal) < 0) {
                return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.INVALID_AMOUNT);
            }
        } catch (Exception e) {
            Log.error(e);
            return ValidateResult.getFailedResult(CLASS_NAME, KernelErrorCode.SYS_UNKOWN_EXCEPTION);
        }

        return ValidateResult.getSuccessResult();
    }

    /**
     * 双花验证不通过的交易需要放入result的data中，一次只验证一对双花的交易
     *
     * @return ValidateResult<List                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               <                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               Transaction>>
     */
    @Override
    public ValidateResult<List<Transaction>> verifyDoubleSpend(Block block) {
        if (block == null) {
            return ValidateResult.getFailedResult(CLASS_NAME, KernelErrorCode.BLOCK_IS_NULL);
        }
        return verifyDoubleSpend(block.getTxs());
    }

    /**
     * 双花验证不通过的交易需要放入result的data中，一次只验证一对双花的交易
     *
     * @return ValidateResult<List                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               <                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               Transaction>>
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
                    ValidateResult validateResult = ValidateResult.getFailedResult(CLASS_NAME, TransactionErrorCode.TRANSACTION_REPEATED);
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
                    throw new NulsException(rollbackResult.getErrorCode());
                }
            }
            return result;
        } catch (IOException e) {
            Result rollbackResult = rollbackUnlockTxCoinData(tx);
            if (rollbackResult.isFailed()) {
                throw new NulsException(rollbackResult.getErrorCode());
            }
            Log.error(e);
            return Result.getFailed(KernelErrorCode.IO_ERROR);
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
                throw new NulsException(result.getErrorCode());
            }
            return result;
        } catch (IOException e) {
            Log.error(e);
            throw new NulsException(KernelErrorCode.IO_ERROR);
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
                    coin.parse(utxoBytes, 0);
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


    /**
     * get UTXO by key
     * <p>
     * 根据key获取UTXO
     *
     * @param address
     * @return Coin
     */
    @Override
    public List<Coin> getAllUtxo(byte[] address) {
        List<Coin> coinList = new ArrayList<>();
        Collection<Entry<byte[], byte[]>> rawList = utxoLedgerUtxoStorageService.getAllUtxoEntryBytes();
        for (Entry<byte[], byte[]> coinEntry : rawList) {
            Coin coin = new Coin();
            try {
                coin.parse(coinEntry.getValue(), 0);
            } catch (NulsException e) {
                Log.info("parse coin form db error");
                continue;
            }
            if (java.util.Arrays.equals(coin.getAddress(), address)) {
                coin.setTempOwner(coin.getOwner());
                coin.setOwner(coinEntry.getKey());
                coinList.add(coin);
            }
        }
        return coinList;
    }
}

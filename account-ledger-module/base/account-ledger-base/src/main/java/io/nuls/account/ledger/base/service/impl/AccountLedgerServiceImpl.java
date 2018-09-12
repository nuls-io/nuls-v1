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

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.ledger.base.manager.BalanceManager;
import io.nuls.account.ledger.base.service.LocalUtxoService;
import io.nuls.account.ledger.base.service.TransactionInfoService;
import io.nuls.account.ledger.base.util.AccountLegerUtils;
import io.nuls.account.ledger.base.util.CoinComparator;
import io.nuls.account.ledger.base.util.CoinComparatorDesc;
import io.nuls.account.ledger.constant.AccountLedgerErrorCode;
import io.nuls.account.ledger.model.CoinDataResult;
import io.nuls.account.ledger.model.MultipleAddressTransferModel;
import io.nuls.account.ledger.model.TransactionInfo;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.ledger.storage.po.TransactionInfoPo;
import io.nuls.account.ledger.storage.service.UnconfirmedTransactionStorageService;
import io.nuls.account.model.Account;
import io.nuls.account.model.Balance;
import io.nuls.account.service.AccountService;
import io.nuls.account.util.AccountTool;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.service.ContractService;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.*;

import io.nuls.kernel.script.*;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.TransactionFeeCalculator;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.service.LedgerService;
import io.nuls.ledger.util.LedgerUtil;
import io.nuls.protocol.model.tx.TransferTransaction;
import io.nuls.protocol.model.validator.TxMaxSizeValidator;
import io.nuls.protocol.model.validator.TxRemarkValidator;
import io.nuls.protocol.service.BlockService;
import io.nuls.protocol.service.TransactionService;
import io.nuls.sdk.script.P2PKHScriptSig;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author Facjas
 */
@Service
public class AccountLedgerServiceImpl implements AccountLedgerService, InitializingBean {

    @Autowired
    private LocalUtxoService localUtxoService;

    @Autowired
    private UnconfirmedTransactionStorageService unconfirmedTransactionStorageService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private BalanceManager balanceManager;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private BlockService blockService;

    @Autowired
    private TransactionInfoService transactionInfoService;

    @Autowired
    private ContractService contractService;

    private Lock lock = new ReentrantLock();
    private Lock saveLock = new ReentrantLock();
    private Lock changeWholeLock = new ReentrantLock();

    //todo 这里有问题 保存本地已使用的交易，Save locally used transactions
    private Set<String> usedTxSets;

    @Override
    public void afterPropertiesSet() throws NulsException {
    }

    @Override
    public Result<Integer> saveConfirmedTransactionList(List<Transaction> txs) {
        if (txs == null || txs.size() == 0) {
            Result.getSuccess().setData(0);
        }

        List<byte[]> localAddresses = AccountLegerUtils.getLocalAddresses();

        List<Transaction> savedTxList = new ArrayList<>();
        Result result;
        for (int i = 0; i < txs.size(); i++) {

            Transaction tx = txs.get(i);
            List<byte[]> addresses = AccountLegerUtils.getRelatedAddresses(tx, localAddresses);
            if (addresses == null || addresses.size() == 0) {
                continue;
            }

            result = saveConfirmedTransaction(tx, addresses);
            if (result.isSuccess()) {
                if (result.getData() != null && (int) result.getData() == 1) {
                    savedTxList.add(tx);
                }
            } else {
                rollbackTransactions(savedTxList, false);
                return result;
            }
        }

        balanceManager.refreshBalanceIfNesessary();

        return Result.getSuccess().setData(savedTxList.size());
    }

    @Override
    public Result<Integer> saveConfirmedTransaction(Transaction tx) {

        if (tx == null) {
            Result.getSuccess().setData(0);
        }

        List<byte[]> addresses = AccountLegerUtils.getRelatedAddresses(tx);
        if (addresses == null || addresses.size() == 0) {
            Result.getSuccess().setData(0);
        }
        return saveConfirmedTransaction(tx, addresses);
    }


    private Result<Integer> saveConfirmedTransaction(Transaction tx, List<byte[]> addresses) {
        if (tx == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }

        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        txInfoPo.setStatus(TransactionInfo.CONFIRMED);

        Result result = transactionInfoService.saveTransactionInfo(txInfoPo, addresses);
        if (result.isFailed()) {
            return result;
        }

        // 判断交易是否已经存在，如果存在则不处理coin
        Transaction unconfirmedTx = unconfirmedTransactionStorageService.getUnconfirmedTx(tx.getHash()).getData();

        if (unconfirmedTx == null) {
            result = localUtxoService.saveUtxoForLocalAccount(tx);
            if (result.isFailed()) {
                transactionInfoService.deleteTransactionInfo(txInfoPo);
                return result;
            }
        } else {
            unconfirmedTransactionStorageService.deleteUnconfirmedTx(tx.getHash());
        }

        for (int i = 0; i < addresses.size(); i++) {
            balanceManager.refreshBalance(addresses.get(i));
        }
        result.setData(new Integer(1));
        return result;
    }

    @Override
    public Result<Integer> verifyAndSaveUnconfirmedTransaction(Transaction tx) {
        saveLock.lock();
        try {
            ValidateResult result = tx.verify();
            if (result.isFailed()) {
                return result;
            }
            if (!tx.isSystemTx()) {
                Map<String, Coin> toCoinMap = addToCoinMap(tx);
                if (usedTxSets == null) {
                    initUsedTxSets();
                }
                result = this.ledgerService.verifyCoinData(tx, toCoinMap, usedTxSets);
                if (result.isFailed()) {
                    Log.info("verifyCoinData failed : " + result.getMsg());
                    return result;
                }
            }
            Result<Integer> res = saveUnconfirmedTransaction(tx);
            return res;
        } finally {
            saveLock.unlock();
        }
    }


    public void resetUsedTxSets() {
        usedTxSets = null;
    }

    public void initUsedTxSets() {
        usedTxSets = new HashSet<>();
        List<Transaction> allUnconfirmedTxs = unconfirmedTransactionStorageService.loadAllUnconfirmedList().getData();
        for (Transaction tx : allUnconfirmedTxs) {
            CoinData coinData = tx.getCoinData();
            if (coinData == null) {
                continue;
            }
            List<Coin> froms = tx.getCoinData().getFrom();
            for (Coin from : froms) {
                usedTxSets.add(LedgerUtil.asString(from.getOwner()));
            }
        }
    }

    private Map<String, Coin> addToCoinMap(Transaction transaction) {
        Map<String, Coin> toMap = new HashMap<>();

        CoinData coinData = transaction.getCoinData();
        if (coinData == null) {
            return toMap;
        }

        List<Coin> froms = coinData.getFrom();

        if (froms == null || froms.size() == 0) {
            return toMap;
        }

        for (Coin coin : froms) {
            byte[] keyBytes = coin.getOwner();
            try {
                Transaction unconfirmedTx = getUnconfirmedTransaction(NulsDigestData.fromDigestHex(LedgerUtil.getTxHash(keyBytes))).getData();
                if (unconfirmedTx != null) {
                    int index = LedgerUtil.getIndex(keyBytes);
                    Coin toCoin = unconfirmedTx.getCoinData().getTo().get(index);
                    toMap.put(LedgerUtil.asString(keyBytes), toCoin);
                }
            } catch (NulsException e) {
                Log.error(e);
            }
        }
        return toMap;
    }

    protected Result saveUnconfirmedTransaction(Transaction tx) {
        if (tx == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        List<byte[]> localAccountList = AccountLegerUtils.getLocalAddresses();
        List<byte[]> addresses = AccountLegerUtils.getRelatedAddresses(tx, localAccountList);
        if (addresses == null || addresses.size() == 0) {
            return Result.getSuccess().setData(new Integer(0));
        }

        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        txInfoPo.setStatus(TransactionInfo.UNCONFIRMED);

        Result result = transactionInfoService.saveTransactionInfo(txInfoPo, addresses);
        if (result.isFailed()) {
            return result;
        }

        result = localUtxoService.saveUtxoForAccount(tx, addresses);
        if (result.isFailed()) {
            transactionInfoService.deleteTransactionInfo(txInfoPo);
            return result;
        }

        result = unconfirmedTransactionStorageService.saveUnconfirmedTx(tx.getHash(), tx);

        for (int i = 0; i < addresses.size(); i++) {
            balanceManager.refreshBalance(addresses.get(i));
        }
        return result;
    }


    @Override
    public Result<List<Transaction>> getAllUnconfirmedTransaction() {
        List<Transaction> localTxList = unconfirmedTransactionStorageService.loadAllUnconfirmedList().getData();
        return Result.getSuccess().setData(localTxList);
    }

    @Override
    public Result<Integer> rollbackTransactions(List<Transaction> txs) {
        Result result = rollbackTransactions(txs, true);
        if (result.isSuccess()) {
            balanceManager.refreshBalanceIfNesessary();
        }
        return result;
    }

    private Result<Integer> rollbackTransactions(List<Transaction> txs, boolean isCheckMine) {
        List<Transaction> txListToRollback;
        if (isCheckMine) {
            txListToRollback = filterLocalTransaction(txs);
        } else {
            txListToRollback = txs;
        }
        for (int i = txListToRollback.size() - 1; i >= 0; i--) {
            rollbackTransaction(txListToRollback.get(i));
        }
        return Result.getSuccess().setData(new Integer(txListToRollback.size()));
    }

    private Result<Integer> rollbackTransaction(Transaction tx) {
        if (!AccountLegerUtils.isLocalTransaction(tx)) {
            return Result.getSuccess().setData(new Integer(0));
        }

        List<byte[]> addresses = AccountLegerUtils.getRelatedAddresses(tx);
        if (addresses == null || addresses.size() == 0) {
            return Result.getSuccess().setData(new Integer(0));
        }
        if (tx.isSystemTx()) {
            return deleteTransaction(tx);
        }
        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        Result result = transactionInfoService.saveTransactionInfo(txInfoPo, addresses);
        if (result.isFailed()) {
            return result;
        }
        result = unconfirmedTransactionStorageService.saveUnconfirmedTx(tx.getHash(), tx);
        return result;
    }

    @Override
    public Result<Integer> deleteTransaction(Transaction tx) {
        if (!AccountLegerUtils.isLocalTransaction(tx)) {
            return Result.getSuccess().setData(new Integer(0));
        }

        List<byte[]> addresses = AccountLegerUtils.getRelatedAddresses(tx);
        if (addresses == null || addresses.size() == 0) {
            return Result.getSuccess().setData(new Integer(0));
        }

        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        Result result = transactionInfoService.deleteTransactionInfo(txInfoPo);

        if (result.isFailed()) {
            return result;
        }
        result = localUtxoService.deleteUtxoOfTransaction(tx);

        if (result.isFailed()) {
            return result;
        }
        result = unconfirmedTransactionStorageService.deleteUnconfirmedTx(tx.getHash());

        for (int i = 0; i < addresses.size(); i++) {
            balanceManager.refreshBalance(addresses.get(i));
        }

        if (usedTxSets != null) {
            CoinData coinData = tx.getCoinData();
            if (coinData != null) {
                List<Coin> froms = tx.getCoinData().getFrom();
                for (Coin from : froms) {
                    usedTxSets.remove(LedgerUtil.asString(from.getOwner()));
                }
            }
        }

        return result;
    }

    @Override
    public Result<Balance> getBalance(byte[] address) {
        if (address == null || address.length != Address.ADDRESS_LENGTH) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }

        if (!AccountLegerUtils.isLocalAccount(address)) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }

        Balance balance = balanceManager.getBalance(address).getData();

        if (balance == null) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }

        return Result.getSuccess().setData(balance);
    }

    @Override
    public CoinDataResult getCoinData(byte[] address, Na amount, int size, Na price) throws NulsException {
        if (null == price) {
            throw new NulsRuntimeException(KernelErrorCode.PARAMETER_ERROR);
        }
        lock.lock();
        try {
            CoinDataResult coinDataResult = new CoinDataResult();
            coinDataResult.setEnough(false);
            List<Coin> coinList = balanceManager.getCoinListByAddress(address);

            coinList = coinList.stream()
                    .filter(coin1 -> coin1.usable() && !Na.ZERO.equals(coin1.getNa()))
                    .sorted(CoinComparator.getInstance())
                    .collect(Collectors.toList());

            if (coinList.isEmpty()) {
                return coinDataResult;
            }
            List<Coin> coins = new ArrayList<>();
            Na values = Na.ZERO;
            // 累加到足够支付转出额与手续费
            for (int i = 0; i < coinList.size(); i++) {
                Coin coin = coinList.get(i);
                coins.add(coin);
                size += coin.size();
                if (i == 127) {
                    size += 1;
                }
                //每次累加一条未花费余额时，需要重新计算手续费
                Na fee = TransactionFeeCalculator.getFee(size, price);
                values = values.add(coin.getNa());

                /**
                 * 判断是否是脚本验证UTXO
                 * */
                int signType = coinDataResult.getSignType();
                if(signType != 3){
                    if((signType & 0x01) == 0  && coin.getTempOwner().length == 23){
                        coinDataResult.setSignType((byte)(signType|0x01));
                        size+= P2PHKSignature.SERIALIZE_LENGTH;
                    }else if ((signType & 0x02) == 0 && coin.getTempOwner().length != 23){
                        coinDataResult.setSignType((byte)(signType|0x02));
                        size+= P2PHKSignature.SERIALIZE_LENGTH;
                    }
                }

                //需要判断是否找零，如果有找零，则需要重新计算手续费
                if (values.isGreaterThan(amount.add(fee))) {
                    Na change = values.subtract(amount.add(fee));
                    Coin changeCoin = new Coin();
                    //changeCoin.setOwner(address);
                    changeCoin.setOwner(SignatureUtil.createOutputScript(address).getProgram());
                    changeCoin.setNa(change);

                    fee = TransactionFeeCalculator.getFee(size + changeCoin.size(), price);
                    if (values.isLessThan(amount.add(fee))) {
                        continue;
                    }
                    changeCoin.setNa(values.subtract(amount.add(fee)));
                    if (!changeCoin.getNa().equals(Na.ZERO)) {
                        coinDataResult.setChange(changeCoin);
                    }
                }
                coinDataResult.setFee(fee);
                if (values.isGreaterOrEquals(amount.add(fee))) {
                    coinDataResult.setEnough(true);
                    coinDataResult.setCoinList(coins);
                    break;
                }
            }
            return coinDataResult;
        }finally {
            lock.unlock();
        }
    }


    /**
     * 根据账户计算一次交易(不超出最大交易数据大小下)的最大金额
     */
    @Override
    public Result<Na> getMaxAmountOfOnce(byte[] address, Transaction tx, Na price) {
        lock.lock();
        try {
            tx.getCoinData().setFrom(null);
            int txSize = tx.size();
            //计算所有to的size
            for (Coin coin : tx.getCoinData().getTo()){
                txSize += coin.size();
            }
            //如果交易未签名，则计算一个默认签名的长度
            if (null == tx.getTransactionSignature()) {
                txSize = txSize + P2PHKSignature.SERIALIZE_LENGTH;
            }
            //计算目标size，coindata中from的总大小
            int targetSize = TxMaxSizeValidator.MAX_TX_SIZE - txSize;
            List<Coin> coinList = balanceManager.getCoinListByAddress(address);
            if (coinList.isEmpty()) {
                return Result.getSuccess().setData(Na.ZERO);
            }
            Collections.sort(coinList, CoinComparator.getInstance());
            Na max = Na.ZERO;
            int size = 0;
            //将所有余额从小到大排序后，累计未花费的余额
            for (int i = 0; i < coinList.size(); i++) {
                Coin coin = coinList.get(i);
                if (!coin.usable()) {
                    continue;
                }
                if (coin.getNa().equals(Na.ZERO)) {
                    continue;
                }
                size += coin.size();
                if (i == 127) {
                    size += 1;
                }
                if (size > targetSize) {
                    break;
                }
                max = max.add(coin.getNa());
            }
            Na fee = TransactionFeeCalculator.getFee(size, price);
            max = max.subtract(fee);
            return Result.getSuccess().setData(max);
        } catch (Exception e) {
            return Result.getFailed(TransactionErrorCode.DATA_ERROR);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Na getTxFee(byte[] address, Na amount, int size, Na price) {
        List<Coin> coinList = balanceManager.getCoinListByAddress(address);
        if (coinList.isEmpty()) {
            return Na.ZERO;
        }
        Collections.sort(coinList, CoinComparator.getInstance());
        if (null == price) {
            price = TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES;
        }
        Na values = Na.ZERO;
        Na fee = null;
        for (int i = 0; i < coinList.size(); i++) {
            Coin coin = coinList.get(i);
            if (!coin.usable()) {
                continue;
            }
            size += coin.size();
            if (i == 127) {
                size += 1;
            }
            fee = TransactionFeeCalculator.getFee(size, price);
            values = values.add(coin.getNa());

            if (values.isGreaterOrEquals(amount.add(fee))) {
                Na change = values.subtract(amount.add(fee));
                if (change.isGreaterThan(Na.ZERO)) {
                    Coin changeCoin = new Coin();
                    changeCoin.setOwner(address);
                    changeCoin.setNa(change);

                    fee = TransactionFeeCalculator.getFee(size + changeCoin.size(), price);
                    if (values.isLessThan(amount.add(fee))) {
                        continue;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return fee;
    }

    @Override
    public Result estimateFee(byte[] address, Na price) {
        if (null == price) {
            throw new NulsRuntimeException(KernelErrorCode.PARAMETER_ERROR);
        }
        Transaction tx = new TransferTransaction();
        tx.setTime(TimeService.currentTimeMillis());
        lock.lock();
        try {
            //获取这个地址的所有coin的总大小
            List<Coin> coinList = balanceManager.getCoinListByAddress(address);
            if (coinList.isEmpty()) {//没有可用余额
                Result.getFailed(TransactionErrorCode.DATA_ERROR);
            }
            tx.setCoinData(null);
            //默认coindata中to为15+脚本长度 +备注+签名
            Script scriptPubkey = SignatureUtil.createOutputScript(address);
            int txSize = tx.size() + 15+scriptPubkey.getProgram().length + TxRemarkValidator.MAX_REMARK_LEN;
            int targetSize = TxMaxSizeValidator.MAX_TX_SIZE - txSize;
            Collections.sort(coinList, CoinComparatorDesc.getInstance());
            int size = tx.size()+15+scriptPubkey.getProgram().length;
            //将所有余额从大到小排序后，累计未花费的余额
            byte sign_type = 0;
            int txNum=1;
            for (int i = 0; i < coinList.size(); i++) {
                Coin coin = coinList.get(i);
                if (!coin.usable()) {
                    continue;
                }
                if (coin.getNa().equals(Na.ZERO)) {
                    continue;
                }
                size += coin.size();
                if (i == 127) {
                    size += 1;
                }
                /**
                 * 判断是否是脚本验证UTXO
                 * */
                if(sign_type != 3){
                    if((sign_type & 0x01) == 0  && coin.getTempOwner().length == 23){
                        sign_type = (byte)(sign_type|0x01);
                        size+= P2PHKSignature.SERIALIZE_LENGTH;
                    }else if ((sign_type & 0x02) == 0 && coin.getTempOwner().length != 23){
                        sign_type = (byte)(sign_type|0x02);
                        size+= P2PHKSignature.SERIALIZE_LENGTH;
                    }
                }
                if (size > targetSize*txNum) {//大于一个tx的size 所以需要另一个tx装
                    size += txSize;
                    txNum++;
                }
            }
            Na fee = TransactionFeeCalculator.getFee(size, price);
            return Result.getSuccess().setData(fee);
        } catch (Exception e) {
            return Result.getFailed(TransactionErrorCode.DATA_ERROR);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Result getAvailableTotalUTXO(byte[] address) {
        Map<String,Object> map =new HashMap<>();
        List<Coin> coinList=  balanceManager.getCoinListByAddress(address);
        Na max=Na.ZERO;
        List<Coin> coins =new ArrayList<>();
        for (int i = 0; i < coinList.size(); i++) {
            Coin coin = coinList.get(i);
            if (!coin.usable()) {
                continue;
            }
            if (coin.getNa().equals(Na.ZERO)) {
                continue;
            }
            max = max.add(coin.getNa());
            coins.add(coin);
        }
        map.put("size",coins.size());
        map.put("max",max.getValue());
        return Result.getSuccess().setData(map);
    }


    @Override
    public Result transfer(byte[] from, byte[] to, Na values, String password, String remark, Na price) {
        try {
            Result<Account> accountResult = accountService.getAccount(from);
            if (accountResult.isFailed()) {
                return accountResult;
            }
            Account account = accountResult.getData();
            if (account.isEncrypted() && account.isLocked()) {
                AssertUtil.canNotEmpty(password, "the password can not be empty");
                if (!account.validatePassword(password)) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
                }
            }

            // 检查to是否为合约地址，如果是合约地址，则返回错误
            if (contractService.isContractAddress(to)) {
                return Result.getFailed(ContractErrorCode.NON_CONTRACTUAL_TRANSACTION_NO_TRANSFER);
            }

            TransferTransaction tx = new TransferTransaction();
            if (StringUtils.isNotBlank(remark)) {
                try {
                    tx.setRemark(remark.getBytes(NulsConfig.DEFAULT_ENCODING));
                } catch (UnsupportedEncodingException e) {
                    Log.error(e);
                }
            }
            tx.setTime(TimeService.currentTimeMillis());
            CoinData coinData = new CoinData();
            Script scriptPubkey = SignatureUtil.createOutputScript(to);
            Coin toCoin = new Coin(scriptPubkey.getProgram(), values);
            coinData.getTo().add(toCoin);
            if (price == null) {
                price = TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES;
            }
            CoinDataResult coinDataResult = getCoinData(from, values, tx.size() + coinData.size(), price);
            if (!coinDataResult.isEnough()) {
                return Result.getFailed(AccountLedgerErrorCode.INSUFFICIENT_BALANCE);
            }
            coinData.setFrom(coinDataResult.getCoinList());
            if (coinDataResult.getChange() != null) {
                coinData.getTo().add(coinDataResult.getChange());
            }
            tx.setCoinData(coinData);
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            /*TransactionSignature transactionSignature = new TransactionSignature(accountService.signDigest(tx.getHash().getDigestBytes(), account, password),account.getPubKey());
            transactionSignature.setSignData();
            P2PKHScriptSig sig = new P2PKHScriptSig();
            sig.setPublicKey();
            sig.setSignData();
            tx.setTransactionSignature(sig.serialize());*/
            //生成签名
            List<ECKey> signEckeys = new ArrayList<>();
            List<ECKey> scriptEckeys = new ArrayList<>();
            ECKey eckey = account.getEcKey(password);
            //如果最后一位为1则表示该交易包含普通签名
            if((coinDataResult.getSignType() & 0x01) == 0x01){
                signEckeys.add(eckey);
            }
            //如果倒数第二位位为1则表示该交易包含脚本签名
            if((coinDataResult.getSignType() & 0x02) == 0x02){
                scriptEckeys.add(eckey);
            }
            SignatureUtil.createTransactionSignture(tx,scriptEckeys,signEckeys);


            // 保存未确认交易到本地账户
            Result saveResult = verifyAndSaveUnconfirmedTransaction(tx);
            if (saveResult.isFailed()) {
                if (KernelErrorCode.DATA_SIZE_ERROR.getCode().equals(saveResult.getErrorCode().getCode())) {
                    //重新算一次交易(不超出最大交易数据大小下)的最大金额
                    Result rs = getMaxAmountOfOnce(from, tx, price);
                    if (rs.isSuccess()) {
                        Na maxAmount = (Na) rs.getData();
                        rs = Result.getFailed(KernelErrorCode.DATA_SIZE_ERROR_EXTEND);
                        rs.setMsg(rs.getMsg() + maxAmount.toDouble());
                    }
                    return rs;
                }
                return saveResult;
            }
//          transactionService.newTx(tx);
            Result sendResult = transactionService.broadcastTx(tx);
            if (sendResult.isFailed()) {
                this.deleteTransaction(tx);
                return sendResult;
            }
            return Result.getSuccess().setData(tx.getHash().getDigestHex());
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(KernelErrorCode.IO_ERROR);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }

    @Override
    public Result multipleAddressTransfer(List<MultipleAddressTransferModel> fromList, List<MultipleAddressTransferModel> toList, String password, String remark, Na price) {
        try {
            for (MultipleAddressTransferModel from : fromList) {
                Result<Account> accountResult = accountService.getAccount(from.getAddress());
                if (accountResult.isFailed()) {
                    return accountResult;
                }
                Account account = accountResult.getData();
                if (account.isEncrypted() && account.isLocked()) {
                    AssertUtil.canNotEmpty(password, "the password can not be empty");
                    if (!account.validatePassword(password)) {
                        return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
                    }
                }
            }
            for (MultipleAddressTransferModel to : toList) {
                // 检查to是否为合约地址，如果是合约地址，则返回错误
                if (contractService.isContractAddress(to.getAddress())) {
                    return Result.getFailed(ContractErrorCode.NON_CONTRACTUAL_TRANSACTION_NO_TRANSFER);
                }
            }
            TransferTransaction tx = new TransferTransaction();
            if (StringUtils.isNotBlank(remark)) {
                try {
                    tx.setRemark(remark.getBytes(NulsConfig.DEFAULT_ENCODING));
                } catch (UnsupportedEncodingException e) {
                    Log.error(e);
                }
            }
            tx.setTime(TimeService.currentTimeMillis());
            CoinData coinData = new CoinData();
            for (MultipleAddressTransferModel to : toList) {
                Script scriptPubkey = SignatureUtil.createOutputScript(to.getAddress());
                //Coin toCoin = new Coin(to.getAddress(), Na.valueOf(to.getAmount()));
                Coin toCoin = new Coin(scriptPubkey.getProgram(), Na.valueOf(to.getAmount()));
                coinData.getTo().add(toCoin);
            }
            if (price == null) {
                price = TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES;
            }
            List<CoinDataResult> coinDataResultList = new ArrayList<>();
            int sub = 0;
            for (MultipleAddressTransferModel from : fromList) {//获取每个from地址的 utxo(需要的币从小到大排序)
                CoinDataResult coinDataResult = null;
                if(sub == 0){
                    coinDataResult = getCoinData(from.getAddress(), Na.valueOf(from.getAmount()), tx.size() + coinData.size(), price);
                }else{
                    coinDataResult = getCoinData(from.getAddress(), Na.valueOf(from.getAmount()), coinData.size(), price);
                }
                coinDataResultList.add(coinDataResult);
            }
            List<Coin> fromCoinList = new ArrayList<>();//从多个地址中获取币 from
            List<Coin> changeCoinList = new ArrayList<>();
            for (CoinDataResult coinDataResult : coinDataResultList) {
                if (!coinDataResult.isEnough()) {//验证utxo是否足够
                    return Result.getFailed(AccountLedgerErrorCode.INSUFFICIENT_BALANCE);
                }
                fromCoinList.addAll(coinDataResult.getCoinList());//把每个地址获取的币放到list里面
                if (coinDataResult.getChange() != null) {
                    changeCoinList.add(coinDataResult.getChange());
                }
            }
            coinData.setFrom(fromCoinList);//每个地址from获取的utxo list
            coinData.getTo().addAll(changeCoinList);//找零钱
            tx.setCoinData(coinData);
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));

            //生成签名
            List<ECKey> signEckeys = new ArrayList<>();
            List<ECKey> scriptEckeys = new ArrayList<>();

            for(int index = 0; index < fromList.size();index++){
                Result<Account> accountResult = accountService.getAccount(fromList.get(index).getAddress());
                Account account = accountResult.getData();
                //用于生成ECKey
                ECKey ecKey = account.getEcKey(password);
                CoinDataResult coinDataResult = coinDataResultList.get(index);
                //如果最后一位为1则表示该交易包含普通签名
                if((coinDataResult.getSignType() & 0x01) == 0x01){
                    signEckeys.add(ecKey);
                }
                //如果倒数第二位位为1则表示该交易包含脚本签名
                if((coinDataResult.getSignType() & 0x02) == 0x02){
                    scriptEckeys.add(ecKey);
                }
            }
            SignatureUtil.createTransactionSignture(tx,scriptEckeys,signEckeys);
            // 保存未确认交易到本地账户
            Result saveResult = verifyAndSaveUnconfirmedTransaction(tx);
            if (saveResult.isFailed()) {
                for (MultipleAddressTransferModel from : fromList) {
                    if (KernelErrorCode.DATA_SIZE_ERROR.getCode().equals(saveResult.getErrorCode().getCode())) {
                        //重新算一次交易(不超出最大交易数据大小下)的最大金额
                        Na maxAmount = getMaxAmountOfOnce(from.getAddress(), tx, price).getData();
                        Result rs = Result.getFailed(KernelErrorCode.DATA_SIZE_ERROR_EXTEND);
                        rs.setMsg(rs.getMsg() + maxAmount.toDouble());
                        return rs;
                    }
                }
                return saveResult;
            }
            Result sendResult = transactionService.broadcastTx(tx);
            if (sendResult.isFailed()) {
                this.deleteTransaction(tx);
                return sendResult;
            }
            return Result.getSuccess().setData(tx.getHash().getDigestHex());
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(KernelErrorCode.IO_ERROR);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }

    @Override
    public Result changeWhole(byte[] address, String password, Na price) {
        try {
            Result<Account> accountResult = accountService.getAccount(address);
            if (accountResult.isFailed()) {
                return accountResult;
            }
            Account account = accountResult.getData();
            if (account.isEncrypted() && account.isLocked()) {
                AssertUtil.canNotEmpty(password, "the password can not be empty");
                if (!account.validatePassword(password)) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
                }
            }
            // 检查to是否为合约地址，如果是合约地址，则返回错误
            if (contractService.isContractAddress(address)) {
                return Result.getFailed(ContractErrorCode.NON_CONTRACTUAL_TRANSACTION_NO_TRANSFER);
            }
            TransferTransaction tx=null;
            boolean flag=true;
            while (flag) {
                tx = getChangeWholeTxInfoList(address, account, password, price);
                // 保存未确认交易到本地账户
                if (null!=tx) {
                    Result saveResult = verifyAndSaveUnconfirmedTransaction(tx);
                    if (saveResult.isFailed()) {
                        if (KernelErrorCode.DATA_SIZE_ERROR.getCode().equals(saveResult.getErrorCode().getCode())) {
                            //重新算一次交易(不超出最大交易数据大小下)的最大金额
                            Result rs = getMaxAmountOfOnce(address, tx, price);
                            if (rs.isSuccess()) {
                                Na maxAmount = (Na) rs.getData();
                                rs = Result.getFailed(KernelErrorCode.DATA_SIZE_ERROR_EXTEND);
                                rs.setMsg(rs.getMsg() + maxAmount.toDouble());
                            }
                            return rs;
                        }
                        return saveResult;
                    }
                    Result sendResult = transactionService.broadcastTx(tx);
                    if (sendResult.isFailed()) {
                        this.deleteTransaction(tx);
                        return sendResult;
                    }
                    Result available=  getAvailableTotalUTXO(address);//可用总额
                    Map<String ,Object> map=  (Map<String ,Object>) available.getData();
                    int size=  (int) map.get("size");
                    if (size<AccountConstant.MIM_COUNT){  //小于20就停止
                        flag=false;
                    }
                }else{
                    flag=false;
                }
            }
            return Result.getSuccess().setData(tx.getHash().getDigestHex());
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(KernelErrorCode.NUMBER_SMALL);
        }
    }

    private TransferTransaction getChangeWholeTxInfoList(byte[] address, Account account, String password, Na price) {
        List<Coin> coinList = balanceManager.getCoinListByAddress(address);//这里要重新获取可用余额
        TransferTransaction tx = new TransferTransaction();
        changeWholeLock.lock();
        try {
            tx.setTime(TimeService.currentTimeMillis());
            Script scriptPubkey = SignatureUtil.createOutputScript(address);
            //默认coindata中to暂定76字节（两条tocoin）
            int size = tx.size() + 15 + scriptPubkey.getProgram().length;
            //计算目标size，coindata中from的总大小
            int targetSize = TxMaxSizeValidator.MAX_TX_SIZE - size;
            if (coinList.isEmpty()) {
                return null;
            }
            //从大到小
            Collections.sort(coinList, CoinComparatorDesc.getInstance());
            Na max = Na.ZERO;
            List<Coin> coins = new ArrayList<>();
            byte sign_type = 0;
            for (int i = 0; i < coinList.size(); i++) {
                Coin coin = coinList.get(i);
                if (!coin.usable()) {
                    continue;
                }
                if (coin.getNa().equals(Na.ZERO)) {
                    continue;
                }
                size += coin.size();
                if (i == 127) {
                    size += 1;
                }
                if (size > targetSize) {
                    break;
                }
                coins.add(coin);
                /**
                 * 判断是否是脚本验证UTXO
                 * */
                if(sign_type != 3){
                    if((sign_type & 0x01) == 0  && coin.getTempOwner().length == 23){
                        sign_type = (byte)(sign_type|0x01);
                        size+= P2PHKSignature.SERIALIZE_LENGTH;
                    }else if ((sign_type & 0x02) == 0 && coin.getTempOwner().length != 23){
                        sign_type = (byte)(sign_type|0x02);
                        size+= P2PHKSignature.SERIALIZE_LENGTH;
                    }
                }
                max = max.add(coin.getNa());
            }
            Na fee = TransactionFeeCalculator.getFee(size, price);
            max = max.subtract(fee);
            CoinData coinData = new CoinData();
            Coin toCoin = new Coin(scriptPubkey.getProgram(), max);
            coinData.getTo().add(toCoin);
            coinData.setFrom(coins);
            tx.setCoinData(coinData);
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));//一定要交易组装完才能setHash

            //生成签名
            List<ECKey> signEckeys = new ArrayList<>();
            List<ECKey> scriptEckeys = new ArrayList<>();
            ECKey eckey = account.getEcKey(password);
            //如果最后一位为1则表示该交易包含普通签名
            if((sign_type & 0x01) == 0x01){
                signEckeys.add(eckey);
            }
            //如果倒数第二位位为1则表示该交易包含脚本签名
            if((sign_type & 0x02) == 0x02){
                scriptEckeys.add(eckey);
            }
            SignatureUtil.createTransactionSignture(tx,scriptEckeys,signEckeys);
            return tx;
        } catch (Exception e) {
            Log.error(e);
            return null;
        } finally {
            changeWholeLock.unlock();
        }
    }


    @Override
    public Result transferFee(byte[] from, byte[] to, Na values, String remark, Na price) {
        Result<Account> accountResult = accountService.getAccount(from);
        if (accountResult.isFailed()) {
            return accountResult;
        }
        TransferTransaction tx = new TransferTransaction();
        try {
            tx.setRemark(remark.getBytes(NulsConfig.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            return Result.getFailed(LedgerErrorCode.PARAMETER_ERROR);
        }
        tx.setTime(TimeService.currentTimeMillis());
        CoinData coinData = new CoinData();
        Coin toCoin = new Coin(to, values);
        coinData.getTo().add(toCoin);
        tx.setCoinData(coinData);
        Na fee = getTxFee(from, values, tx.size() + P2PHKSignature.SERIALIZE_LENGTH, price);
        Result result = Result.getSuccess().setData(fee);
        return result;
    }

    @Override
    public Result createTransaction(List<Coin> inputs, List<Coin> outputs, byte[] remark) {
        TransferTransaction tx = new TransferTransaction();
        CoinData coinData = new CoinData();
        coinData.setTo(outputs);
        coinData.setFrom(inputs);
        tx.setRemark(remark);

        tx.setCoinData(coinData);
        tx.setTime(TimeService.currentTimeMillis());
        //计算交易手续费最小值
        int size = tx.size() + P2PHKSignature.SERIALIZE_LENGTH;
        Na minFee = TransactionFeeCalculator.getTransferFee(size);
        //计算inputs和outputs的差额 ，求手续费
        Na fee = Na.ZERO;
        for (Coin coin : tx.getCoinData().getFrom()) {
            fee = fee.add(coin.getNa());
        }
        for (Coin coin : tx.getCoinData().getTo()) {
            fee = fee.subtract(coin.getNa());
        }
        if (fee.isLessThan(minFee)) {
            return Result.getFailed(LedgerErrorCode.FEE_NOT_RIGHT);
        }

        try {
            String txHex = Hex.encode(tx.serialize());
            return Result.getSuccess().setData(txHex);
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(KernelErrorCode.IO_ERROR);
        }
    }

    @Override
    public Transaction signTransaction(Transaction tx, ECKey ecKey) throws IOException {
        /*P2PKHScriptSig sig = new P2PKHScriptSig();
        sig.setPublicKey(ecKey.getPubKey());
        sig.setSignData(accountService.signDigest(tx.getHash().getDigestBytes(), ecKey));
        tx.setTransactionSignature(sig.serialize());*/
        List<ECKey> pubEckeys = new ArrayList<>();
        pubEckeys.add(ecKey);
        SignatureUtil.createTransactionSignture(tx,null,pubEckeys);
        return tx;
    }

    @Override
    public Result broadcast(Transaction tx) {
        return transactionService.broadcastTx(tx);
    }


    /**
     * 导入账户
     */
    @Override
    public Result importLedgerByAddress(String address) {
        if (address == null || !AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }

        // 初始化NRC20资产
        contractService.initAllTokensByAccount(address);

        byte[] addressBytes = null;
        try {
            addressBytes = AddressTool.getAddress(address);
        } catch (Exception e) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }

        long start = 0;
        long end = NulsContext.getInstance().getBestHeight();
        while (start <= end) {
            for (long i = start; i <= end; i++) {
                List<Transaction> txs = blockService.getBlock(i).getData().getTxs();
                for (Transaction tx : txs) {
                    importConfirmedTransaction(tx, addressBytes);
                }
            }
            start = end;
            end = NulsContext.getInstance().getBestHeight();
            if (start == end) {
                break;
            }
        }
        try {
            balanceManager.refreshBalance(addressBytes);
        } catch (Exception e) {
            Log.info(address);
        }
        return Result.getSuccess();
    }

    @Override
    public Result<List<TransactionInfo>> getTxInfoList(byte[] address) {
        return transactionInfoService.getTxInfoList(address);
    }

    @Override
    public Result<List<Coin>> getLockedUtxo(byte[] address) {
        Result<List<Coin>> result = new Result<>();
        result.setSuccess(true);
        List<Coin> coinList = balanceManager.getCoinListByAddress(address);
        List<Coin> lockCoinList = new ArrayList<>();
        for (Coin coin : coinList) {
            if (coin != null && !coin.usable()) {
                lockCoinList.add(coin);
            }
        }
//        Collections.sort(coinList, CoinComparator.getInstance());
        result.setData(lockCoinList);
        return result;
    }

    @Override
    public Result<Integer> deleteUnconfirmedTx(byte[] address) {
        Result result = getAllUnconfirmedTransaction();
        if (result.getData() == null) {
            return Result.getSuccess().setData(new Integer(0));
        }
        List<Transaction> txs = (List<Transaction>) result.getData();
        int i = 0;
        try {
            for (Transaction tx : txs) {
                //if (Arrays.equals(tx.getAddressFromSig(), address))
                if(SignatureUtil.containsAddress(tx,address)){
                    unconfirmedTransactionStorageService.deleteUnconfirmedTx(tx.getHash());
                    localUtxoService.deleteUtxoOfTransaction(tx);
                    i++;
                }
            }
        }catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
        return Result.getSuccess().setData(new Integer(i));
    }

    protected Result<Integer> importConfirmedTransaction(Transaction tx, byte[] address) {

        if (!AccountLegerUtils.isTxRelatedToAddress(tx, address)) {
            return Result.getSuccess().setData(new Integer(0));
        }

        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        txInfoPo.setStatus(TransactionInfo.CONFIRMED);

        List<byte[]> addresses = new ArrayList<>();
        addresses.add(address);
        Result result = transactionInfoService.saveTransactionInfo(txInfoPo, addresses);

        if (result.isFailed()) {
            return result;
        }
        result = localUtxoService.saveUtxoForAccount(tx, address);
        if (result.isFailed()) {
            transactionInfoService.deleteTransactionInfo(txInfoPo);
        }
        return result;
    }

    protected List<Transaction> filterLocalTransaction(List<Transaction> txs) {
        List<Transaction> resultTxs = new ArrayList<>();
        if (txs == null || txs.size() == 0) {
            return resultTxs;
        }
        Collection<Account> localAccountList = accountService.getAccountList().getData();
        if (localAccountList == null || localAccountList.size() == 0) {
            return resultTxs;
        }
        Transaction tmpTx;
        for (int i = 0; i < txs.size(); i++) {
            tmpTx = txs.get(i);
            if (AccountLegerUtils.isLocalTransaction(tmpTx)) {
                resultTxs.add(tmpTx);
            }
        }
        return resultTxs;
    }

    @Override
    public Result unlockCoinData(Transaction tx, long newLockTime) {
        List<byte[]> addresses = AccountLegerUtils.getRelatedAddresses(tx);
        if (addresses == null || addresses.size() == 0) {
            return Result.getSuccess();
        }
        byte status = TransactionInfo.CONFIRMED;
        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        txInfoPo.setStatus(status);

        List<byte[]> addresses1 = localUtxoService.unlockCoinData(tx, newLockTime).getData();
        for (byte[] address : addresses1) {
            balanceManager.refreshBalance(address);
        }
        return Result.getSuccess();
    }

    @Override
    public Result rollbackUnlockTxCoinData(Transaction tx) {
        List<byte[]> addresses = AccountLegerUtils.getRelatedAddresses(tx);
        if (addresses == null || addresses.size() == 0) {
            return Result.getSuccess();
        }
        byte status = TransactionInfo.CONFIRMED;
        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        txInfoPo.setStatus(status);

        List<byte[]> addresses1 = localUtxoService.rollbackUnlockTxCoinData(tx).getData();
        for (byte[] address : addresses1) {
            balanceManager.refreshBalance(address);
        }

        CoinData coinData = tx.getCoinData();
        if (coinData != null) {
            List<Coin> froms = tx.getCoinData().getFrom();
            for (Coin from : froms) {
                usedTxSets.remove(LedgerUtil.asString(from.getOwner()));
            }
        }
        return Result.getSuccess();
    }

    @Override
    public Result<Transaction> getUnconfirmedTransaction(NulsDigestData hash) {
        return unconfirmedTransactionStorageService.getUnconfirmedTx(hash);
    }
}

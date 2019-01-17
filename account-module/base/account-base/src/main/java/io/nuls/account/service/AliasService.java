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

package io.nuls.account.service;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.ledger.model.CoinDataResult;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.model.Account;
import io.nuls.account.model.Alias;
import io.nuls.account.model.MultiSigAccount;
import io.nuls.account.storage.po.AccountPo;
import io.nuls.account.storage.po.AliasPo;
import io.nuls.account.storage.service.AccountStorageService;
import io.nuls.account.storage.service.AliasStorageService;
import io.nuls.account.tx.AliasTransaction;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.*;

import io.nuls.kernel.script.*;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.TransactionFeeCalculator;
import io.nuls.ledger.service.LedgerService;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.protocol.service.TransactionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 账户模块内部功能服务类
 * Account module internal function service class
 *
 * @author: Charlie
 */
@Service
public class AliasService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountStorageService accountStorageService;

    @Autowired
    private AccountLedgerService accountLedgerService;

    @Autowired
    private AliasStorageService aliasStorageService;

    @Autowired
    private MessageBusService messageBusService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LedgerService ledgerService;

    private AccountCacheService accountCacheService = AccountCacheService.getInstance();

    /**
     * 设置别名
     * Initiate a transaction to set alias.
     *
     * @param addr      Address of account
     * @param password  password of account
     * @param aliasName the alias to set
     * @return txhash
     */
    public Result<String> setAlias(String addr, String aliasName, String password) {
        if (!AddressTool.validAddress(addr)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Account account = accountService.getAccount(addr).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        if (account.isEncrypted() && account.isLocked()) {
            if (!account.validatePassword(password)) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
        }
        if (StringUtils.isNotBlank(account.getAlias())) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_ALREADY_SET_ALIAS);
        }
        if (!StringUtils.validAlias(aliasName)) {
            return Result.getFailed(AccountErrorCode.ALIAS_FORMAT_WRONG);
        }
        if (!isAliasUsable(aliasName)) {
            return Result.getFailed(AccountErrorCode.ALIAS_EXIST);
        }
        byte[] addressBytes = account.getAddress().getAddressBytes();
        try {
            //创建一笔设置别名的交易
            AliasTransaction tx = new AliasTransaction();
            tx.setTime(TimeService.currentTimeMillis());
            Alias alias = new Alias(addressBytes, aliasName);
            tx.setTxData(alias);

            CoinDataResult coinDataResult = accountLedgerService.getCoinData(addressBytes, AccountConstant.ALIAS_NA, tx.size() , TransactionFeeCalculator.OTHER_PRICE_PRE_1024_BYTES);
            if (!coinDataResult.isEnough()) {
                return Result.getFailed(AccountErrorCode.INSUFFICIENT_BALANCE);
            }
            CoinData coinData = new CoinData();
            coinData.setFrom(coinDataResult.getCoinList());
            Coin change = coinDataResult.getChange();
            if (null != change) {
                //创建toList
                List<Coin> toList = new ArrayList<>();
                toList.add(change);
                coinData.setTo(toList);
            }

            Coin coin = new Coin(NulsConstant.BLACK_HOLE_ADDRESS, AccountConstant.ALIAS_NA, 0);
            coinData.addTo(coin);

            tx.setCoinData(coinData);
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));

            //生成签名
            List<ECKey> signEckeys = new ArrayList<>();
            List<ECKey> scriptEckeys = new ArrayList<>();;
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

            Result saveResult = accountLedgerService.verifyAndSaveUnconfirmedTransaction(tx);
            if (saveResult.isFailed()) {
                if (KernelErrorCode.DATA_SIZE_ERROR.getCode().equals(saveResult.getErrorCode().getCode())) {
                    //重新算一次交易(不超出最大交易数据大小下)的最大金额
                    Result rs = accountLedgerService.getMaxAmountOfOnce(account.getAddress().getAddressBytes(), tx,
                            TransactionFeeCalculator.OTHER_PRICE_PRE_1024_BYTES);
                    if(rs.isSuccess()){
                        Na maxAmount = (Na)rs.getData();
                        rs = Result.getFailed(KernelErrorCode.DATA_SIZE_ERROR_EXTEND);
                        rs.setMsg(rs.getMsg() + maxAmount.toDouble());
                    }
                    return rs;

                }
                return saveResult;
            }

            this.transactionService.newTx(tx);

            Result sendResult = this.transactionService.broadcastTx(tx);
            if (sendResult.isFailed()) {
                accountLedgerService.deleteTransaction(tx);
                return sendResult;
            }
            String hash = tx.getHash().getDigestHex();
            return Result.getSuccess().setData(hash);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(KernelErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 保存别名(全网)
     * 1.保存别名alias至数据库
     * 2.从数据库取出对应的account账户,将别名设置进account然后保存至数据库
     * 3.将修改后的account重新进行缓存
     * saveAlias
     * 1. Save the alias to the database.
     * 2. Take the corresponding account from the database, set the alias to account and save it to the database.
     * 3. Re-cache the modified account.
     */
    public Result saveAlias(AliasPo aliaspo) throws NulsException {
        try {
            Result result = aliasStorageService.saveAlias(aliaspo);
            if (result.isFailed()) {
                this.rollbackAlias(aliaspo);
            }
            AccountPo po = accountStorageService.getAccount(aliaspo.getAddress()).getData();
            if (null != po) {
                po.setAlias(aliaspo.getAlias());
                Result resultAcc = accountStorageService.updateAccount(po);
                if (resultAcc.isFailed()) {
                    this.rollbackAlias(aliaspo);
                }
                Account account = po.toAccount();
                accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
            }
        } catch (Exception e) {
            this.rollbackAlias(aliaspo);
            Log.error(e);
            return Result.getFailed(AccountErrorCode.FAILED);
        }
        return Result.getSuccess().setData(true);
    }

    public Alias getAlias(String alias) {
        AliasPo aliasPo = aliasStorageService.getAlias(alias).getData();
        return aliasPo == null ? null : aliasPo.toAlias();
    }

    public boolean isAliasUsable(String alias) {
        return null == getAlias(alias);
    }

    /**
     * 回滚别名操作(删除别名(全网))
     * 1.从数据库删除别名对象数据
     * 2.取出对应的account将别名清除,重新存入数据库
     * 3.重新缓存account
     * rollbackAlias
     * 1.Delete the alias data from the database.
     * 2. Remove the corresponding account to clear the alias and restore it in the database.
     * 3. Recache the account.
     */
    public Result rollbackAlias(AliasPo aliasPo) throws NulsException {
        try {
            AliasPo po = aliasStorageService.getAlias(aliasPo.getAlias()).getData();
            if (po != null && Arrays.equals(po.getAddress(), aliasPo.getAddress())) {
                aliasStorageService.removeAlias(aliasPo.getAlias());
                Result<AccountPo> rs = accountStorageService.getAccount(aliasPo.getAddress());
                if (rs.isSuccess()) {
                    AccountPo accountPo = rs.getData();
                    accountPo.setAlias("");
                    Result result = accountStorageService.updateAccount(accountPo);
                    if (result.isFailed()) {
                        return Result.getFailed(AccountErrorCode.FAILED);
                    }
                    Account account = accountPo.toAccount();
                    accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
                }
            }
        } catch (Exception e) {
            Log.error(e);
            throw new NulsException(AccountErrorCode.ALIAS_ROLLBACK_ERROR);
        }
        return Result.getSuccess().setData(true);
    }


    /**
     * 获取设置别名交易手续费
     * Gets to set the alias transaction fee
     *
     * @param address
     * @param aliasName
     * @return
     */
    public Result<Na> getAliasFee(String address, String aliasName) {
        if (!AddressTool.validAddress(address)) {
            Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Account account = accountService.getAccount(address).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        byte[] addressBytes = account.getAddress().getAddressBytes();
        try {
            //创建一笔设置别名的交易
            AliasTransaction tx = new AliasTransaction();
            tx.setTime(TimeService.currentTimeMillis());
            Alias alias = new Alias(addressBytes, aliasName);
            tx.setTxData(alias);
            CoinDataResult coinDataResult = accountLedgerService.getCoinData(addressBytes, AccountConstant.ALIAS_NA, tx.size() , TransactionFeeCalculator.OTHER_PRICE_PRE_1024_BYTES);
            if (!coinDataResult.isEnough()) {
                return Result.getFailed(AccountErrorCode.INSUFFICIENT_BALANCE);
            }
            CoinData coinData = new CoinData();
            coinData.setFrom(coinDataResult.getCoinList());
            Coin change = coinDataResult.getChange();
            if (null != change) {
                //创建toList
                List<Coin> toList = new ArrayList<>();
                toList.add(change);
                coinData.setTo(toList);
            }
            Coin coin = new Coin(NulsConstant.BLACK_HOLE_ADDRESS, Na.parseNuls(1), 0);
            coinData.addTo(coin);
            tx.setCoinData(coinData);
            Na fee = TransactionFeeCalculator.getMaxFee(tx.size() );
            return Result.getSuccess().setData(fee);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(KernelErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }


    /**
     * 获取设置别名交易手续费
     * Gets to set the alias transaction fee
     *
     * @param address
     * @param aliasName
     * @return
     */
    public Result<Na> getMultiAliasFee(String address, String aliasName) {
        if (!AddressTool.validAddress(address)) {
            Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        byte[] addressBytes = AddressTool.getAddress(address);
        try {
            //创建一笔设置别名的交易
            AliasTransaction tx = new AliasTransaction();
            tx.setTime(TimeService.currentTimeMillis());
            Alias alias = new Alias(addressBytes, aliasName);
            tx.setTxData(alias);
            CoinDataResult coinDataResult = accountLedgerService.getMutilCoinData(addressBytes, AccountConstant.ALIAS_NA, tx.size() , TransactionFeeCalculator.OTHER_PRICE_PRE_1024_BYTES);
            if (!coinDataResult.isEnough()) {
                return Result.getFailed(AccountErrorCode.INSUFFICIENT_BALANCE);
            }
            CoinData coinData = new CoinData();
            coinData.setFrom(coinDataResult.getCoinList());
            Coin change = coinDataResult.getChange();
            if (null != change) {
                //创建toList
                List<Coin> toList = new ArrayList<>();
                toList.add(change);
                coinData.setTo(toList);
            }
            Coin coin = new Coin(NulsConstant.BLACK_HOLE_ADDRESS, Na.parseNuls(1), 0);
            coinData.addTo(coin);
            tx.setCoinData(coinData);
            Na fee = TransactionFeeCalculator.getMaxFee(tx.size() );
            return Result.getSuccess().setData(fee);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(KernelErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }
    /**
     * 多签账户设置别名
     * Initiate a transaction to set alias.
     *
     * @param addr      Address of account
     * @param password  password of account
     * @param aliasName the alias to set
     * @return txhash
     */
    public Result<String> setMutilAlias(String addr,String signAddr, String aliasName, String password,List<String> pubKeys,int m,String txdata) {
        //签名账户
        Account account = accountService.getAccount(signAddr).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        if (account.isEncrypted() && account.isLocked()) {
            if (!account.validatePassword(password)) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
        }
        try {
            AliasTransaction tx = new AliasTransaction();
            TransactionSignature transactionSignature = new TransactionSignature();
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            List<Script> scripts = new ArrayList<>();
            byte[] addressBytes = AddressTool.getAddress(addr);
            //如果txdata为空则表示当前请求为多签发起者调用，需要创建交易
            if(txdata == null || txdata.trim().length() == 0){
                if (!StringUtils.validAlias(aliasName)) {
                    return Result.getFailed(AccountErrorCode.ALIAS_FORMAT_WRONG);
                }
                if (!isAliasUsable(aliasName)) {
                    return Result.getFailed(AccountErrorCode.ALIAS_EXIST);
                }
                //创建一笔设置别名的交易
                tx = new AliasTransaction();
                Script redeemScript = ScriptBuilder.createNulsRedeemScript(m,pubKeys);
                tx.setTime(TimeService.currentTimeMillis());
                Alias alias = new Alias(addressBytes, aliasName);
                tx.setTxData(alias);
                //交易签名的长度为m*单个签名长度+赎回脚本长度
                int scriptSignLenth = redeemScript.getProgram().length + m*72;
                CoinDataResult coinDataResult = accountLedgerService.getMutilCoinData(addressBytes, AccountConstant.ALIAS_NA, tx.size()+scriptSignLenth , TransactionFeeCalculator.OTHER_PRICE_PRE_1024_BYTES);
                if (!coinDataResult.isEnough()) {
                    return Result.getFailed(AccountErrorCode.INSUFFICIENT_BALANCE);
                }
                CoinData coinData = new CoinData();
                coinData.setFrom(coinDataResult.getCoinList());
                Coin change = coinDataResult.getChange();
                if (null != change) {
                    //创建toList
                    List<Coin> toList = new ArrayList<>();
                    toList.add(change);
                    coinData.setTo(toList);
                }
                Coin coin = new Coin(NulsConstant.BLACK_HOLE_ADDRESS, Na.parseNuls(1), 0);
                coinData.addTo(coin);
                tx.setCoinData(coinData);
                tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
                //将赎回脚本先存储在签名脚本中
                scripts.add(redeemScript);
                transactionSignature.setScripts(scripts);
            }
            //如果txdata不为空表示多签交易已经创建好了，将交易反序列化出来
            else{
                byte[] txByte = Hex.decode(txdata);
                tx.parse(new NulsByteBuffer(txByte));
                transactionSignature.parse(new NulsByteBuffer(tx.getTransactionSignature()));
                p2PHKSignatures = transactionSignature.getP2PHKSignatures();
                scripts = transactionSignature.getScripts();
            }
            //使用签名账户对交易进行签名
            P2PHKSignature p2PHKSignature = new P2PHKSignature();
            ECKey eckey = account.getEcKey(password);
            p2PHKSignature.setPublicKey(eckey.getPubKey());
            //用当前交易的hash和账户的私钥账户
            p2PHKSignature.setSignData(accountService.signDigest(tx.getHash().getDigestBytes(),eckey));
            p2PHKSignatures.add(p2PHKSignature);
            Result result = txMutilProcessing(tx,p2PHKSignatures,scripts,transactionSignature,addressBytes);
            return  result;
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(KernelErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }


    /**
     * 多签账户设置别名
     * Initiate a transaction to set alias.
     *
     * @param addr      Address of account
     * @param signAddr      Address of account
     * @param password  password of account
     * @param aliasName the alias to set
     * @return Result
     */
    public Result<String> setMutilAlias(String addr,String signAddr, String aliasName, String password) {
        //签名账户
        Account account = accountService.getAccount(signAddr).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        if (account.isEncrypted() && account.isLocked()) {
            if (!account.validatePassword(password)) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
        }
        try {
            byte[] addressBytes = AddressTool.getAddress(addr);
            Result<MultiSigAccount> sigAccountResult = accountService.getMultiSigAccount(addr);
            MultiSigAccount multiSigAccount = sigAccountResult.getData();
            if(!AddressTool.validSignAddress(multiSigAccount.getPubKeyList(),account.getPubKey())){
                return Result.getFailed(AccountErrorCode.SIGN_ADDRESS_NOT_MATCH);
            }
            Script redeemScript = accountLedgerService.getRedeemScript(multiSigAccount);
            if(redeemScript == null){
                return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
            }
            AliasTransaction tx = new AliasTransaction();
            TransactionSignature transactionSignature = new TransactionSignature();
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            List<Script> scripts = new ArrayList<>();
            tx.setTime(TimeService.currentTimeMillis());
            Alias alias = new Alias(addressBytes, aliasName);
            tx.setTxData(alias);
            //交易签名的长度为m*单个签名长度+赎回脚本长度
            int scriptSignLenth = redeemScript.getProgram().length + ((int)multiSigAccount.getM())*72;
            CoinDataResult coinDataResult = accountLedgerService.getMutilCoinData(addressBytes, AccountConstant.ALIAS_NA, tx.size()+scriptSignLenth , TransactionFeeCalculator.OTHER_PRICE_PRE_1024_BYTES);
            if (!coinDataResult.isEnough()) {
                return Result.getFailed(AccountErrorCode.INSUFFICIENT_BALANCE);
            }
            CoinData coinData = new CoinData();
            coinData.setFrom(coinDataResult.getCoinList());
            Coin change = coinDataResult.getChange();
            if (null != change) {
                //创建toList
                List<Coin> toList = new ArrayList<>();
                toList.add(change);
                coinData.setTo(toList);
            }
            Coin coin = new Coin(NulsConstant.BLACK_HOLE_ADDRESS, Na.parseNuls(1), 0);
            coinData.addTo(coin);
            tx.setCoinData(coinData);
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            //将赎回脚本先存储在签名脚本中
            scripts.add(redeemScript);
            transactionSignature.setScripts(scripts);
            //使用签名账户对交易进行签名
            P2PHKSignature p2PHKSignature = new P2PHKSignature();
            ECKey eckey = account.getEcKey(password);
            p2PHKSignature.setPublicKey(eckey.getPubKey());
            //用当前交易的hash和账户的私钥账户
            p2PHKSignature.setSignData(accountService.signDigest(tx.getHash().getDigestBytes(),eckey));
            p2PHKSignatures.add(p2PHKSignature);
            Result result = txMutilProcessing(tx,p2PHKSignatures,scripts,transactionSignature,addressBytes);
            return  result;
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(KernelErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }


    /**
     * A transfers NULS to B   多签交易
     *
     * @param signAddr 签名地址
     * @param password password of A
     * @param txdata   需要签名的数据
     * @return Result
     */
    public Result signMultiAliasTransaction(String signAddr,String password,String txdata){
        try {
            Result<Account> accountResult = accountService.getAccount(signAddr);
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
            AliasTransaction tx = new AliasTransaction();
            TransactionSignature transactionSignature = new TransactionSignature();
            byte[] txByte = Hex.decode(txdata);
            tx.parse(new NulsByteBuffer(txByte));
            transactionSignature.parse(new NulsByteBuffer(tx.getTransactionSignature()));
            return accountLedgerService.txMultiProcess(tx,transactionSignature,account,password);
        }catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }catch (Exception e){
            Log.error(e);
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
    }

    public boolean isMutilAliasUsable(byte[] address,String aliasName) {
        List<AliasPo> list = aliasStorageService.getAliasList().getData();
        for (AliasPo aliasPo : list) {
            if (Arrays.equals(aliasPo.getAddress(), address)) {
                return false;
            }
        }
        for (AliasPo aliasPo : list) {
            if (aliasName.equals(aliasPo.getAlias())) {
                return  false;
            }
        }
        return  true;
    }

    public Result<String> txMutilProcessing(Transaction tx, List<P2PHKSignature> p2PHKSignatures,List<Script> scripts,TransactionSignature transactionSignature,byte[] fromAddr) throws NulsException,IOException {
        //当已签名数等于M则自动广播该交易
        if(p2PHKSignatures.size() == SignatureUtil.getM(scripts.get(0))){
            //将交易中的签名数据P2PHKSignatures按规则排序
            Collections.sort(p2PHKSignatures,P2PHKSignature.PUBKEY_COMPARATOR);
            //将排序后的P2PHKSignatures的签名数据取出和赎回脚本结合生成解锁脚本
            List<byte[]> signatures= new ArrayList<>();
            for (P2PHKSignature p2PHKSignatureTemp:p2PHKSignatures) {
                signatures.add(p2PHKSignatureTemp.getSignData().getSignBytes());
            }
            transactionSignature.setP2PHKSignatures(null);
            Script scriptSign = ScriptBuilder.createNulsP2SHMultiSigInputScript(signatures,scripts.get(0));
            transactionSignature.getScripts().clear();
            transactionSignature.getScripts().add(scriptSign);
            tx.setTransactionSignature(transactionSignature.serialize());
            // 保存未确认交易到本地账户
            Result saveResult = accountLedgerService.verifyAndSaveUnconfirmedTransaction(tx);
            if (saveResult.isFailed()) {
                if (KernelErrorCode.DATA_SIZE_ERROR.getCode().equals(saveResult.getErrorCode().getCode())) {
                    //重新算一次交易(不超出最大交易数据大小下)的最大金额
                    Result rs = accountLedgerService.getMaxAmountOfOnce(fromAddr, tx, TransactionFeeCalculator.OTHER_PRICE_PRE_1024_BYTES);
                    if (rs.isSuccess()) {
                        Na maxAmount = (Na) rs.getData();
                        rs = Result.getFailed(KernelErrorCode.DATA_SIZE_ERROR_EXTEND);
                        rs.setMsg(rs.getMsg() + maxAmount.toDouble());
                    }
                    return rs;
                }
                return saveResult;
            }
            transactionService.newTx(tx);
            Result sendResult = transactionService.broadcastTx(tx);
            if (sendResult.isFailed()) {
                accountLedgerService.deleteTransaction(tx);
                return sendResult;
            }
            return Result.getSuccess().setData(tx.getHash().getDigestHex());
        }
        //如果签名数还没达到，则返回交易
        else{
            transactionSignature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(transactionSignature.serialize());
            return Result.getSuccess().setData(Hex.encode(tx.serialize()));
        }
    }
}

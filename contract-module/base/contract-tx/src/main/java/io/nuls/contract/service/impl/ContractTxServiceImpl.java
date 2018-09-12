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
package io.nuls.contract.service.impl;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.ledger.model.CoinDataResult;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.model.Account;
import io.nuls.account.service.AccountService;
import io.nuls.account.util.AccountTool;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.dto.ContractResult;
import io.nuls.contract.dto.ContractTokenTransferInfoPo;
import io.nuls.contract.entity.tx.CallContractTransaction;
import io.nuls.contract.entity.tx.CreateContractTransaction;
import io.nuls.contract.entity.tx.DeleteContractTransaction;
import io.nuls.contract.entity.txdata.CallContractData;
import io.nuls.contract.entity.txdata.CreateContractData;
import io.nuls.contract.entity.txdata.DeleteContractData;
import io.nuls.contract.helper.VMHelper;
import io.nuls.contract.ledger.manager.ContractBalanceManager;
import io.nuls.contract.service.ContractTxService;
import io.nuls.contract.storage.po.ContractAddressInfoPo;
import io.nuls.contract.storage.service.ContractAddressStorageService;
import io.nuls.contract.storage.service.ContractTokenTransferStorageService;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.program.ProgramCall;
import io.nuls.contract.vm.program.ProgramCreate;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.contract.vm.program.ProgramResult;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.calc.LongUtils;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.map.MapUtil;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.*;

import io.nuls.kernel.script.SignatureUtil;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.TransactionFeeCalculator;
import io.nuls.kernel.utils.VarInt;
import io.nuls.protocol.service.TransactionService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/5/22
 */
@Component
public class ContractTxServiceImpl implements ContractTxService, InitializingBean {

    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountLedgerService accountLedgerService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ContractAddressStorageService contractAddressStorageService;
    @Autowired
    private ContractTokenTransferStorageService contractTokenTransferStorageService;
    @Autowired
    private VMHelper vmHelper;
    @Autowired
    private VMContext vmContext;
    @Autowired
    private ContractBalanceManager contractBalanceManager;

    private ProgramExecutor programExecutor;

    @Override
    public void afterPropertiesSet() throws NulsException {
        programExecutor = vmHelper.getProgramExecutor();
    }

    /**
     * 创建生成智能合约的交易
     * 如果是创建合约的交易，交易仅仅用于创建合约，合约内部不执行复杂逻辑
     *
     * @param sender       交易创建者
     * @param gasLimit     最大gas消耗
     * @param price        执行合约单价
     * @param contractCode 合约代码
     * @param args         参数列表
     * @param password     账户密码
     * @param remark       备注
     * @return
     */
    @Override
    public Result contractCreateTx(String sender, Long gasLimit, Long price,
                                   byte[] contractCode, String[][] args,
                                   String password, String remark) {
        try {
            AssertUtil.canNotEmpty(sender, "the sender address can not be empty");
            AssertUtil.canNotEmpty(contractCode, "the contractCode can not be empty");
            Na value = Na.ZERO;

            Result<Account> accountResult = accountService.getAccount(sender);
            if (accountResult.isFailed()) {
                return accountResult;
            }

            Account account = accountResult.getData();
            // 验证账户密码
            if (account.isEncrypted() && account.isLocked()) {
                AssertUtil.canNotEmpty(password, "the password can not be empty");
                if (!account.validatePassword(password)) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
                }
            }

            // 生成一个地址作为智能合约地址
            Address contractAddress = AccountTool.createContractAddress();

            byte[] contractAddressBytes = contractAddress.getAddressBytes();
            byte[] senderBytes = AddressTool.getAddress(sender);

            CreateContractTransaction tx = new CreateContractTransaction();
            if (StringUtils.isNotBlank(remark)) {
                try {
                    tx.setRemark(remark.getBytes(NulsConfig.DEFAULT_ENCODING));
                } catch (UnsupportedEncodingException e) {
                    Log.error(e);
                    throw new RuntimeException(e);
                }
            }
            tx.setTime(TimeService.currentTimeMillis());


            // 计算CoinData
            /*
             * 智能合约计算手续费以消耗的Gas*Price为根据，然而创建交易时并不执行智能合约，
             * 所以此时交易的CoinData是不固定的，比实际要多，
             * 打包时执行智能合约，真实的手续费已算出，然而tx的手续费已扣除，
             * 多扣除的费用会以CoinBase交易还给Sender
             */
            CoinData coinData = new CoinData();
            // 向智能合约账户转账
            if (!Na.ZERO.equals(value)) {
                Coin toCoin = new Coin(contractAddressBytes, value);
                coinData.getTo().add(toCoin);
            }

            BlockHeader blockHeader = NulsContext.getInstance().getBestBlock().getHeader();
            // 当前区块高度
            long blockHeight = blockHeader.getHeight();
            // 当前区块状态根
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);
            AssertUtil.canNotEmpty(prevStateRoot, "All features of the smart contract are locked.");

            // 执行VM验证合法性
            ProgramCreate programCreate = new ProgramCreate();
            programCreate.setContractAddress(contractAddressBytes);
            programCreate.setSender(senderBytes);
            programCreate.setValue(BigInteger.valueOf(value.getValue()));
            programCreate.setPrice(price.longValue());
            programCreate.setGasLimit(gasLimit.longValue());
            programCreate.setNumber(blockHeight);
            programCreate.setContractCode(contractCode);
            if (args != null) {
                programCreate.setArgs(args);
            }
            ProgramExecutor track = programExecutor.begin(prevStateRoot);
            ProgramResult programResult = track.create(programCreate);

            // 执行结果失败时，交易直接返回错误，不上链，不消耗Gas，
            if(!programResult.isSuccess()) {
                Result result = Result.getFailed(ContractErrorCode.DATA_ERROR);
                result.setMsg(ContractUtil.simplifyErrorMsg(programResult.getErrorMessage()));
                return result;
            }
            /*long gasUsed = programResult.getGasUsed();
            // 预估1.5倍Gas
            gasUsed += gasUsed >> 1;*/
            long gasUsed = gasLimit.longValue();
            Na imputedNa = Na.valueOf(LongUtils.mul(gasUsed, price));
            // 总花费
            Na totalNa = imputedNa.add(value);

            // 组装txData
            CreateContractData createContractData = new CreateContractData();
            createContractData.setSender(senderBytes);
            createContractData.setContractAddress(contractAddressBytes);
            createContractData.setValue(value.getValue());
            createContractData.setGasLimit(gasLimit);
            createContractData.setPrice(price);
            createContractData.setCodeLen(contractCode.length);
            createContractData.setCode(contractCode);
            if (args != null) {
                createContractData.setArgsCount((byte) args.length);
                if (args.length > 0) {
                    createContractData.setArgs(args);
                }
            }
            tx.setTxData(createContractData);

            CoinDataResult coinDataResult = accountLedgerService.getCoinData(senderBytes, totalNa, tx.size() + coinData.size(), TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES);
            if (!coinDataResult.isEnough()) {
                return Result.getFailed(TransactionErrorCode.INSUFFICIENT_BALANCE);
            }
            coinData.setFrom(coinDataResult.getCoinList());
            // 找零的UTXO
            if (coinDataResult.getChange() != null) {
                coinData.getTo().add(coinDataResult.getChange());
            }
            tx.setCoinData(coinData);
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));

            // 交易签名

            List<ECKey> signEckeys = new ArrayList<>();
            List<ECKey> pubEckeys = new ArrayList<>();

            //如果最后一位为1则表示该交易包含普通签名
            if ((coinDataResult.getSignType() & 0x01) == 0x01) {
                signEckeys.add(account.getEcKey());
            }
            //如果倒数第二位位为1则表示该交易包含脚本签名
            if ((coinDataResult.getSignType() & 0x02) == 0x02) {
                pubEckeys.add(account.getEcKey());
            }
            SignatureUtil.createTransactionSignture(tx, signEckeys, pubEckeys);

            // 保存未确认交易到本地账本
            Result saveResult = accountLedgerService.verifyAndSaveUnconfirmedTransaction(tx);
            if (saveResult.isFailed()) {
                if (KernelErrorCode.DATA_SIZE_ERROR.getCode().equals(saveResult.getErrorCode().getCode())) {
                    //重新算一次交易(不超出最大交易数据大小下)的最大金额
                    Result rs = accountLedgerService.getMaxAmountOfOnce(senderBytes, tx, TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES);
                    if (rs.isSuccess()) {
                        Na maxAmount = (Na) rs.getData();
                        rs = Result.getFailed(KernelErrorCode.DATA_SIZE_ERROR_EXTEND);
                        rs.setMsg(rs.getMsg() + maxAmount.toDouble());
                    }
                    return rs;
                }
                return saveResult;
            }

            // 广播交易
            Result sendResult = transactionService.broadcastTx(tx);
            if (sendResult.isFailed()) {
                accountLedgerService.deleteTransaction(tx);
                return sendResult;
            }
            Map<String, String> resultMap = MapUtil.createHashMap(2);
            String txHash = tx.getHash().getDigestHex();
            String contractAddressStr = AddressTool.getStringAddressByBytes(contractAddressBytes);
            resultMap.put("txHash", txHash);
            resultMap.put("contractAddress", contractAddressStr);
            // 保留未确认的创建合约交易到内存中
            this.saveLocalUnconfirmedCreateContractTransaction(sender, resultMap, tx.getTime());
            return Result.getSuccess().setData(resultMap);
        } catch (IOException e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.CONTRACT_TX_CREATE_ERROR);
            result.setMsg(e.getMessage());
            return result;
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (Exception e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.CONTRACT_TX_CREATE_ERROR);
            result.setMsg(e.getMessage());
            return result;
        }
    }

    /**
     * key: accountAddress
     * value(Map):
     * key: contractAddress
     * value(Map):
     * key: txHash / contractAddress / time/ success(optional)
     * value: txHash-V / contractAddress-V / time-V/ success-V(true,false)
     */
    private static final Map<String, Map<String, Map<String, String>>> LOCAL_UNCONFIRMED_CREATE_CONTRACT_TRANSACTION = MapUtil.createLinkedHashMap(4);
    private ReentrantLock lock = new ReentrantLock();

    private void saveLocalUnconfirmedCreateContractTransaction(String sender, Map<String, String> resultMap, long time) {
        lock.lock();
        try {
            LinkedHashMap<String, String> map = MapUtil.createLinkedHashMap(3);
            map.putAll(resultMap);
            map.put("time", String.valueOf(time));
            String contractAddress = map.get("contractAddress");
            Map<String, Map<String, String>> unconfirmedOfAccountMap = LOCAL_UNCONFIRMED_CREATE_CONTRACT_TRANSACTION.get(sender);
            if (unconfirmedOfAccountMap == null) {
                unconfirmedOfAccountMap = MapUtil.createLinkedHashMap(4);
                unconfirmedOfAccountMap.put(contractAddress, map);
                LOCAL_UNCONFIRMED_CREATE_CONTRACT_TRANSACTION.put(sender, unconfirmedOfAccountMap);
            } else {
                unconfirmedOfAccountMap.put(contractAddress, map);
            }
        } finally {
            lock.unlock();
        }
    }

    public LinkedList<Map<String, String>> getLocalUnconfirmedCreateContractTransaction(String sender) {
        Map<String, Map<String, String>> unconfirmedOfAccountMap = LOCAL_UNCONFIRMED_CREATE_CONTRACT_TRANSACTION.get(sender);
        if (unconfirmedOfAccountMap == null) {
            return null;
        }
        return new LinkedList<>(unconfirmedOfAccountMap.values());
    }

    public void removeLocalUnconfirmedCreateContractTransaction(String sender, String contractAddress, ContractResult contractResult) {
        lock.lock();
        try {
            Map<String, Map<String, String>> unconfirmedOfAccountMap = LOCAL_UNCONFIRMED_CREATE_CONTRACT_TRANSACTION.get(sender);
            if (unconfirmedOfAccountMap == null) {
                return;
            }
            // 合约创建成功，删除未确认交易
            if (contractResult.isSuccess()) {
                unconfirmedOfAccountMap.remove(contractAddress);
            } else {
                // 合约执行失败，保留未确认交易，并标注错误信息
                Map<String, String> dataMap = unconfirmedOfAccountMap.get(contractAddress);
                if (dataMap != null) {
                    dataMap.put("success", "false");
                    dataMap.put("msg", contractResult.getErrorMessage());
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeLocalUnconfirmedCreateContractTransaction(String sender, String contractAddress) {
        lock.lock();
        try {
            Map<String, Map<String, String>> unconfirmedOfAccountMap = LOCAL_UNCONFIRMED_CREATE_CONTRACT_TRANSACTION.get(sender);
            if (unconfirmedOfAccountMap == null) {
                return;
            }
            unconfirmedOfAccountMap.remove(contractAddress);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeLocalFailedUnconfirmedCreateContractTransaction(String sender, String contractAddress) {
        lock.lock();
        try {
            Map<String, Map<String, String>> unconfirmedOfAccountMap = LOCAL_UNCONFIRMED_CREATE_CONTRACT_TRANSACTION.get(sender);
            if (unconfirmedOfAccountMap == null) {
                return;
            }
            Map<String, String> dataMap = unconfirmedOfAccountMap.get(contractAddress);
            if (dataMap != null) {
                String success = dataMap.get("success");
                if ("false".equals(success)) {
                    unconfirmedOfAccountMap.remove(contractAddress);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 预创建生成智能合约的交易
     * 用于测试合约是否能正确创建
     *
     * @param sender       交易创建者
     * @param gasLimit     最大gas消耗
     * @param price        执行合约单价
     * @param contractCode 合约代码
     * @param args         参数列表
     * @param password     账户密码
     * @param remark       备注
     * @return
     */
    @Override
    public Result contractPreCreateTx(String sender, Long gasLimit, Long price,
                                      byte[] contractCode, String[][] args,
                                      String password, String remark) {
        try {
            AssertUtil.canNotEmpty(sender, "the sender address can not be empty");
            AssertUtil.canNotEmpty(contractCode, "the contractCode can not be empty");
            Na value = Na.ZERO;

            Result<Account> accountResult = accountService.getAccount(sender);
            if (accountResult.isFailed()) {
                return accountResult;
            }

            /*Account account = accountResult.getData();
            // 验证账户密码
            if (accountService.isEncrypted(account).isSuccess() && account.isLocked()) {
                AssertUtil.canNotEmpty(password, "the password can not be empty");
                if (!account.validatePassword(password)) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
                }
            }*/

            // 生成一个地址作为智能合约地址
            Address contractAddress = AccountTool.createContractAddress();

            byte[] contractAddressBytes = contractAddress.getAddressBytes();
            byte[] senderBytes = AddressTool.getAddress(sender);

            CreateContractTransaction tx = new CreateContractTransaction();
            if (StringUtils.isNotBlank(remark)) {
                try {
                    tx.setRemark(remark.getBytes(NulsConfig.DEFAULT_ENCODING));
                } catch (UnsupportedEncodingException e) {
                    Log.error(e);
                    throw new RuntimeException(e);
                }
            }
            tx.setTime(TimeService.currentTimeMillis());


            // 计算CoinData
            /*
             * 智能合约计算手续费以消耗的Gas*Price为根据，然而创建交易时并不执行智能合约，
             * 所以此时交易的CoinData是不固定的，比实际要多，
             * 打包时执行智能合约，真实的手续费已算出，然而tx的手续费已扣除，
             * 多扣除的费用会以CoinBase交易还给Sender
             */
            CoinData coinData = new CoinData();
            // 向智能合约账户转账
            if (!Na.ZERO.equals(value)) {
                Coin toCoin = new Coin(contractAddressBytes, value);
                coinData.getTo().add(toCoin);
            }

            BlockHeader blockHeader = NulsContext.getInstance().getBestBlock().getHeader();
            // 当前区块高度
            long blockHeight = blockHeader.getHeight();
            // 当前区块状态根
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);
            AssertUtil.canNotEmpty(prevStateRoot, "All features of the smart contract are locked.");

            // 执行VM验证合法性
            ProgramCreate programCreate = new ProgramCreate();
            programCreate.setContractAddress(contractAddressBytes);
            programCreate.setSender(senderBytes);
            programCreate.setValue(BigInteger.valueOf(value.getValue()));
            programCreate.setPrice(price.longValue());
            programCreate.setGasLimit(gasLimit.longValue());
            programCreate.setNumber(blockHeight);
            programCreate.setContractCode(contractCode);
            if (args != null) {
                programCreate.setArgs(args);
            }
            ProgramExecutor track = programExecutor.begin(prevStateRoot);
            ProgramResult programResult = track.create(programCreate);

            // 执行结果失败时，交易直接返回错误，不上链，不消耗Gas，
            if(!programResult.isSuccess()) {
                Result result = Result.getFailed(ContractErrorCode.DATA_ERROR);
                result.setMsg(ContractUtil.simplifyErrorMsg(programResult.getErrorMessage()));
                return result;
            }
            long gasUsed = gasLimit.longValue();
            Na imputedNa = Na.valueOf(LongUtils.mul(gasUsed, price));
            // 总花费
            Na totalNa = imputedNa.add(value);

            // 组装txData
            CreateContractData createContractData = new CreateContractData();
            createContractData.setSender(senderBytes);
            createContractData.setContractAddress(contractAddressBytes);
            createContractData.setValue(value.getValue());
            createContractData.setGasLimit(gasLimit);
            createContractData.setPrice(price);
            createContractData.setCodeLen(contractCode.length);
            createContractData.setCode(contractCode);
            if (args != null) {
                createContractData.setArgsCount((byte) args.length);
                if (args.length > 0) {
                    createContractData.setArgs(args);
                }
            }
            tx.setTxData(createContractData);

            CoinDataResult coinDataResult = accountLedgerService.getCoinData(senderBytes, totalNa, tx.size(), TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES);
            if (!coinDataResult.isEnough()) {
                return Result.getFailed(TransactionErrorCode.INSUFFICIENT_BALANCE);
            }
            return Result.getSuccess();
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (Exception e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.CONTRACT_TX_CREATE_ERROR);
            result.setMsg(e.getMessage());
            return result;
        }
    }

    /**
     * 创建调用智能合约的交易
     *
     * @param sender          交易创建者
     * @param value           交易附带的货币量
     * @param gasLimit        最大gas消耗
     * @param price           执行合约单价
     * @param contractAddress 合约地址
     * @param methodName      方法名
     * @param methodDesc      方法签名，如果方法名不重复，可以不传
     * @param args            参数列表
     * @param password        账户密码
     * @param remark          备注
     * @return
     */
    @Override
    public Result contractCallTx(String sender, Na value, Long gasLimit, Long price, String contractAddress,
                                 String methodName, String methodDesc, String[][] args,
                                 String password, String remark) {
        try {
            AssertUtil.canNotEmpty(sender, "the sender address can not be empty");
            AssertUtil.canNotEmpty(contractAddress, "the contractAddress can not be empty");
            AssertUtil.canNotEmpty(methodName, "the methodName can not be empty");
            if (value == null) {
                value = Na.ZERO;
            }

            Result<Account> accountResult = accountService.getAccount(sender);
            if (accountResult.isFailed()) {
                return accountResult;
            }

            Account account = accountResult.getData();
            // 验证账户密码
            if (account.isEncrypted() && account.isLocked()) {
                AssertUtil.canNotEmpty(password, "the password can not be empty");
                if (!account.validatePassword(password)) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
                }
            }

            byte[] senderBytes = AddressTool.getAddress(sender);
            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);

            BlockHeader blockHeader = NulsContext.getInstance().getBestBlock().getHeader();
            // 当前区块高度
            long blockHeight = blockHeader.getHeight();
            // 当前区块状态根
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);
            AssertUtil.canNotEmpty(prevStateRoot, "All features of the smart contract are locked.");

            // 组装VM执行数据
            ProgramCall programCall = new ProgramCall();
            programCall.setContractAddress(contractAddressBytes);
            programCall.setSender(senderBytes);
            programCall.setNumber(blockHeight);
            programCall.setMethodName(methodName);
            programCall.setMethodDesc(methodDesc);
            programCall.setArgs(args);

            // 如果方法是不上链的合约调用，同步执行合约代码，不改变状态根，并返回值
            if (vmHelper.checkIsViewMethod(methodName, contractAddressBytes)) {
                programCall.setValue(BigInteger.ZERO);
                programCall.setGasLimit(ContractConstant.CONTRACT_CONSTANT_GASLIMIT);
                programCall.setPrice(ContractConstant.CONTRACT_CONSTANT_PRICE);

                ProgramExecutor track = programExecutor.begin(prevStateRoot);
                ProgramResult programResult = track.call(programCall);
                Result result = null;
                if (!programResult.isSuccess()) {
                    result = Result.getFailed(ContractErrorCode.DATA_ERROR);
                    result.setMsg(ContractUtil.simplifyErrorMsg(programResult.getErrorMessage()));
                } else {
                    result = Result.getSuccess();
                    result.setData(programResult.getResult());
                }
                return result;
            }


            // 创建链上交易，包含智能合约
            programCall.setValue(BigInteger.valueOf(value.getValue()));
            programCall.setPrice(price.longValue());
            programCall.setGasLimit(gasLimit.longValue());

            CallContractTransaction tx = new CallContractTransaction();
            if (StringUtils.isNotBlank(remark)) {
                try {
                    tx.setRemark(remark.getBytes(NulsConfig.DEFAULT_ENCODING));
                } catch (UnsupportedEncodingException e) {
                    Log.error(e);
                    throw new RuntimeException(e);
                }
            }
            tx.setTime(TimeService.currentTimeMillis());

            // 计算CoinData
            /*
             * 智能合约计算手续费以消耗的Gas*Price为根据，然而创建交易时并不执行智能合约，
             * 所以此时交易的CoinData是不固定的，比实际要多，
             * 打包时执行智能合约，真实的手续费已算出，然而tx的手续费已扣除，
             * 多扣除的费用会以CoinBase交易还给Sender
             */
            CoinData coinData = new CoinData();
            // 向智能合约账户转账
            if (!Na.ZERO.equals(value)) {
                Coin toCoin = new Coin(contractAddressBytes, value);
                coinData.getTo().add(toCoin);
            }

            // 执行VM验证合法性
            ProgramExecutor track = programExecutor.begin(prevStateRoot);
            ProgramResult programResult = track.call(programCall);

            // 执行结果失败时，交易直接返回错误，不上链，不消耗Gas
            if(!programResult.isSuccess()) {
                Result result = Result.getFailed(ContractErrorCode.DATA_ERROR);
                result.setMsg(ContractUtil.simplifyErrorMsg(programResult.getErrorMessage()));
                return result;
            }
            /*long gasUsed = programResult.getGasUsed();
            // 预估1.5倍Gas
            gasUsed += gasUsed >> 1;*/
            long gasUsed = gasLimit.longValue();
            Na imputedNa = Na.valueOf(LongUtils.mul(gasUsed, price));
            // 总花费
            Na totalNa = imputedNa.add(value);

            // 组装txData
            CallContractData callContractData = new CallContractData();
            callContractData.setContractAddress(contractAddressBytes);
            callContractData.setSender(senderBytes);
            callContractData.setValue(value.getValue());
            callContractData.setPrice(price.longValue());
            callContractData.setGasLimit(gasLimit.longValue());
            callContractData.setMethodName(methodName);
            callContractData.setMethodDesc(methodDesc);
            if (args != null) {
                callContractData.setArgsCount((byte) args.length);
                callContractData.setArgs(args);
            }
            tx.setTxData(callContractData);

            CoinDataResult coinDataResult = accountLedgerService.getCoinData(senderBytes, totalNa, tx.size() + coinData.size(), TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES);
            if (!coinDataResult.isEnough()) {
                return Result.getFailed(TransactionErrorCode.INSUFFICIENT_BALANCE);
            }
            coinData.setFrom(coinDataResult.getCoinList());
            // 找零的UTXO
            if (coinDataResult.getChange() != null) {
                coinData.getTo().add(coinDataResult.getChange());
            }
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

            //TODO pierre 代码结构需优化，先实现功能
            // 保存未确认Token转账
            byte[] infoKey = null;
            do {
                Result<ContractAddressInfoPo> contractAddressInfoResult = contractAddressStorageService.getContractAddressInfo(contractAddressBytes);
                ContractAddressInfoPo po = contractAddressInfoResult.getData();
                if(po != null && po.isNrc20() &&
                        (ContractConstant.NRC20_METHOD_TRANSFER.equals(methodName)
                                || ContractConstant.NRC20_METHOD_TRANSFER_FROM.equals(methodName))) {
                    byte[] txHashBytes = tx.getHash().serialize();
                    infoKey = ArraysTool.concatenate(senderBytes, txHashBytes, new VarInt(0).encode());
                    ContractTokenTransferInfoPo tokenTransferInfoPo = new ContractTokenTransferInfoPo();
                    if(ContractConstant.NRC20_METHOD_TRANSFER.equals(methodName)) {
                        try {
                            String to = args[0][0];
                            String tokenValue = args[1][0];
                            BigInteger token = new BigInteger(tokenValue);
                            Result result = contractBalanceManager.subtractContractToken(sender, contractAddress, token);
                            if(result.isFailed()) {
                                return result;
                            }
                            contractBalanceManager.addContractToken(to, contractAddress, token);
                            tokenTransferInfoPo.setFrom(senderBytes);
                            tokenTransferInfoPo.setTo(AddressTool.getAddress(to));
                            tokenTransferInfoPo.setValue(token);
                        } catch (Exception e) {
                            Log.error(e);
                            Result result = Result.getFailed(ContractErrorCode.CONTRACT_TX_CREATE_ERROR);
                            result.setMsg(e.getMessage());
                            return result;
                        }
                    } else {
                        try {
                            String from = args[0][0];
                            String to = args[1][0];
                            String tokenValue = args[2][0];
                            BigInteger token = new BigInteger(tokenValue);
                            Result result = contractBalanceManager.subtractContractToken(sender, contractAddress, token);
                            if(result.isFailed()) {
                                return result;
                            }
                            contractBalanceManager.addContractToken(to, contractAddress, token);
                            tokenTransferInfoPo.setFrom(AddressTool.getAddress(from));
                            tokenTransferInfoPo.setTo(AddressTool.getAddress(to));
                            tokenTransferInfoPo.setValue(token);
                        } catch (Exception e) {
                            Log.error(e);
                            Result result = Result.getFailed(ContractErrorCode.CONTRACT_TX_CREATE_ERROR);
                            result.setMsg(e.getMessage());
                            return result;
                        }
                    }

                    tokenTransferInfoPo.setName(po.getNrc20TokenName());
                    tokenTransferInfoPo.setSymbol(po.getNrc20TokenSymbol());
                    tokenTransferInfoPo.setDecimals(po.getDecimals());
                    tokenTransferInfoPo.setTime(tx.getTime());
                    tokenTransferInfoPo.setContractAddress(contractAddressBytes);
                    tokenTransferInfoPo.setBlockHeight(tx.getBlockHeight());
                    tokenTransferInfoPo.setTxHash(txHashBytes);
                    tokenTransferInfoPo.setStatus((byte) 0);
                    Result result = contractTokenTransferStorageService.saveTokenTransferInfo(infoKey, tokenTransferInfoPo);
                    if(result.isFailed()) {
                        return result;
                    }
                }
            } while (false);



            // 保存未确认交易到本地账本
            Result saveResult = accountLedgerService.verifyAndSaveUnconfirmedTransaction(tx);
            if (saveResult.isFailed()) {
                if (infoKey != null) {
                    contractTokenTransferStorageService.deleteTokenTransferInfo(infoKey);
                }
                if (KernelErrorCode.DATA_SIZE_ERROR.getCode().equals(saveResult.getErrorCode().getCode())) {
                    //重新算一次交易(不超出最大交易数据大小下)的最大金额
                    Result rs = accountLedgerService.getMaxAmountOfOnce(senderBytes, tx, TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES);
                    if (rs.isSuccess()) {
                        Na maxAmount = (Na) rs.getData();
                        rs = Result.getFailed(KernelErrorCode.DATA_SIZE_ERROR_EXTEND);
                        rs.setMsg(rs.getMsg() + maxAmount.toDouble());
                    }
                    return rs;
                }
                return saveResult;
            }

            // 广播
            Result sendResult = transactionService.broadcastTx(tx);
            if (sendResult.isFailed()) {
                // 失败则回滚
                accountLedgerService.deleteTransaction(tx);
                if (infoKey != null) {
                    contractTokenTransferStorageService.deleteTokenTransferInfo(infoKey);
                }
                return sendResult;
            }

            return Result.getSuccess().setData(tx.getHash().getDigestHex());
        } catch (IOException e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.CONTRACT_TX_CREATE_ERROR);
            result.setMsg(e.getMessage());
            return result;
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (Exception e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.CONTRACT_TX_CREATE_ERROR);
            result.setMsg(e.getMessage());
            return result;
        }
    }

    public Result transferFee(String sender, Na value, Long gasLimit, Long price, String contractAddress,
                              String methodName, String methodDesc, String[][] args, String remark) {
        try {
            AssertUtil.canNotEmpty(sender, "the sender address can not be empty");
            AssertUtil.canNotEmpty(contractAddress, "the contractAddress can not be empty");
            AssertUtil.canNotEmpty(methodName, "the methodName can not be empty");
            if (value == null) {
                value = Na.ZERO;
            }

            Result<Account> accountResult = accountService.getAccount(sender);
            if (accountResult.isFailed()) {
                return accountResult;
            }

            byte[] senderBytes = AddressTool.getAddress(sender);
            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);

            BlockHeader blockHeader = NulsContext.getInstance().getBestBlock().getHeader();
            // 当前区块高度
            long blockHeight = blockHeader.getHeight();
            // 当前区块状态根
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);
            AssertUtil.canNotEmpty(prevStateRoot, "All features of the smart contract are locked.");

            CallContractTransaction tx = new CallContractTransaction();
            if (StringUtils.isNotBlank(remark)) {
                try {
                    tx.setRemark(remark.getBytes(NulsConfig.DEFAULT_ENCODING));
                } catch (UnsupportedEncodingException e) {
                    Log.error(e);
                    throw new RuntimeException(e);
                }
            }
            tx.setTime(TimeService.currentTimeMillis());

            CoinData coinData = new CoinData();
            // 向智能合约账户转账
            if (!Na.ZERO.equals(value)) {
                Coin toCoin = new Coin(contractAddressBytes, value);
                coinData.getTo().add(toCoin);
            }

            long gasUsed = gasLimit.longValue();
            Na imputedGasUsedNa = Na.valueOf(LongUtils.mul(gasUsed, price));
            // 总花费
            Na totalNa = imputedGasUsedNa.add(value);

            // 组装txData
            CallContractData callContractData = new CallContractData();
            callContractData.setContractAddress(contractAddressBytes);
            callContractData.setSender(senderBytes);
            callContractData.setValue(value.getValue());
            callContractData.setPrice(price.longValue());
            callContractData.setGasLimit(gasLimit.longValue());
            callContractData.setMethodName(methodName);
            callContractData.setMethodDesc(methodDesc);
            if (args != null) {
                callContractData.setArgsCount((byte) args.length);
                callContractData.setArgs(args);
            }
            tx.setTxData(callContractData);

            Na fee = accountLedgerService.getTxFee(senderBytes, totalNa, tx.size() + coinData.size(), TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES);
            fee = fee.add(imputedGasUsedNa);
            return Result.getSuccess().setData(new Object[]{fee, tx});
        } catch (Exception e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.SYS_UNKOWN_EXCEPTION);
            result.setMsg(e.getMessage());
            return result;
        }
    }

    /**
     * 创建删除智能合约的交易
     *
     * @param sender          交易创建者
     * @param contractAddress 合约地址
     * @param password        账户密码
     * @param remark          备注
     * @return
     */
    @Override
    public Result contractDeleteTx(String sender, String contractAddress,
                                   String password, String remark) {
        try {
            AssertUtil.canNotEmpty(sender, "the sender address can not be empty");
            AssertUtil.canNotEmpty(contractAddress, "the contractAddress can not be empty");

            Result<Account> accountResult = accountService.getAccount(sender);
            if (accountResult.isFailed()) {
                return accountResult;
            }

            Account account = accountResult.getData();
            // 验证账户密码
            if (account.isEncrypted() && account.isLocked()) {
                AssertUtil.canNotEmpty(password, "the password can not be empty");
                if (!account.validatePassword(password)) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
                }
            }

            DeleteContractTransaction tx = new DeleteContractTransaction();
            if (StringUtils.isNotBlank(remark)) {
                try {
                    tx.setRemark(remark.getBytes(NulsConfig.DEFAULT_ENCODING));
                } catch (UnsupportedEncodingException e) {
                    Log.error(e);
                    throw new RuntimeException(e);
                }
            }
            tx.setTime(TimeService.currentTimeMillis());

            byte[] senderBytes = AddressTool.getAddress(sender);
            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);

            // 组装txData
            DeleteContractData deleteContractData = new DeleteContractData();
            deleteContractData.setContractAddress(contractAddressBytes);
            deleteContractData.setSender(senderBytes);

            tx.setTxData(deleteContractData);

            // 计算CoinData
            /*
             * 没有Gas消耗，在终止智能合约里
             */
            CoinData coinData = new CoinData();

            // 总花费 终止智能合约的交易手续费按普通交易计算手续费
            CoinDataResult coinDataResult = accountLedgerService.getCoinData(senderBytes, Na.ZERO, tx.size() + coinData.size(), TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES);
            if (!coinDataResult.isEnough()) {
                return Result.getFailed(TransactionErrorCode.INSUFFICIENT_BALANCE);
            }
            coinData.setFrom(coinDataResult.getCoinList());
            // 找零的UTXO
            if (coinDataResult.getChange() != null) {
                coinData.getTo().add(coinDataResult.getChange());
            }
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

            // 保存删除合约的交易到本地账本
            Result saveResult = accountLedgerService.verifyAndSaveUnconfirmedTransaction(tx);
            if (saveResult.isFailed()) {
                if (KernelErrorCode.DATA_SIZE_ERROR.getCode().equals(saveResult.getErrorCode().getCode())) {
                    //重新算一次交易(不超出最大交易数据大小下)的最大金额
                    Result rs = accountLedgerService.getMaxAmountOfOnce(senderBytes, tx, TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES);
                    if (rs.isSuccess()) {
                        Na maxAmount = (Na) rs.getData();
                        rs = Result.getFailed(KernelErrorCode.DATA_SIZE_ERROR_EXTEND);
                        rs.setMsg(rs.getMsg() + maxAmount.toDouble());
                    }
                    return rs;
                }
                return saveResult;
            }
            // 广播交易
            Result sendResult = transactionService.broadcastTx(tx);
            if (sendResult.isFailed()) {
                // 失败则回滚
                accountLedgerService.deleteTransaction(tx);
                return sendResult;
            }
            return Result.getSuccess().setData(tx.getHash().getDigestHex());
        } catch (IOException e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.CONTRACT_TX_CREATE_ERROR);
            result.setMsg(e.getMessage());
            return result;
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (Exception e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.CONTRACT_TX_CREATE_ERROR);
            result.setMsg(e.getMessage());
            return result;
        }
    }

}

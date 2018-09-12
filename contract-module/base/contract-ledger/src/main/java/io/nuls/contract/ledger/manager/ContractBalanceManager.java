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

package io.nuls.contract.ledger.manager;

import io.nuls.account.model.Account;
import io.nuls.account.service.AccountService;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.dto.ContractTokenInfo;
import io.nuls.contract.ledger.module.ContractBalance;
import io.nuls.contract.service.ContractService;
import io.nuls.contract.storage.po.ContractAddressInfoPo;
import io.nuls.contract.storage.service.ContractAddressStorageService;
import io.nuls.contract.storage.service.ContractUtxoStorageService;
import io.nuls.core.tools.log.Log;
import io.nuls.db.model.Entry;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.utils.AddressTool;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.nuls.ledger.util.LedgerUtil.asString;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/7
 */
@Component
public class ContractBalanceManager {

    @Autowired
    private ContractService contractService;

    @Autowired
    private ContractUtxoStorageService contractUtxoStorageService;

    @Autowired
    private ContractAddressStorageService contractAddressStorageService;

    @Autowired
    private AccountService accountService;

    /**
     * key: String - local account address
     *      value:
     *          key: String - contract address
     *          value: ContractTokenInfo - token name && amount
     */
    private Map<String, Map<String, ContractTokenInfo>> contractTokenOfLocalAccount = new ConcurrentHashMap<>();

    private Map<String, ContractBalance> balanceMap;

    private Lock lock = new ReentrantLock();

    private Lock tokenLock = new ReentrantLock();

    private ThreadLocal<Map<String, ContractBalance>> tempBalanceMapManager = new ThreadLocal<>();

    public void createTempBalanceMap() {
        tempBalanceMapManager.remove();
        tempBalanceMapManager.set(new ConcurrentHashMap<>());
    }

    public void removeTempBalanceMap() {
        tempBalanceMapManager.remove();
    }

    /**
     * 初始化缓存本地所有合约账户的余额信息
     */
    public void initContractBalance() {
        balanceMap = new ConcurrentHashMap<>();
        List<Coin> coinList = new ArrayList<>();
        List<Entry<byte[], byte[]>> rawList = contractUtxoStorageService.loadAllCoinList();
        Coin coin;
        String strAddress;
        ContractBalance balance;
        byte[] fromOwner;
        for (Entry<byte[], byte[]> coinEntry : rawList) {
            coin = new Coin();
            try {
                coin.parse(coinEntry.getValue(), 0);
                //strAddress = asString(coin.getOwner());
                strAddress = asString(coin.getAddress());
            } catch (NulsException e) {
                Log.error("parse contract coin error form db", e);
                continue;
            }
            balance = balanceMap.get(strAddress);
            if(balance == null) {
                balance = new ContractBalance();
                balanceMap.put(strAddress, balance);
            }
            if (coin.usable()) {
                balance.addUsable(coin.getNa());
            } else {
                balance.addLocked(coin.getNa());
            }
        }
    }

    /**
     * 获取账户余额
     *
     * @param address
     * @return
     */
    public Result<ContractBalance> getBalance(byte[] address) {
        lock.lock();
        try {
            if (address == null || address.length != Address.ADDRESS_LENGTH) {
                return Result.getFailed(ContractErrorCode.PARAMETER_ERROR);
            }

            String addressKey = asString(address);
            ContractBalance balance = null;
            // 打包或验证区块前创建一个临时余额区，实时更新余额，打包完或验证区块后移除
            Map<String, ContractBalance> tempBalanceMap = tempBalanceMapManager.get();
            if(tempBalanceMap != null) {
                balance = tempBalanceMap.get(addressKey);
                // 临时余额区没有余额，则从真实余额中取值
                if(balance == null) {
                    balance = balanceMap.get(addressKey);
                    // 真实余额区也没有值时，初始化真实余额区和临时余额区
                    if (balance == null) {
                        balanceMap.put(addressKey, new ContractBalance());
                        balance = new ContractBalance();
                        tempBalanceMap.put(addressKey, balance);
                    } else {
                        // 真实余额区有值时，深度复制这个值到临时余额区，避免真实余额被临时余额所影响
                        balance = depthClone(balance);
                        tempBalanceMap.put(addressKey, balance);
                    }
                }
            } else {
                balance = balanceMap.get(addressKey);
                if (balance == null) {
                    balance = new ContractBalance();
                    balanceMap.put(addressKey, balance);
                }
            }

            return Result.getSuccess().setData(balance);
        } finally {
            lock.unlock();
        }
    }

    private ContractBalance depthClone(ContractBalance contractBalance) {
        if(contractBalance == null) {
            return null;
        }
        ContractBalance result = new ContractBalance(Na.valueOf(contractBalance.getUsable().getValue()),
                Na.valueOf(contractBalance.getLocked().getValue()));
        return result;
    }

    public void addTempBalance(byte[] address, long amount) {
        Map<String, ContractBalance> tempBalanceMap = tempBalanceMapManager.get();
        String addressKey = asString(address);
        ContractBalance contractBalance = tempBalanceMap.get(addressKey);

        if(contractBalance != null) {
            contractBalance.addUsable(Na.valueOf(amount));
        }
    }

    public void minusTempBalance(byte[] address, long amount) {
        Map<String, ContractBalance> tempBalanceMap = tempBalanceMapManager.get();
        String addressKey = asString(address);
        ContractBalance contractBalance = tempBalanceMap.get(addressKey);

        if(contractBalance != null) {
            contractBalance.minusUsable(Na.valueOf(amount));
        }
    }



    /**
     * 刷新余额
     */
    public void refreshBalance(List<Coin> addUtxoList, List<Coin> deleteUtxoList) {
        lock.lock();
        try {
            //Log.info("=========================================刷新余额开始+");
            ContractBalance balance;
            String strAddress;

            if(deleteUtxoList != null) {
                //Log.info("减少UTXO数量: {}", deleteUtxoList.size());
                for (Coin coin : deleteUtxoList) {
                    strAddress = asString(coin.getTempOwner());
                    balance = balanceMap.get(strAddress);
                    if(balance == null) {
                        balance = new ContractBalance();
                        balanceMap.put(strAddress, balance);
                    }
                    if (coin.usable()) {
                        balance.minusUsable(coin.getNa());
                        //Log.info("====[{}]减少可用余额: {}, 当前余额: {}",
                        //        AddressTool.getStringAddressByBytes(coin.getOwner()),
                        //        coin.getNa(),
                        //        balance.getUsable());
                    } else {
                        balance.minusLocked(coin.getNa());
                        //Log.info("====[{}]减少锁定余额: {}, 当前余额: {}",
                        //        AddressTool.getStringAddressByBytes(coin.getOwner()),
                        //        coin.getNa(),
                        //        balance.getUsable());
                    }
                }
            }

            if(addUtxoList != null) {
                //Log.info("增加UTXO数量: {}", addUtxoList.size());
                for (Coin coin : addUtxoList) {
                    strAddress = asString(coin.getTempOwner());
                    balance = balanceMap.get(strAddress);
                    if(balance == null) {
                        balance = new ContractBalance();
                        balanceMap.put(strAddress, balance);
                    }
                    if (coin.usable()) {
                        balance.addUsable(coin.getNa());
                        //Log.info("====[{}]增加可用余额: {}, 当前可用余额: {}",
                        //        AddressTool.getStringAddressByBytes(coin.()),
                        //        coin.getNa(),
                        //        balance.getUsable());
                    } else {
                        balance.addLocked(coin.getNa());
                        //Log.info("====[{}]增加锁定余额: {}, 当前余额: {}",
                        //        AddressTool.getStringAddressByBytes(coin.()),
                        //        coin.getNa(),
                        //        balance.getUsable());
                    }
                }
            }

            //Log.info("=========================================刷新余额结束+");
        } finally {
            lock.unlock();
        }
    }

    public List<Coin> getCoinListByAddress(byte[] address) {
        List<Coin> coinList = new ArrayList<>();
        List<Entry<byte[], byte[]>> rawList = contractUtxoStorageService.loadAllCoinList();
        for (Entry<byte[], byte[]> coinEntry : rawList) {
            Coin coin = new Coin();
            try {
                coin.parse(coinEntry.getValue(), 0);
            } catch (NulsException e) {
                Log.info("parse coin form db error");
                continue;
            }
            //if (Arrays.equals(coin.(), address))
            if (Arrays.equals(coin.getAddress(), address))
            {
                coin.setOwner(coinEntry.getKey());
                coin.setKey(asString(coinEntry.getKey()));
                coinList.add(coin);
            }
        }
        return coinList;
    }

    public void initialContractToken(String account, String contract) {
        tokenLock.lock();
        try {
            Result<ContractTokenInfo> result = contractService.getContractTokenViaVm(account, contract);
            if(result.isFailed()) {
                return;
            }
            ContractTokenInfo tokenInfo = result.getData();
            BigInteger amount = tokenInfo.getAmount();
            if(amount == null || amount.equals(BigInteger.ZERO)) {
                return;
            }
            Map<String, ContractTokenInfo> tokens = contractTokenOfLocalAccount.get(account);
            if(tokens == null) {
                tokens = new HashMap<>();
            }
            tokens.put(contract, tokenInfo);
            contractTokenOfLocalAccount.put(account, tokens);
        } finally {
            tokenLock.unlock();
        }
    }

    public void refreshContractToken(String account, String contract, ContractAddressInfoPo po, BigInteger value) {
        tokenLock.lock();
        try {
            ContractTokenInfo tokenInfo = new ContractTokenInfo(contract, po.getNrc20TokenName(), po.getDecimals(), value, po.getNrc20TokenSymbol(), po.getBlockHeight());
            Map<String, ContractTokenInfo> tokens = contractTokenOfLocalAccount.get(account);
            if(tokens == null) {
                tokens = new HashMap<>();
            }
            tokens.put(contract, tokenInfo);
            contractTokenOfLocalAccount.put(account, tokens);
        } finally {
            tokenLock.unlock();
        }
    }

    public void initAllTokensForAllAccounts() {
        Result<Collection<Account>> result = accountService.getAccountList();
        if(result.isFailed()) {
            return;
        }
        Result<List<ContractAddressInfoPo>> allContractInfoListResult = contractAddressStorageService.getAllNrc20ContractInfoList();
        if(allContractInfoListResult.isFailed()) {
            return;
        }
        List<ContractAddressInfoPo> contractAddressInfoPoList = allContractInfoListResult.getData();

        Collection<Account> list = result.getData();
        for(Account account : list) {
            Address address = account.getAddress();
            String addressStr = address.getBase58();
            for(ContractAddressInfoPo po : contractAddressInfoPoList) {
                initialContractToken(addressStr, AddressTool.getStringAddressByBytes(po.getContractAddress()));
            }
        }
    }

    public void initAllTokensByAccount(String account) {
        if(!AddressTool.validAddress(account)) {
            return;
        }
        Result<List<ContractAddressInfoPo>> allContractInfoListResult = contractAddressStorageService.getAllNrc20ContractInfoList();
        if(allContractInfoListResult.isFailed()) {
            return;
        }
        List<ContractAddressInfoPo> contractAddressInfoPoList = allContractInfoListResult.getData();
        for(ContractAddressInfoPo po : contractAddressInfoPoList) {
            initialContractToken(account, AddressTool.getStringAddressByBytes(po.getContractAddress()));
        }
    }

    public Result<List<ContractTokenInfo>> getAllTokensByAccount(String account) {
        Map<String, ContractTokenInfo> tokensMap = contractTokenOfLocalAccount.get(account);
        if(tokensMap == null || tokensMap.size() == 0) {
            return Result.getSuccess().setData(new ArrayList<>());
        }
        List<ContractTokenInfo> resultList = new ArrayList<>();
        Set<Map.Entry<String, ContractTokenInfo>> entries = tokensMap.entrySet();
        String contractAddress;
        ContractTokenInfo info;
        for(Map.Entry<String, ContractTokenInfo> entry : entries) {
            contractAddress = entry.getKey();
            info = entry.getValue();
            info.setContractAddress(contractAddress);
            resultList.add(info);
        }
        return Result.getSuccess().setData(resultList);
    }

    public Result subtractContractToken(String account, String contract, BigInteger token) {
        tokenLock.lock();
        try {
            Map<String, ContractTokenInfo> tokens = contractTokenOfLocalAccount.get(account);
            if(tokens == null) {
                return Result.getSuccess();
            } else {
                ContractTokenInfo info = tokens.get(contract);
                if(info == null) {
                    return Result.getSuccess();
                }
                BigInteger currentToken = info.getAmount();
                if(currentToken == null) {
                    return Result.getSuccess();
                } else {
                    if(currentToken.compareTo(token) < 0) {
                        return Result.getFailed(ContractErrorCode.INSUFFICIENT_BALANCE);
                    }
                    currentToken = currentToken.subtract(token);
                    tokens.put(contract, info.setAmount(currentToken));
                }
            }
            return Result.getSuccess();
        } finally {
            tokenLock.unlock();
        }
    }

    public Result addContractToken(String account, String contract, BigInteger token) {
        tokenLock.lock();
        try {
            Map<String, ContractTokenInfo> tokens = contractTokenOfLocalAccount.get(account);
            do {
                if(tokens == null) {
                    break;
                } else {
                    ContractTokenInfo info = tokens.get(contract);
                    if(info == null) {
                        return Result.getSuccess();
                    }
                    BigInteger currentToken = info.getAmount();
                    if(currentToken == null) {
                        break;
                    } else {
                        currentToken = currentToken.add(token);
                        tokens.put(contract, info.setAmount(currentToken));
                    }
                }
            } while(false);
        } finally {
            tokenLock.unlock();
        }
        return Result.getSuccess();
    }
}

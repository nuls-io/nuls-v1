package io.nuls.account.ledger.service.Balance.impl;

import io.nuls.account.ledger.constant.AccountLedgerErrorCode;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.ledger.service.Balance.BalanceService;
import io.nuls.account.model.Balance;
import io.nuls.account.ledger.storage.service.AccountLedgerStorageService;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.utils.AddressTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * author Facjas
 * date 2018/5/15.
 */
@Component
public class BalanceServiceImpl extends Thread implements BalanceService {
    @Autowired
    AccountLedgerService accountLedgerService;

    @Autowired
    private AccountLedgerStorageService storageService;

    private static boolean needToReloadBalance = false;
    private static List<String> addressNeedtoReloadBalance = new ArrayList<>();
    private static Map<String, Balance> balanceMap;

    @Override
    public void run() {
        while (true) {
            if (!needToReloadBalance) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                if (addressNeedtoReloadBalance == null || addressNeedtoReloadBalance.size() == 0) {
                    continue;
                }
                List<String> reloadedAddresses = new ArrayList<>();
                for (String address : addressNeedtoReloadBalance) {
                    reloadAccountBalance(address);
                    reloadedAddresses.add(address);
                }
                addressNeedtoReloadBalance.removeAll(reloadedAddresses);
                setNeedToReloadBalance(false);
            }
        }
    }

    public static boolean isNeedToReloadBalance() {
        return needToReloadBalance;
    }

    public static void setNeedToReloadBalance(boolean needToReloadBalance) {
        BalanceServiceImpl.needToReloadBalance = needToReloadBalance;
    }

    @Override
    public Balance getBalance(String address) {
        Balance balance = null;
        try {
            balance = getBalance(Base58.decode(address)).getData();
        } catch (Exception e) {
            Log.info("getBalance of address[" + address + "] failed");
            return null;
        }
        return balance;
    }

    @Override
    public Result<Balance> getBalance(byte[] address) {
        if (address == null || address.length != AddressTool.HASH_LENGTH) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR);
        }

        if (!accountLedgerService.isLocalAccount(address)) {
            return Result.getFailed(AccountLedgerErrorCode.ACCOUNT_NOT_EXIST);
        }

        Balance balance = balanceMap.get(Base58.encode(address));

        if (balance == null) {
            return Result.getFailed(AccountLedgerErrorCode.ACCOUNT_NOT_EXIST);
        }

        return Result.getSuccess().setData(balance);
    }

    @Override
    public void reloadAccountBalance(List<String> addresses) {
        setNeedToReloadBalance(true);
        for (String address : addresses) {
            if (!addressNeedtoReloadBalance.contains(address)) {
                addressNeedtoReloadBalance.add(address);
            }
        }
    }

    public void reloadAccountBalance(String address) {
        Balance balance = null;
        try {
            balance = getBalanceByAddress(Base58.decode(address)).getData();
        } catch (Exception e) {
            Log.info("load balance of address[" + address + "]failed");
        }
        balanceMap.put(address, balance);
    }

    protected Result<Balance> getBalanceByAddress(byte[] address) throws NulsException {
        if (accountLedgerService.isLocalAccount(address)) {
            return Result.getFailed(AccountLedgerErrorCode.ACCOUNT_NOT_EXIST);
        }
        List<Coin> coinList = storageService.getCoinBytes(address);
        Balance balance = new Balance();
        long usable = 0;
        long locked = 0;

        long currentTime = System.currentTimeMillis();
        //long bestHeight = NulsContext.getInstance().getBestHeight();
        long bestHeight = 1;

        for (Coin coin : coinList) {
            if (coin.getLockTime() < 0) {
                locked += coin.getNa().getValue();
            } else if (coin.getLockTime() == 0) {
                usable += coin.getNa().getValue();
            } else {
                if (coin.getLockTime() > NulsConstant.BlOCKHEIGHT_TIME_DIVIDE) {
                    if (coin.getLockTime() <= currentTime) {
                        usable += coin.getNa().getValue();
                    } else {
                        locked += coin.getNa().getValue();
                    }
                } else {
                    if (coin.getLockTime() <= bestHeight) {
                        usable += coin.getNa().getValue();
                    } else {
                        locked += coin.getNa().getValue();
                    }
                }
            }
        }

        balance.setUsable(Na.valueOf(usable));
        balance.setLocked(Na.valueOf(locked));
        balance.setBalance(balance.getUsable().add(balance.getLocked()));
        Result<Balance> result = new Result<>();
        result.setData(balance);
        return result;
    }
}

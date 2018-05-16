package io.nuls.account.ledger.base.service.balance;

import io.nuls.account.ledger.constant.AccountLedgerErrorCode;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.model.Account;
import io.nuls.account.model.Balance;
import io.nuls.account.ledger.storage.service.AccountLedgerStorageService;
import io.nuls.account.service.AccountService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author Facjas
 * date 2018/5/15.
 */
@Component
public class BalanceCalculator extends Thread{

    @Autowired
    AccountLedgerService accountLedgerService;

    @Autowired
    AccountLedgerStorageService storageService;

    @Autowired
    AccountService accountService;

    final static int WAITING = 0;
    final static int LOADING = 1;

    @Override
    public void run() {
        while (true) {
            if (getStatus() == WAITING) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if(getStatus() == LOADING){
                reloadAccountBalance();
                setStatus(LOADING);
            }
        }
    }

    private static List<byte[]> addressNeedtoReloadBalance = new ArrayList<>();

    private static Map<String, Balance> balanceMap = new HashMap<>();

    private static int status = LOADING;

    public Result<Balance> getBalance(String address) {
        Result result = null;
        try {
            result = getBalance(Base58.decode(address));
        } catch (Exception e) {
            Log.info("getBalance of address[" + address + "] failed");
            return null;
        }
        return result;
    }

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

    public void reloadAccountBalance() {
        Map<String, Balance> newbalanceMap = new HashMap<>();
        addressNeedtoReloadBalance.clear();
        List<Account> accounts = accountService.getAccountList().getData();
        if (accounts != null) {
            for (Account account : accounts) {
                addressNeedtoReloadBalance.add(account.getAddress().getBase58Bytes());
            }
        }
        if (addressNeedtoReloadBalance == null || addressNeedtoReloadBalance.size() == 0) {
            return;
        }

        //todo need to improve performance
        for(byte[] address:addressNeedtoReloadBalance){
            try {
                Balance balance = getBalanceByAddress(address).getData();
                newbalanceMap.put(Base58.encode(address),balance);
            }catch (NulsException e){
                Log.info("getbalance of address["+Base58.encode(address)+"] error");
            }
        }
        balanceMap = newbalanceMap;
        return;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        BalanceCalculator.status = status;
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

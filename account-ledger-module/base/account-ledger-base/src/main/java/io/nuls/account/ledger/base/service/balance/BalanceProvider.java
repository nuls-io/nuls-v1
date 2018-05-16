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
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.func.TimeService;
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
public class BalanceProvider extends Thread {

    private final static int STATUS_NEWEST = 1;
    private final static int STATUS_EXPIRED = 2;

    @Autowired
    private AccountLedgerService accountLedgerService;
    @Autowired
    private AccountLedgerStorageService storageService;
    @Autowired
    private AccountService accountService;

    private List<byte[]> addressList = new ArrayList<>();
    private Map<String, Balance> balanceMap = new HashMap<>();
    private Map<String, Integer> statusMap = new HashMap<>();

    @Override
    public void run() {
        initAccountsBalance();
    }

    public Result<Balance> getBalance(String address) {
        Result result = null;
        try {
            result = getBalance(Base58.decode(address));
        } catch (Exception e) {
            Log.info("getBalance of address[" + address + "] failed");
            return Result.getFailed(e.getMessage());
        }
        return result;
    }

    public Result<Balance> getBalance(byte[] address) {
        if (address == null || address.length != AddressTool.HASH_LENGTH) {
            return Result.getFailed(AccountLedgerErrorCode.PARAMETER_ERROR);
        }

        String addressKey = new String(address);
        Integer status = statusMap.get(addressKey);

        Balance balance = null;
        if(status == null || status.intValue() == STATUS_EXPIRED) {
            try {
                balance = loadBalanceByAddress(address);
            } catch (NulsException e) {
                Log.error(e);
                return Result.getFailed(e.getMessage());
            }
        } else {
            balance = balanceMap.get(addressKey);
        }

        if (balance == null) {
            return Result.getFailed(AccountLedgerErrorCode.ACCOUNT_NOT_EXIST);
        }

        return Result.getSuccess().setData(balance);
    }

    public Result refreshBalance(byte[] address) {

        if(address == null) {
            // FIXME 当address为null时刷新所有，慎重使用
            statusMap.clear();
            return Result.getSuccess();
        }

        String addressKey = new String(address);
        Integer status = statusMap.get(addressKey);

        if(status != null && status.intValue() == STATUS_NEWEST) {
            statusMap.put(addressKey, STATUS_EXPIRED);
        }

        return Result.getSuccess();
    }

    protected void initAccountsBalance() {

        addressList.clear();
        balanceMap.clear();

        List<Account> accounts = accountService.getAccountList().getData();
        if(accounts == null) {
            return;
        }
        for (Account account : accounts) {
            addressList.add(account.getAddress().getBase58Bytes());
        }
        for (byte[] address : addressList) {
            try {
                loadBalanceByAddress(address);
            }catch (NulsException e){
                Log.info("getbalance of address["+Base58.encode(address)+"] error");
            }
        }
    }

    protected Balance loadBalanceByAddress(byte[] address) throws NulsException {
        if (!accountLedgerService.isLocalAccount(address)) {
            return null;
        }
        List<Coin> coinList = storageService.getCoinBytes(address);

        long currentTime = TimeService.currentTimeMillis();
        long bestHeight = NulsContext.getInstance().getBestHeight();
        long usable = 0;
        long locked = 0;

        for (Coin coin : coinList) {
            if (coin.getLockTime() < 0L) {
                locked += coin.getNa().getValue();
            } else if (coin.getLockTime() == 0L) {
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

        Balance balance = new Balance();
        balance.setUsable(Na.valueOf(usable));
        balance.setLocked(Na.valueOf(locked));
        balance.setBalance(balance.getUsable().add(balance.getLocked()));

        String addressKey = new String(address);
        statusMap.put(addressKey, STATUS_NEWEST);
        balanceMap.put(addressKey, balance);

        return balance;
    }

}

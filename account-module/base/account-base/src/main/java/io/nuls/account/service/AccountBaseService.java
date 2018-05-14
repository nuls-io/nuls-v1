package io.nuls.account.service;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Account;
import io.nuls.account.model.Address;
import io.nuls.account.storage.po.AccountPo;
import io.nuls.account.storage.service.AccountStorageService;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;


/**
 * @author: Charlie
 * @date: 2018/5/14
 */
@Component
public class AccountBaseService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountStorageService accountStorageService;

    /**
     * 获取账户私钥
     * @param address
     * @param password
     * @return
     */
    public Result getPrivateKey(String address, String password) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Account account = accountService.getAccount(address).getData();

        if (!account.isLocked()) {
            return Result.getSuccess().setData(Hex.encode(account.getPriKey()));
        } else {
            try {
                if (!account.unlock(password)) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
                }
                byte[] priKeyBytes = account.getPriKey();
                account.lock();
                return Result.getSuccess().setData(Hex.encode(priKeyBytes));
            } catch (NulsException e) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
        }
    }

    /**
     * 设置默认账户
     * @param address
     * @return
     */
    public Result setDefaultAccount(String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Account account = accountService.getAccount(address).getData();
        if (null != account) {
            Result.getFailed(AccountErrorCode.FAILED, "The account not exist, address:" + address);
        }
        accountStorageService.saveDefaultAccount(new AccountPo(account));
        AccountConstant.DEFAULT_ACCOUNT = account;
        return Result.getSuccess();
    }

    /**
     * 修改账户密码
     * @param oldPassword
     * @param newPassword
     * @return
     */
    public Result changePassword(String address, String oldPassword, String newPassword) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (!StringUtils.validPassword(oldPassword)) {
            return new Result(false, "Length between 8 and 20, the combination of characters and numbers");
        }
        if (!StringUtils.validPassword(newPassword)) {
            return new Result(false, "Length between 8 and 20, the combination of characters and numbers");
        }
        Account account = accountService.getAccount(address).getData();
        if (null != account) {
            Result.getFailed(AccountErrorCode.FAILED, "The account not exist, address:" + address);
        }

       /* List<Account> accounts = accountService.getAccountList().getData();
        if (accounts == null || accounts.isEmpty()) {
            new Result(false, "No account was found");
        }*/

        try {
            if (!account.isEncrypted()) {
                return new Result(false, "No password has been set up yet");
            }

//            List<AccountPo> accountPoList = new ArrayList<>();
//            for (Account account : accounts) {
                if (!account.unlock(oldPassword)) {
                    return new Result(false, "old password error");
                }
                account.encrypt(newPassword, true);

                AccountPo po = new AccountPo(account);
//                AccountTool.toPojo(account, po);
//                accountPoList.add(po);
//            }

//            if (accountPoList.size() > 0) {
//                accountDao.update(accountPoList);
//            }
            accountStorageService.updateAccount(po);
            //accountCacheService.putAccountList(accounts);
        } catch (Exception e) {
            Log.error(e);
            return new Result(false, "change password failed");
        }
        //this.eventBroadcaster.publishToLocal(new PasswordChangeNotice());
        return new Result(true, "OK");
    }
}

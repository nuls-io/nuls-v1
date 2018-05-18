package io.nuls.account.service;

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
        if(null == account){
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
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
     * 设置密码
     * @param address
     * @param password
     * @return
     */
    public Result setPassword(String address, String password) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if(StringUtils.isBlank(password)){
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR,"The password is required");
        }
        if (!StringUtils.validPassword(password)) {
            return new Result(false, "Length between 8 and 20, the combination of characters and numbers");
        }
        Account account = accountService.getAccount(address).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.FAILED, "The account not exist, address:" + address);
        }
        if(account.isEncrypted()){
            return Result.getFailed(AccountErrorCode.ACCOUNT_IS_ALREADY_ENCRYPTED, "The account has been set to password.");
        }
        try {
            account.encrypt(password);
            accountStorageService.updateAccount(new AccountPo(account));
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed();
        }
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
        if(StringUtils.isBlank(oldPassword)){
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR,"The oldPassword is required");
        }
        if(StringUtils.isBlank(newPassword)){
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR,"The newPassword is required");
        }
        if (!StringUtils.validPassword(oldPassword)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG, "oldPassword Length between 8 and 20, the combination of characters and numbers");
        }
        if (!StringUtils.validPassword(newPassword)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG, "newPassword Length between 8 and 20, the combination of characters and numbers");
        }
        Account account = accountService.getAccount(address).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST, "The account not exist, address:" + address);
        }
        try {
            if (!account.isEncrypted()) {
                return Result.getFailed(AccountErrorCode.FAILED, "No password has been set up yet");
            }
            if (!account.unlock(oldPassword)) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG, "old password error");
            }
            account.encrypt(newPassword, true);
            AccountPo po = new AccountPo(account);
            return accountStorageService.updateAccount(po);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.FAILED, "change password failed");
        }
    }
}

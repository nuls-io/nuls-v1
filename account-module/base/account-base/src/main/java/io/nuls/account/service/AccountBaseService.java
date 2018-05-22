package io.nuls.account.service;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Account;
import io.nuls.account.model.Address;
import io.nuls.account.storage.po.AccountPo;
import io.nuls.account.storage.service.AccountStorageService;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.utils.SerializeUtils;

import java.math.BigInteger;


/**
 * @author: Charlie
 * @date: 2018/5/14
 */
@Service
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
        //加过密(有密码)并且没有解锁, 就验证密码 Already encrypted(Added password) and did not unlock, verify password
        if (account.isEncrypted() && account.isLocked()) {
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
        } else {
            return Result.getSuccess().setData(Hex.encode(account.getPriKey()));
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
     * 根据原密码修改账户密码
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
            return Result.getFailed(AccountErrorCode.FAILED, "The oldPassword is wrong, change password failed");
        }
    }

    /**
     * 根据私钥修改账户密码
     * Modify the account password based on the private key
     *
     * @param prikey
     * @param newPassword
     * @return
     */
    public Result changePasswordByPrikey(String address, String prikey, String newPassword) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if(StringUtils.isBlank(prikey)){
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR,"The prikey is required");
        }
        if(StringUtils.isBlank(newPassword)){
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR,"The newPassword is required");
        }
        if (!StringUtils.validPassword(newPassword)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG, "newPassword Length between 8 and 20, the combination of characters and numbers");
        }
        Account account = accountService.getAccount(address).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST, "The account not exist, address:" + address);
        }
        if (!ECKey.isValidPrivteHex(prikey)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR, "The prikey is wrong");
        }
        try {
            ECKey key = ECKey.fromPrivate(new BigInteger(Hex.decode(prikey)));
            Address addr = new Address(NulsContext.DEFAULT_CHAIN_ID, SerializeUtils.sha256hash160(key.getPubKey()));
            if(addr.toString().equals(account.getAddress().toString())) {
                return Result.getFailed(AccountErrorCode.PARAMETER_ERROR, "The prikey is wrong");
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

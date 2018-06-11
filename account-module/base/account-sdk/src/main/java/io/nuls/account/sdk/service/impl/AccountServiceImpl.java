package io.nuls.account.sdk.service.impl;

import io.nuls.account.sdk.constant.AccountErrorCode;
import io.nuls.account.sdk.model.Account;
import io.nuls.account.sdk.service.AccountService;
import io.nuls.account.sdk.util.AccountTool;
import io.nuls.sdk.exception.NulsException;
import io.nuls.sdk.model.Result;
import io.nuls.sdk.utils.JSONUtils;
import io.nuls.sdk.utils.RestFulUtils;
import io.nuls.sdk.utils.StringUtils;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/6/10
 */
public class AccountServiceImpl implements AccountService {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public Result createAccount() {
        return createAccount(1, null);
    }

    @Override
    public Result createAccount(String password) {
        return createAccount(1, password);
    }

    @Override
    public Result createAccount(int count) {
        return createAccount(count, null);
    }

    @Override
    public Result createAccount(int count, String password) {
        if (count < 1) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        if (!StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_FORMAT_WRONG);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("password", password);
        parameters.put("count", count);
        Result result = restFul.post("/account", parameters);
        try {
            System.out.println(JSONUtils.obj2json(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }

    @Override
    public Result createLocalAccount() {
        return null;
    }

    @Override
    public Result createLocalAccount(String password) {
        return null;
    }

    @Override
    public Result createLocalAccount(int count) {
        return null;
    }

    @Override
    public Result createLocalAccount(int count, String password) {
        if (count <= 0 || count > AccountTool.CREATE_MAX_SIZE) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR, "between 0 and 100 can be created at once");
        }
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG, "Length between 8 and 20, the combination of characters and numbers");
        }

        List<Account> accounts = new ArrayList<>();
        try {
            for (int i = 0; i < count; i++) {
                Account account = AccountTool.createAccount();
                if (StringUtils.isNotBlank(password)) {
                    account.encrypt(password);
                }
                accounts.add(account);
            }
        } catch (NulsException e) {
            return Result.getFailed();
        }
        return Result.getSuccess().setData(accounts);
    }

    @Override
    public Result backupAccount(String address, String password) {
        return null;
    }

    @Override
    public Result getAliasFee(String address, String alias) {
        return null;
    }

    @Override
    public Result getAccount(String address) {
        return null;
    }

    @Override
    public Result getAccountList(int pageNumber, int pageSize) {
        return null;
    }

    @Override
    public Result getAssets(String address) {
        return null;
    }

    @Override
    public Result getAddressByAlias(String alias) {
        return null;
    }

    @Override
    public Result getPrikey(String address, String password) {
        return null;
    }

    @Override
    public Result getLocalTotalBalance() {
        return null;
    }

    @Override
    public Result isAliasExist(String alias) {
        return null;
    }

    @Override
    public Result importAccountByKeystore(String path, String password, boolean overwrite) {
        return null;
    }

    @Override
    public Result importAccountByKeystore(FileReader fileReader, String password, boolean overwrite) {
        return null;
    }

    @Override
    public Result importAccountByPriKey(String privateKey, String password, boolean overwrite) {
        return null;
    }

    @Override
    public Result isEncrypted(String address) {
        return null;
    }

    @Override
    public Result lockAccount(String address) {
        return null;
    }

    @Override
    public Result unlockAccount(String address, String password, int unlockTime) {
        return null;
    }

    @Override
    public Result removeAccount(String address, String password) {
        return null;
    }

    @Override
    public Result setPassword(String address, String password) {
        return null;
    }

    @Override
    public Result updatePassword(String address, String oldPassword, String password) {
        return null;
    }

    @Override
    public Result updatePasswordByKeystore(FileReader fileReader, String password) {
        return null;
    }

    @Override
    public Result setAlias(String address, String alias, String password) {
        return null;
    }
}

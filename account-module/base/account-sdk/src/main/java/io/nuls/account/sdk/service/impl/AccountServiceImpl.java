package io.nuls.account.sdk.service.impl;

import io.nuls.account.sdk.constant.AccountConstant;
import io.nuls.account.sdk.constant.AccountErrorCode;
import io.nuls.account.sdk.model.Account;
import io.nuls.account.sdk.model.Address;
import io.nuls.account.sdk.model.dto.AccountDto;
import io.nuls.account.sdk.model.dto.AccountKeyStoreDto;
import io.nuls.account.sdk.service.AccountService;
import io.nuls.account.sdk.util.AccountTool;
import io.nuls.sdk.SDKBootstrap;
import io.nuls.sdk.exception.NulsException;
import io.nuls.sdk.model.Na;
import io.nuls.sdk.model.Result;
import io.nuls.sdk.utils.JSONUtils;
import io.nuls.sdk.utils.Log;
import io.nuls.sdk.utils.RestFulUtils;
import io.nuls.sdk.utils.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
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
        if (count <= 0 || count > AccountTool.CREATE_MAX_SIZE) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR, "between 0 and 100 can be created at once");
        }
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG, "Length between 8 and 20, the combination of characters and numbers");
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
        return createLocalAccount(1, null);
    }

    @Override
    public Result createLocalAccount(String password) {
        return createLocalAccount(1, password);
    }

    @Override
    public Result createLocalAccount(int count) {
        return createLocalAccount(count, null);
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
      /*  try {
            System.out.println(JSONUtils.obj2json(Result.getSuccess().setData(accounts)));
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return Result.getSuccess().setData(accounts);
    }

    @Override
    public Result backupAccount(String address, String path, String password) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if(StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)){
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("password", password);
        Result result = restFul.post("/account/export/" + address, parameters);
        if(result.isFailed()){
            return result;
        }
        AccountKeyStoreDto accountKeyStoreDto = new AccountKeyStoreDto((Map<String, Object>) result.getData());
        return backUpFile(path, accountKeyStoreDto);
    }

    /**
     * 导出文件
     * Export file
     *
     * @param path
     * @param accountKeyStoreDto
     * @return
     */
    private Result backUpFile(String path, AccountKeyStoreDto accountKeyStoreDto) {
        File backupFile = new File(path);
        //if not directory , create directory
        if (!backupFile.isDirectory()) {
            if (!backupFile.mkdirs()) {
                return Result.getFailed("create directory failed");
            }
            if (!backupFile.exists() && !backupFile.mkdir()) {
                return Result.getFailed("create directory failed");
            }
        }
        String fileName = accountKeyStoreDto.getAddress().concat(AccountConstant.ACCOUNTKEYSTORE_FILE_SUFFIX);
        backupFile = new File(backupFile, fileName);
        try {
            if (!backupFile.exists() && !backupFile.createNewFile()) {
                return Result.getFailed("create file failed");
            }
        } catch (IOException e) {
            return Result.getFailed("create file failed");
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(backupFile);
            fileOutputStream.write(JSONUtils.obj2json(accountKeyStoreDto).getBytes());
        } catch (Exception e) {
            return Result.getFailed("export failed");
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
        }
        return Result.getSuccess().setData("The path to the backup file is " +  path + File.separator + fileName);
    }


    //未测试
    @Override
    public Result getAliasFee(String address, String alias) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if(!StringUtils.validAlias(alias)){
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("address", address);
        parameters.put("alias", alias);
        Result<Na> result = restFul.get("/account/alias/fee", parameters);
        try {
            System.out.println(JSONUtils.obj2json(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /** ---------------------------------------------------------------------*/
    public static void main(String[] args) {
        SDKBootstrap.sdkStart();
        AccountService as = new AccountServiceImpl();
        //as.createLocalAccount(3, "nuls123456");
        //as.createAccount("nuls123456");
        //as.backupAccount("2ChqBTvFXttQsghj8zQpcdv76TQU8G5", "/Users/lichao/Downloads", "nuls123456");
//        as.getAliasFee("2CX4AaWCeqrz3qdJ6dmPYNcbPSUEy4F", "charlie");
        as.getAccount("2CX4AaWCeqrz3qdJ6dmPYNcbPSUEy4F");
    }
    /** ---------------------------------------------------------------------*/
    @Override
    public Result getAccount(String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Result<AccountDto> result = restFul.get("/account/" + address, null);
        System.out.println(result);
        try {
            System.out.println(JSONUtils.obj2json(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
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

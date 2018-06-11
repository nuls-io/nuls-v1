package io.nuls.account.sdk.service.impl;

import io.nuls.account.sdk.constant.AccountConstant;
import io.nuls.account.sdk.constant.AccountErrorCode;
import io.nuls.account.sdk.model.Account;
import io.nuls.account.sdk.model.Address;
import io.nuls.account.sdk.model.dto.AccountDto;
import io.nuls.account.sdk.model.dto.AccountKeyStoreDto;
import io.nuls.account.sdk.model.dto.AssetDto;
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
import java.util.*;

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
        Result result = restFul.get("/account/alias/fee", parameters);
        if(result.isFailed()){
            return result;
        }
        Double nuls = Na.naToNuls(((Map)result.getData()).get("value"));
        /*try {
            System.out.println(JSONUtils.obj2json(result.setData(Na.naToNuls(result.getData()))));
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return result.setData(Na.naToNuls(result.getData()));
    }


    @Override
    public Result getAccount(String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Result result = restFul.get("/account/" + address, null);
        if(result.isFailed()){
            return result;
        }
        AccountDto accountDto = new AccountDto((Map<String, Object>) result.getData());
       /* try {
            System.out.println(JSONUtils.obj2json(result));
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return result.setData(accountDto);
    }

    @Override
    public Result getAccountList(int pageNumber, int pageSize) {
        if(pageNumber < 1 || pageSize < 1 || pageSize > 100){
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("pageNumber", pageNumber);
        parameters.put("pageSize", pageSize);
        Result result = restFul.get("/account", parameters);
        if(result.isFailed()){
            return result;
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>)((Map)result.getData()).get("list");
        List<AccountDto> accountList = new ArrayList<>();
        for(Map<String, Object> map : list){
            AccountDto accountDto = new AccountDto(map);
            accountList.add(accountDto);
        }
        /*try {
            System.out.println(JSONUtils.obj2json(result.setData(accountList)));
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return result.setData(accountList);
    }

    @Override
    public Result getAssets(String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Result result = restFul.get("/account/assets/" + address, null);
        if(result.isFailed()){
            return result;
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>)result.getData();
        List<AssetDto> assetDtoList = new ArrayList<>();
        for(Map<String, Object> map : list){
            map.put("balance",  Na.naToNuls(map.get("balance")));
            map.put("usable", Na.naToNuls(map.get("usable")));
            map.put("locked", Na.naToNuls(map.get("locked")));
            AssetDto assetDto = new AssetDto("NULS", map);
            assetDtoList.add(assetDto);
        }
       /* try {
            System.out.println(JSONUtils.obj2json(result.setData(assetDtoList)));
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return result.setData(assetDtoList);
    }


    /** ---------------------------------------------------------------------*/
    public static void main(String[] args) {
        SDKBootstrap.sdkStart();
        AccountService as = new AccountServiceImpl();
        //as.createLocalAccount(3, "nuls123456");
        //as.createAccount("nuls123456");
        //as.backupAccount("2ChqBTvFXttQsghj8zQpcdv76TQU8G5", "/Users/lichao/Downloads", "nuls123456");
        //as.getAliasFee("2ChDcC1nvki521xXhYAUzYXt4RLNuLs", "charlie");
        //as.getAccount("2ChDcC1nvki521xXhYAUzYXt4RLNuLs");
        //as.getAccountList(1, 100);
        as.getAssets("2ChDcC1nvki521xXhYAUzYXt4RLNuLs");
    }
    /** ---------------------------------------------------------------------*/
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

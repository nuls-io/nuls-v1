package io.nuls.account.sdk.service.impl;

import io.nuls.account.sdk.constant.AccountConstant;
import io.nuls.account.sdk.model.dto.AccountDto;
import io.nuls.account.sdk.model.dto.AccountKeyStoreDto;
import io.nuls.account.sdk.model.dto.AssetDto;
import io.nuls.account.sdk.service.AccountService;
import io.nuls.sdk.SDKBootstrap;
import io.nuls.sdk.constant.AccountErrorCode;
import io.nuls.sdk.crypto.ECKey;
import io.nuls.sdk.exception.NulsException;
import io.nuls.sdk.model.Account;
import io.nuls.sdk.model.Address;
import io.nuls.sdk.model.Na;
import io.nuls.sdk.model.Result;
import io.nuls.sdk.model.dto.BalanceDto;
import io.nuls.sdk.utils.*;

import java.io.*;
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
        return Result.getSuccess().setData(accounts);
    }

    @Override
    public Result backupAccount(String address, String path, String password) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("password", password);
        Result result = restFul.post("/account/export/" + address, parameters);
        if (result.isFailed()) {
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
        return Result.getSuccess().setData("The path to the backup file is " + path + File.separator + fileName);
    }

    @Override
    public Result getAliasFee(String address, String alias) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (!StringUtils.validAlias(alias)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("address", address);
        parameters.put("alias", alias);
        Result result = restFul.get("/account/alias/fee", parameters);
        if (result.isFailed()) {
            return result;
        }
        Double nuls = Na.naToNuls(((Map) result.getData()).get("value"));
        return result.setData(Na.naToNuls(nuls));
    }


    @Override
    public Result getAccount(String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Result result = restFul.get("/account/" + address, null);
        if (result.isFailed()) {
            return result;
        }
        AccountDto accountDto = new AccountDto((Map<String, Object>) result.getData());
        return result.setData(accountDto);
    }

    @Override
    public Result getAccountList(int pageNumber, int pageSize) {
        if (pageNumber < 1 || pageSize < 1 || pageSize > 100) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("pageNumber", pageNumber);
        parameters.put("pageSize", pageSize);
        Result result = restFul.get("/account", parameters);
        if (result.isFailed()) {
            return result;
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>) ((Map) result.getData()).get("list");
        List<AccountDto> accountList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            AccountDto accountDto = new AccountDto(map);
            accountList.add(accountDto);
        }
        return result.setData(accountList);
    }

    @Override
    public Result getAssets(String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Result result = restFul.get("/account/assets/" + address, null);
        if (result.isFailed()) {
            return result;
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.getData();
        List<AssetDto> assetDtoList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            map.put("balance", Na.naToNuls(map.get("balance")));
            map.put("usable", Na.naToNuls(map.get("usable")));
            map.put("locked", Na.naToNuls(map.get("locked")));
            AssetDto assetDto = new AssetDto("NULS", map);
            assetDtoList.add(assetDto);
        }
        return result.setData(assetDtoList);
    }


    @Override
    public Result getAddressByAlias(String alias) {
        if (!StringUtils.validAlias(alias)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("alias", alias);
        Result result = restFul.get("/account/alias/address", parameters);
        return result;
    }

    @Override
    public Result getPrikey(String address, String password) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("password", password);
        Result result = restFul.post("/account/prikey/" + address, parameters);
        return result;
    }

    @Override
    public Result getWalletTotalBalance() {
        Result result = restFul.get("/account/balance", null);
        if (result.isFailed()) {
            return result;
        }
        Map<String, Object> map = (Map) result.getData();
        map.put("balance", ((Map) map.get("balance")).get("value"));
        map.put("usable", ((Map) map.get("usable")).get("value"));
        map.put("locked", ((Map) map.get("locked")).get("value"));
        BalanceDto balanceDto = new BalanceDto(map);
        return result.setData(balanceDto);
    }

    @Override
    public Result isAliasExist(String alias) {
        if (!StringUtils.validAlias(alias)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("alias", alias);
        Result result = restFul.get("/account/alias", parameters);
        return result;
    }

    @Override
    public Result importAccountByKeystore(String path, String password, boolean overwrite) {
        File file = new File(path);
        if (null != file && file.isFile()) {
            try {
                return importAccountByKeystore(new FileReader(file), password, overwrite);
            } catch (FileNotFoundException e) {
                return Result.getFailed(AccountErrorCode.ACCOUNTKEYSTORE_FILE_NOT_EXIST);
            }
        }
        return Result.getFailed(AccountErrorCode.ACCOUNTKEYSTORE_FILE_NOT_EXIST);
    }

    @Override
    public Result importAccountByKeystore(FileReader fileReader, String password, boolean overwrite) {
        if (null == fileReader) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        Result rs = getAccountKeystoreDto(fileReader);
        if (rs.isFailed()) {
            return rs;
        }
        AccountKeyStoreDto accountKeyStoreDto = (AccountKeyStoreDto) rs.getData();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("accountKeyStoreDto", accountKeyStoreDto);
        parameters.put("password", password);
        parameters.put("overwrite", overwrite);
        Result result = restFul.post("/account/import", parameters);
        return result;
    }

    /**
     * 根据文件地址获取AccountKeystoreDto对象
     * Gets the AccountKeystoreDto object based on the file address
     *
     * @param fileReader
     * @return
     */
    private Result getAccountKeystoreDto(FileReader fileReader) {
        StringBuilder ks = new StringBuilder();
        BufferedReader bufferedReader = null;
        String str;
        try {
            bufferedReader = new BufferedReader(fileReader);
            while ((str = bufferedReader.readLine()) != null) {
                if (!str.isEmpty()) {
                    ks.append(str);
                }
            }
            AccountKeyStoreDto accountKeyStoreDto = JSONUtils.json2pojo(ks.toString(), AccountKeyStoreDto.class);
            return Result.getSuccess().setData(accountKeyStoreDto);
        } catch (FileNotFoundException e) {
            return Result.getFailed(AccountErrorCode.ACCOUNTKEYSTORE_FILE_NOT_EXIST);
        } catch (IOException e) {
            return Result.getFailed(AccountErrorCode.ACCOUNTKEYSTORE_FILE_DAMAGED);
        } catch (Exception e) {
            return Result.getFailed(AccountErrorCode.ACCOUNTKEYSTORE_FILE_DAMAGED);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
        }
    }

    @Override
    public Result importAccountByPriKey(String privateKey, String password, boolean overwrite) {
        if (!ECKey.isValidPrivteHex(privateKey)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("priKey", privateKey);
        parameters.put("password", password);
        parameters.put("overwrite", overwrite);
        Result result = restFul.post("/account/import/pri", parameters);
        return result;
    }

    @Override
    public Result isEncrypted(String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Result result = restFul.get("/account/encrypted/" + address, null);
        return result;
    }

    @Override
    public Result lockAccount(String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Result result = restFul.post("/account/lock/" + address, "");
        return result;
    }

    @Override
    public Result unlockAccount(String address, String password, int unlockTime) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        if (unlockTime < 1 || unlockTime > 120) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("password", password);
        parameters.put("unlockTime", unlockTime);
        Result result = restFul.post("/account/unlock/" + address, parameters);
        return result;
    }

    @Override
    public Result removeAccount(String address, String password) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("password", password);
        Result result = restFul.post("/account/remove/" + address, parameters);
        return result;
    }

    @Override
    public Result setPassword(String address, String password) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (!StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_FORMAT_WRONG);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("password", password);
        Result result = restFul.post("/account/password/" + address, parameters);
        return result;
    }

    @Override
    public Result resetPassword(String address, String password, String newPassword) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (!StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        if (!StringUtils.validPassword(newPassword)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("password", password);
        parameters.put("newPassword", newPassword);
        Result result = restFul.put("/account/password/" + address, parameters);
        return result;
    }

    @Override
    public Result updatePasswordByKeystore(FileReader fileReader, String password) {
        if (null == fileReader || !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Result rs = getAccountKeystoreDto(fileReader);
        if (rs.isFailed()) {
            return rs;
        }
        AccountKeyStoreDto accountKeyStoreDto = (AccountKeyStoreDto) rs.getData();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("accountKeyStoreDto", accountKeyStoreDto);
        parameters.put("password", password);
        Result result = restFul.post("/account/password/keystore", parameters);
        return result;
    }

    @Override
    public Result setAlias(String address, String alias, String password) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (!StringUtils.validAlias(alias)) {
            return Result.getFailed(AccountErrorCode.ALIAS_FORMAT_WRONG);
        }
        if (!StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("alias", alias);
        parameters.put("password", password);
        Result result = restFul.post("/account/alias/" + address, parameters);
        return result;
    }

    /**
     * -----------------------------------Test------------------------------
     */
    public static void main(String[] args) {
        SDKBootstrap.sdkStart();
        AccountService as = new AccountServiceImpl();
//        as.createLocalAccount(3, "nuls123456");
//        as.createAccount("nuls123456");
//        as.backupAccount("2ChDcC1nvki521xXhYAUzYXt4RLNuLs", "/Users/lichao/Downloads", "nuls123456");
//        as.getAliasFee("2ChDcC1nvki521xXhYAUzYXt4RLNuLs", "charlie");
//        as.getAccount("2ChDcC1nvki521xXhYAUzYXt4RLNuLs");
//        as.getAccountList(1, 100);
//        as.getAssets("2ChDcC1nvki521xXhYAUzYXt4RLNuLs");
//        as.setAlias("2ChDcC1nvki521xXhYAUzYXt4RLNuLs", "charlie", "nuls123456");
//        as.getAddressByAlias("charlie");
//        as.getPrikey("2ChDcC1nvki521xXhYAUzYXt4RLNuLs", "nuls123456");
       /* try {
            System.out.println(JSONUtils.obj2json(as.getWalletTotalBalance()));
        } catch (Exception e) {
            e.printStackTrace();
        }*/
//        as.importAccountByKeystore("/Users/lichao/Downloads/2ChDcC1nvki521xXhYAUzYXt4RLNuLs.accountkeystore","nuls123456",true);

       /* FileReader fileReader = null;
        try {
            fileReader = new FileReader(new File("/Users/lichao/Downloads/2ChDcC1nvki521xXhYAUzYXt4RLNuLs.accountkeystore"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        as.importAccountByKeystore(fileReader,"nuls123456",true);*/

//        as.importAccountByPriKey("1f9d3ad044e0e1201e117b041f3d2ceedacb44688e57969620f3ad7a4d6e9d24", "nuls123456", true);
//        as.isEncrypted("2ChDcC1nvki521xXhYAUzYXt4RLNuLs");
//        as.lockAccount("2ChDcC1nvki521xXhYAUzYXt4RLNuLs");
//        as.unlockAccount("2ChDcC1nvki521xXhYAUzYXt4RLNuLs", "nuls123456", 120);
//        as.setPassword("2CWSpfF1mFTjWmDCAx4A6NXwykgpj4q", "nuls123456");
//        as.removeAccount("2CWSpfF1mFTjWmDCAx4A6NXwykgpj4q", "nuls123456");
        as.resetPassword("2ChDcC1nvki521xXhYAUzYXt4RLNuLs", "nuls123456", "nuls123456");

       /* FileReader fileReader = null;
        try {
            fileReader = new FileReader(new File("/Users/lichao/Downloads/2ChDcC1nvki521xXhYAUzYXt4RLNuLs.accountkeystore"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        as.updatePasswordByKeystore(fileReader,"nuls1234567");*/
    }
    /**
     * ---------------------------------------------------------------------
     */
}

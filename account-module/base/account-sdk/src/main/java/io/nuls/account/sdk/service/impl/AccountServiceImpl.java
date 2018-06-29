package io.nuls.account.sdk.service.impl;

import io.nuls.account.sdk.constant.AccountConstant;
import io.nuls.account.sdk.model.AccountDto;
import io.nuls.account.sdk.model.AccountKeyStoreDto;
import io.nuls.account.sdk.service.AccountService;
import io.nuls.sdk.constant.AccountErrorCode;
import io.nuls.sdk.crypto.AESEncrypt;
import io.nuls.sdk.crypto.ECKey;
import io.nuls.sdk.crypto.Hex;
import io.nuls.sdk.exception.NulsException;
import io.nuls.sdk.model.Account;
import io.nuls.sdk.model.Na;
import io.nuls.sdk.model.Result;
import io.nuls.sdk.model.dto.BalanceDto;
import io.nuls.sdk.utils.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
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
    public Result createOfflineAccount() {
        return createOfflineAccount(1, null);
    }

    @Override
    public Result createOfflineAccount(String password) {
        return createOfflineAccount(1, password);
    }

    @Override
    public Result createOfflineAccount(int count) {
        return createOfflineAccount(count, null);
    }

    @Override
    public Result createOfflineAccount(int count, String password) {
        if (count <= 0 || count > AccountTool.CREATE_MAX_SIZE) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR, "between 0 and 100 can be created at once");
        }
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG, "Length between 8 and 20, the combination of characters and numbers");
        }
        List<AccountDto> accounts = new ArrayList<>();
        try {
            for (int i = 0; i < count; i++) {
                Account account = AccountTool.createAccount();
                if (StringUtils.isNotBlank(password)) {
                    account.encrypt(password);
                }
                accounts.add(new AccountDto(account));
            }
        } catch (NulsException e) {
            return Result.getFailed();
        }
        Map<String, List<AccountDto>> map = new HashMap<>();
        map.put("list", accounts);
        return Result.getSuccess().setData(map);
    }

    @Override
    public Result backupAccount(String address, String path, String password) {
        if (!AddressTool.validAddress(address)) {
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
        if (StringUtils.isBlank(path)) {
            path = System.getProperty("user.dir");
        }
        return backUpFile(path, accountKeyStoreDto);
    }

    @Override
    public Result backupAccount(String address, String path) {
        return backupAccount(address, path, null);
    }

    /**
     * 导出文件
     * Export file
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
        Map<String, String> map = new HashMap<>();
        map.put("value", path + File.separator + fileName);
        return Result.getSuccess().setData(map);
    }

    @Override
    public Result getAliasFee(String address, String alias) {
        if (!AddressTool.validAddress(address)) {
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
        Map<String, Object> map = (Map) result.getData();
        Double nuls = Na.naToNuls(map.get("value"));
        map.put("value", nuls);
        return result.setData(map);
    }


    @Override
    public Result getAccount(String address) {
        if (!AddressTool.validAddress(address)) {
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
        return result.setData(result.getData());
    }

    @Override
    public Result getAssets(String address) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Result result = restFul.get("/account/assets/" + address, null);
        if (result.isFailed()) {
            return result;
        }
        return result;
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
        if (!AddressTool.validAddress(address)) {
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
    public Result getPrikey(String address) {
        return getPrikey(address, null);
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
    public Result isAliasUsable(String alias) {
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
    public Result importAccountByKeystore(String path, boolean overwrite) {
        return importAccountByKeystore(path, null, overwrite);
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

    @Override
    public Result importAccountByKeystore(FileReader fileReader, boolean overwrite) {
        return importAccountByKeystore(fileReader, null, overwrite);
    }

    /**
     * 根据文件地址获取AccountKeystoreDto对象
     * Gets the AccountKeystoreDto object based on the file address
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
    public Result importAccountByPriKey(String privateKey, boolean overwrite) {
        return importAccountByPriKey(privateKey, null, overwrite);
    }

    @Override
    public Result isEncrypted(String address) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Result result = restFul.get("/account/encrypted/" + address, null);
        return result;
    }

    @Override
    public Result lockAccount(String address) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Result result = restFul.post("/account/lock/" + address, "");
        return result;
    }

    @Override
    public Result unlockAccount(String address, String password, int unlockTime) {
        if (!AddressTool.validAddress(address)) {
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
        if (!AddressTool.validAddress(address)) {
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
    public Result removeAccount(String address) {
        return removeAccount(address, null);
    }

    @Override
    public Result setPassword(String address, String password) {
        if (!AddressTool.validAddress(address)) {
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
        if (!AddressTool.validAddress(address)) {
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
    public Result setPasswordOffline(String address, String priKey, String password) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isBlank(priKey)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        if (!StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        return getEncryptedPrivateKey(address, priKey, password);
    }

    @Override
    public Result resetPasswordOffline(String address, String encryptedPriKey, String password, String newPassword) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isBlank(encryptedPriKey)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        if (!StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        if (!StringUtils.validPassword(newPassword)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        try {
            byte[] priKey = AESEncrypt.decrypt(Hex.decode(encryptedPriKey), password);
            return getEncryptedPrivateKey(address, Hex.encode(priKey), newPassword);
        } catch (Exception e) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
    }

    public Result getEncryptedPrivateKey(String address, String prikey, String password) {
        if (!ECKey.isValidPrivteHex(prikey)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Account account;
        try {
            account = AccountTool.createAccount(prikey);
            if (!address.equals(AddressTool.getStringAddressByBytes(account.getAddress().getAddressBytes()))) {
                return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
            }
            account.encrypt(password);
        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.FAILED);
        }
        Map<String, String> map = new HashMap<>();
        map.put("value", Hex.encode(account.getEncryptedPriKey()));
        return Result.getSuccess().setData(map);
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
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (!StringUtils.validAlias(alias)) {
            return Result.getFailed(AccountErrorCode.ALIAS_FORMAT_WRONG);
        }
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("alias", alias);
        parameters.put("password", password);
        Result result = restFul.post("/account/alias/" + address, parameters);
        return result;
    }

    @Override
    public Result setAlias(String address, String alias) {
        return setAlias(address, alias, null);
    }

}

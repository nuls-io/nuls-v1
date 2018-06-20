package io.nuls.account.sdk.service.impl;

import io.nuls.account.sdk.constant.AccountConstant;
import io.nuls.account.sdk.model.AccountDto;
import io.nuls.account.sdk.model.AccountKeyStoreDto;
import io.nuls.account.sdk.model.AssetDto;
import io.nuls.account.sdk.service.AccountService;
import io.nuls.sdk.SDKBootstrap;
import io.nuls.sdk.constant.AccountErrorCode;
import io.nuls.sdk.crypto.AESEncrypt;
import io.nuls.sdk.crypto.ECKey;
import io.nuls.sdk.crypto.Hex;
import io.nuls.sdk.exception.NulsException;
import io.nuls.sdk.model.Account;
import io.nuls.sdk.model.Address;
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
        return Result.getSuccess().setData(path + File.separator + fileName);
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
            map.put("balance", map.get("balance"));
            map.put("usable", map.get("usable"));
            map.put("locked", map.get("locked"));
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
    public Result importAccountByPriKey(String privateKey, boolean overwrite) {
        return importAccountByPriKey(privateKey, null, overwrite);
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
    public Result removeAccount(String address) {
        return removeAccount(address, null);
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
    public Result setPasswordOffline(String address, String priKey, String password) {
        if (!Address.validAddress(address)) {
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
        if (!Address.validAddress(address)) {
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
            if (!address.equals(account.getAddress().getBase58())) {
                return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
            }
            account.encrypt(password);
        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.FAILED);
        }
        return Result.getSuccess().setData(Hex.encode(account.getEncryptedPriKey()));
    }

    /**
     * -----------------------------------Test------------------------------
     */
    public static void main(String[] args) {
        SDKBootstrap.sdkStart();
        AccountService as = new AccountServiceImpl();
//        as.createAccount("nuls123456");
//        as.backupAccount("2ChDcC1nvki521xXhYAUzYXt4RLNuLs", "/Users/lichao/Downloads", "nuls123456");
//        as.getAliasFee("2ChDcC1nvki521xXhYAUzYXt4RLNuLs", "charlie");
//        as.getAccount("2ChDcC1nvki521xXhYAUzYXt4RLNuLs");
//        as.getAccountList(1, 100);
//        as.getAssets("2ChDcC1nvki521xXhYAUzYXt4RLNuLs");
//        as.setAlias("2ChDcC1nvki521xXhYAUzYXt4RLNuLs", "charlie", "nuls123456");
//        as.getAddressByAlias("charlie");
//        as.getPrikey("2ChDcC1nvki521xXhYAUzYXt4RLNuLs", "nuls123456");
        try {
//            System.out.println(JSONUtils.obj2json(as.getWalletTotalBalance()));
//            System.out.println(JSONUtils.obj2json(as.createOffLineAccount(2, "nuls123456")));
//            System.out.println(JSONUtils.obj2json(as.createOffLineAccount(1)));
//            System.out.println(JSONUtils.obj2json(as.createOffLineAccount("nuls123456")));
//            System.out.println(JSONUtils.obj2json(as.createAccount("nuls123456")));
//            System.out.println(JSONUtils.obj2json(as.getAccount("2ChDcC1nvki521xXhYAUzYXt4RLNuLs")));
//            System.out.println(JSONUtils.obj2json(as.getAliasFee("2ChDcC1nvki521xXhYAUzYXt4RLNuLs", "charlie")));
//            System.out.println(JSONUtils.obj2json(as.getAssets("2ChDcC1nvki521xXhYAUzYXt4RLNuLs")));
//            System.out.println(JSONUtils.obj2json(as.getWalletTotalBalance()));
//            System.out.println(JSONUtils.obj2json(as.setAlias("2CiU1CmB6c9jmSLDNBe6PouA7NgNULS","firstblood", "nuls123456")));
//            System.out.println(JSONUtils.obj2json(as.getAddressByAlias("charlie")));
//            System.out.println(JSONUtils.obj2json(as.getAccountList(1,10)));
//            System.out.println(JSONUtils.obj2json(as.getPrikey("2ChDcC1nvki521xXhYAUzYXt4RLNuLs","nuls123456")));
//            System.out.println(JSONUtils.obj2json(as.isAliasExist("charlie")));
//            System.out.println(JSONUtils.obj2json(as.backupAccount("2ChDcC1nvki521xXhYAUzYXt4RLNuLs", "/Users/lichao/Downloads", "nuls123456")));
//            System.out.println(JSONUtils.obj2json(as.importAccountByKeystore("/Users/lichao/Downloads/2ChDcC1nvki521xXhYAUzYXt4RLNuLs.accountkeystore","nuls123456",true)));
//            System.out.println(JSONUtils.obj2json(as.importAccountByPriKey("74ffeeec0b2d6552a07eb7f5691a6da13278f87025283ca474741f35b247f55d",
//                    "nuls123456",true)));
//            System.out.println(JSONUtils.obj2json(as.isEncrypted("2ChDcC1nvki521xXhYAUzYXt4RLNuLs")));
//            System.out.println(JSONUtils.obj2json(as.removeAccount("2Ccv2oMPZ8X7r2pHA4SoFPwpUNiJ11J")));
//            System.out.println(JSONUtils.obj2json(as.setPassword("2CYJcEQvYW7UuX3GMeFxm2Mzn6pQ6jJ","nuls123456")));
//            System.out.println(JSONUtils.obj2json(as.resetPassword("2CWZUrEkkFebiz3T8cx6bDTLSVX43mv","nuls123456", "nuls111111")));
            /*System.out.println(JSONUtils.obj2json(as.setPasswordOffLine(
                    "2Cby7jxykhikf1UyKuWVdpVy6eAiLBm",
                    "00e3bd8fc2cabaefae82c26f97355a4fdfdb38582d99af2f7438b827153e8f1b22",
                    "nuls123456")));*/
            System.out.println(JSONUtils.obj2json(as.resetPasswordOffline(
                    "2Cby7jxykhikf1UyKuWVdpVy6eAiLBm",
                    "a770c1886f566c973b6eb99543ef03825a89ed16e20d8dbe320aed64a85d5863ca23df43ef16ce0475424a49e192b6f9",
                    "nuls123456", "nuls111111")));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
//        as.resetPassword("2ChDcC1nvki521xXhYAUzYXt4RLNuLs", "nuls123456", "nuls123456");

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

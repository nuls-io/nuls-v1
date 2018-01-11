package io.nuls.account.service.intf;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.chain.entity.Result;

import java.util.List;

/**
 * @author Niels
 */
public interface AccountService {

    void init();

    void start();

    void shutdown();

    void destroy();

    Account createAccount();

    Result<List<String>> createAccount(int count);

    Account getDefaultAccount();

    Account getAccount(String address);

    List<Account> getAccountList();

    Address getAddress(String pubKey);

    byte[] getPrivateKey(String address);

    void setDefaultAccount(String id);

    Result encryptAccount(String password);

    Result changePassword(String oldPassword, String newPassword);

    boolean isEncrypted();

    Result unlockAccounts(String password, int seconds);

    NulsSignData signData(byte[] bytes);

    NulsSignData signData(NulsDigestData digestData);

    NulsSignData signData(byte[] bytes, String password);

    NulsSignData signData(NulsDigestData digestData, String password);

    NulsSignData signData(byte[] bytes, Account account, String password);

    NulsSignData signData(NulsDigestData digestData, Account account, String password);

    Result setAlias(String address, String password, String alias);

    Result verifySign(byte[] bytes, NulsSignData data);

    Result exportAccount(String filePath);

    Result exportAccount(String address, String filePath);

    Result exportAccounts(String filePath);

    Result importAccountsFile(String walletFilePath);
}

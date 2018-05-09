package io.nuls.account.service;

import io.nuls.account.model.Account;
import io.nuls.account.model.AccountKeyStore;
import io.nuls.account.model.Address;
import io.nuls.account.model.Balance;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.NulsSignData;
import io.nuls.kernel.model.Result;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/5/9
 */
@Service
public class AccountServiceImpl implements AccountService {


    @Override
    public Result<List<Account>> createAccount(int count, String password) {
        return null;
    }

    @Override
    public Result<List<Account>> createAccount(String password) {
        return createAccount(1, password);
    }

    @Override
    public Result<Boolean> removeAccount(String accountId, String password) {
        return null;
    }

    @Override
    public Result<Account> importAccountFormKeyStore(AccountKeyStore keyStore, String password) {
        return null;
    }

    @Override
    public Result<AccountKeyStore> exportAccountToKeyStore(String accountAddress, String password) {
        return null;
    }

    @Override
    public Result<Account> getAccount(String address) {
        return null;
    }

    @Override
    public Result<Account> getAccount(Address address) {
        return null;
    }

    @Override
    public Result<Address> getAddress(String pubKey) {
        return null;
    }

    @Override
    public Result<Address> getAddress(byte[] pubKey) {
        return null;
    }

    @Override
    public Result<Boolean> isEncrypted(Account account) {
        return null;
    }

    @Override
    public Result<Boolean> isEncrypted(Address address) {
        return null;
    }

    @Override
    public Result<Boolean> isEncrypted(String address) {
        return null;
    }

    @Override
    public Result verifyAddressFormat(String address) {
        return null;
    }

    @Override
    public Result<List<Account>> getAccountList() {
        return null;
    }

    @Override
    public Result<Account> getDefaultAccount() {
        return null;
    }

    @Override
    public NulsSignData signData(byte[] data, Account account, String password) throws NulsException {
        return null;
    }

    @Override
    public NulsSignData signData(byte[] data, Account account) throws NulsException {
        return null;
    }

    @Override
    public NulsSignData signData(byte[] data, ECKey ecKey) throws NulsException {
        return null;
    }

    @Override
    public Result verifySignData(byte[] data, NulsSignData signData, byte[] pubKey) {
        return null;
    }

    @Override
    public Result<Balance> getBalance() {
        return null;
    }

    @Override
    public Result<Balance> getBalance(Account account) {
        return null;
    }

    @Override
    public Result<Balance> getBalance(Address address) {
        return null;
    }

    @Override
    public Result<Balance> getBalance(String address) {
        return null;
    }
}

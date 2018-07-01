/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.account.sdk.service;

import io.nuls.sdk.model.Result;

import java.io.FileReader;

/**
 * @author: Charlie
 */
public interface AccountService {

    /**
     * Create an unencrypted account
     *
     * @return Result
     * If the operation is successful, 'success' is true, and data is List<String> (address);
     * If the operation fails, "success" is false and the result has error information
     */
    Result createAccount();

    /**
     * Create an encrypted account
     *
     * @param password The password of the account
     * @return Result
     * If the operation is successful, 'success' is true and data is List<String> (address);
     * If the operation fails, "success" is false and the result has error information
     */
    Result createAccount(String password);

    /**
     * Create unencrypted accounts
     *
     * @param count The number of accounts you want to create
     * @return Result
     * If the operation is successful, 'success' is true and data is List<String> (address);
     * If the operation fails, "success" is false and the result has error information
     */
    Result createAccount(int count);

    /**
     * Create encrypted accounts
     *
     * @param count    The number of accounts you want to create
     * @param password The password of the account
     * @return Result
     * If the operation is successful, 'success' is true and data is List<String> (address);
     * If the operation fails, "success" is false and the result has error information
     */
    Result createAccount(int count, String password);


    /**
     * Create an unencrypted off-line account (Not saved to the database)
     *
     * @return Result
     * If the operation is successful, 'success' is true, and data is List<AccountDto>;
     * If the operation fails, "success" is false and the result has error information
     */
    Result createOfflineAccount();

    /**
     * Create an encrypted off-line account (Not saved to the database)
     *
     * @param password The password of the account
     * @return Result
     * If the operation is successful, 'success' is true and data is List<AccountDto>;
     * If the operation fails, "success" is false and the result has error information
     */
    Result createOfflineAccount(String password);

    /**
     * Create unencrypted off-line accounts (Not saved to the database)
     *
     * @param count The number of accounts you want to create
     * @return Result
     * If the operation is successful, 'success' is true and data is List<AccountDto>;
     * If the operation fails, "success" is false and the result has error information
     */
    Result createOfflineAccount(int count);

    /**
     * Create encrypted off-line accounts (Not saved to the database)
     *
     * @param count The number of accounts you want to create
     * @return Result
     * If the operation is successful, 'success' is true and data is List<AccountDto>;
     * If the operation fails, "success" is false and the result has error information
     */
    Result createOfflineAccount(int count, String password);


    /**
     * Backup an account
     * Generate a keystore backup file
     *
     * @param address  The number of accounts you want to backup
     * @param path     Folder path to save backup files, if you pass null to save to the current directory
     * @param password
     * @return
     */
    Result backupAccount(String address, String path, String password);

    /**
     * Backup an account
     * Generate a keystore backup file
     *
     * @param address The number of accounts you want to backup
     * @param path    Folder path to save backup files, if you pass null to save to the current directory
     * @return
     */
    Result backupAccount(String address, String path);

    /**
     * Get the fee for setting the alias (The fee don't include the fixed 1 NULS to be destroyed)
     *
     * @param address The address of account to set an alias for
     * @param alias   The alias value to be set
     * @return If the operation is successful, 'success' is true and data is fee;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getAliasFee(String address, String alias);

    /**
     * Get the account information
     *
     * @param address The address of account
     * @return If the operation is successful, 'success' is true and data is AccountDto;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getAccount(String address);

    /**
     * Get the account information list
     *
     * @param pageNumber pageNumber
     * @param pageSize   1~100
     * @return If the operation is successful, 'success' is true and data is List<AccountDto>;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getAccountList(int pageNumber, int pageSize);

    /**
     * Get account assets
     *
     * @param address The address of account
     * @return If the operation is successful, 'success' is true and data is List<AssetDto>;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getAssets(String address);

    /**
     * Get the account address by alias
     *
     * @param alias The alias of account
     * @return If the operation is successful, 'success' is true and data is address string;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getAddressByAlias(String alias);

    /**
     * Get the private key
     *
     * @param address  The address of account
     * @param password The password of account
     * @return If the operation is successful, 'success' is true and data is private key;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getPrikey(String address, String password);

    /**
     * Get the private key
     *
     * @param address The address of account
     * @return If the operation is successful, 'success' is true and data is private key;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getPrikey(String address);

    /**
     * Get the total balance of all accounts in the wallet
     *
     * @return If the operation is successful, 'success' is true and data is BalanceDto;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getWalletTotalBalance();

    /**
     * Verify that the alias is usable
     *
     * @param alias
     * @return If the alias is usable, 'success' is true;
     * If the alias is  unusable, "success" is false;
     */
    Result isAliasUsable(String alias);

    /**
     * Import account according to KeyStore file path
     *
     * @param path      Exported keystore file address during backup
     * @param password  The password of account
     * @param overwrite true: Always perform an override import; false: if the account exists, it will not be executed and return fails
     * @return If the operation is successful, 'success' is true;
     * If the operation fails, "success" is false and the result has error information
     */
    Result importAccountByKeystore(String path, String password, boolean overwrite);

    /**
     * Import account according to KeyStore file path
     *
     * @param path      Exported keystore file address during backup
     * @param overwrite true: Always perform an override import; false: if the account exists, it will not be executed and return fails
     * @return If the operation is successful, 'success' is true;
     * If the operation fails, "success" is false and the result has error information
     */
    Result importAccountByKeystore(String path, boolean overwrite);

    /**
     * Import account according to KeyStore file fileReader
     *
     * @param fileReader The fileReader of KeyStore file
     * @param password   The password of account
     * @param overwrite  true: Always perform an override import; false: if the account exists, it will not be executed and return fails
     * @return If the operation is successful, 'success' is true;
     * If the operation fails, "success" is false and the result has error information
     */
    Result importAccountByKeystore(FileReader fileReader, String password, boolean overwrite);

    /**
     * Import account according to KeyStore file fileReader
     *
     * @param fileReader The fileReader of KeyStore file
     * @param overwrite  true: Always perform an override import; false: if the account exists, it will not be executed and return fails
     * @return If the operation is successful, 'success' is true;
     * If the operation fails, "success" is false and the result has error information
     */
    Result importAccountByKeystore(FileReader fileReader, boolean overwrite);

    /**
     * Import account according to privateKey
     *
     * @param privateKey The privateKey of account
     * @param password   The new password of account
     * @param overwrite  true: Always perform an override import; false: if the account exists, it will not be executed and return fails
     * @return If the operation is successful, 'success' is true;
     * If the operation fails, "success" is false and the result has error information
     */
    Result importAccountByPriKey(String privateKey, String password, boolean overwrite);

    /**
     * Import account according to privateKey
     *
     * @param privateKey The privateKey of account
     * @param overwrite  true: Always perform an override import; false: if the account exists, it will not be executed and return fails
     * @return If the operation is successful, 'success' is true;
     * If the operation fails, "success" is false and the result has error information
     */
    Result importAccountByPriKey(String privateKey, boolean overwrite);

    /**
     * Verify that the account is encrypted
     *
     * @param address The address of the account
     * @return If the account is encrypted, 'success' is true;
     * If the account is unencrypted, "success" is false;
     * If the operation fails, "success" is false and the result has error information
     */
    Result isEncrypted(String address);

    /**
     * Lock the account
     *
     * @param address The address of the account
     * @return If the operation is successful, 'success' is true;
     * If the operation fails, "success" is false and the result has error information
     */
    Result lockAccount(String address);

    /**
     * Unlock the account
     *
     * @param address    The address of account you want to unlock
     * @param password   The password of account
     * @param unlockTime Unlock time (seconds), maximum 120 seconds
     * @return If the operation is successful, 'success' is true;
     * If the operation fails, "success" is false and the result has error information
     */
    Result unlockAccount(String address, String password, int unlockTime);

    /**
     * Remove the account
     *
     * @param address  The address of account you want to remove
     * @param password The password of account
     * @return If the operation is successful, 'success' is true;
     * If the operation fails, "success" is false and the result has error information
     */
    Result removeAccount(String address, String password);

    /**
     * Remove the account
     *
     * @param address The address of account you want to remove
     * @return If the operation is successful, 'success' is true;
     * If the operation fails, "success" is false and the result has error information
     */
    Result removeAccount(String address);

    /**
     * Set a password for your account(encrypt account)
     *
     * @param address  The address of account you want to remove
     * @param password The new password
     * @return If the operation is successful, 'success' is true;
     * If the operation fails, "success" is false and the result has error information
     */
    Result setPassword(String address, String password);


    /**
     * Change the account password by current passowrd
     *
     * @param address     The address of account you want to change
     * @param password    The current password of account
     * @param newPassword The new password
     * @return If the operation is successful, 'success' is true;
     * If the operation fails, "success" is false and the result has error information
     */
    Result resetPassword(String address, String password, String newPassword);

    /**
     * Set a password for your off-line account(encrypt account)
     *
     * @param address  The address of account
     * @param priKey   The private key of account
     * @param password The new password
     * @return
     */
    Result setPasswordOffline(String address, String priKey, String password);

    /**
     * Change the off-line account password by encryptedPriKey and passowrd
     *
     * @param address         The address of account
     * @param encryptedPriKey The encrypted Private Key
     * @param password        The password to use when encrypting the private key
     * @param newPassword     The new password
     * @return
     */
    Result resetPasswordOffline(String address, String encryptedPriKey, String password, String newPassword);

    /**
     * Change the account password by keystore file
     *
     * @param fileReader The fileReader of KeyStore file
     * @param password   The new password
     * @return If the operation is successful, 'success' is true;
     * If the operation fails, "success" is false and the result has error information
     */
    Result updatePasswordByKeystore(FileReader fileReader, String password);

    /**
     * set alias
     *
     * @param address  The address of account to set an alias for
     * @param alias    The alias value to be set
     * @param password The password of account
     * @return If the operation is successful, 'success' is true;
     * If the operation fails, "success" is false and the result has error information
     */
    Result setAlias(String address, String alias, String password);

    /**
     * set alias
     *
     * @param address The address of account to set an alias for
     * @param alias   The alias value to be set
     * @return If the operation is successful, 'success' is true;
     * If the operation fails, "success" is false and the result has error information
     */
    Result setAlias(String address, String alias);
}
package io.nuls.account.ledger.sdk.service;

import io.nuls.sdk.model.Result;

/**
 * @author: Charlie
 * @date: 2018/6/12
 */
public interface AccountLedgerService {

    /**
     * Get transaction details based on transaction hash
     * @param hash Transaction hash
     * @return
     * If the operation is successful, 'success' is true, and data is TransactionDto;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getTxByHash(String hash);

    /**
     * Transfer of account
     * @param address Remittance account address
     * @param toAddress  Beneficiary account Address
     * @param password Remittance account password
     * @param amount Transfer amount
     * @param remark
     * @return
     * If the operation is successful, 'success' is true
     * If the operation fails, "success" is false and the result has error information
     */
    Result transfer(String address, String toAddress, String password, long amount, String remark);

    /**
     * Get account balance
     * @param address
     * @return
     * If the operation is successful, 'success' is true, and data is BalanceDto
     * If the operation fails, "success" is false and the result has error information
     */
    Result getBalance(String address);


}

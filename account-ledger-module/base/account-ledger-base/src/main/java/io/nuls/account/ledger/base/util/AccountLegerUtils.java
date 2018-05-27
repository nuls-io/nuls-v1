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
package io.nuls.account.ledger.base.util;

import io.nuls.account.model.Account;
import io.nuls.account.service.AccountService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * author Facjas
 * date 2018/5/27.
 */
@Component
public class AccountLegerUtils {

    @Autowired
    private static AccountService accountService;

    public static boolean isLocalAccount(byte[] address) {
        List<Account> localAccountList = accountService.getAccountList().getData();
        if (localAccountList == null || localAccountList.size() == 0) {
            return false;
        }

        for (int i = 0; i < localAccountList.size(); i++) {
            if (Arrays.equals(localAccountList.get(i).getAddress().getBase58Bytes(), address)) {
                return true;
            }
        }
        return false;
    }

    public static List<byte[]> getRelatedAddresses(Transaction tx) {
        List<byte[]> result = new ArrayList<>();
        if (tx == null) {
            return result;
        }
        List<Account> localAccountList = accountService.getAccountList().getData();
        if (localAccountList == null || localAccountList.size() == 0) {
            return result;
        }
        List<byte[]> destAddresses = new ArrayList<>();
        for (Account account : localAccountList) {
            destAddresses.add(account.getAddress().getBase58Bytes());
        }

        return getRelatedAddresses(tx, destAddresses);
    }

    private static List<byte[]> getRelatedAddresses(Transaction tx, List<byte[]> addresses) {
        List<byte[]> result = new ArrayList<>();
        if (tx == null) {
            return result;
        }
        if (addresses == null || addresses.size() == 0) {
            return result;
        }
        List<byte[]> sourceAddresses = tx.getAllRelativeAddress();
        if (sourceAddresses == null || sourceAddresses.size() == 0) {
            return result;
        }

        for (byte[] tempSourceAddress : sourceAddresses) {
            for (byte[] tempDestAddress : addresses) {
                if (Arrays.equals(tempDestAddress, tempSourceAddress)) {
                    result.add(tempSourceAddress);
                    continue;
                }
            }
        }
        return result;
    }

    public static boolean isLocalTransaction(Transaction tx) {

        if (tx == null) {
            return false;
        }
        List<Account> localAccountList = accountService.getAccountList().getData();
        if (localAccountList == null || localAccountList.size() == 0) {
            return false;
        }
        List<byte[]> addresses = tx.getAllRelativeAddress();
        for (int j = 0; j < addresses.size(); j++) {
            if (AccountLegerUtils.isLocalAccount(addresses.get(j))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTxRelatedToAddress(Transaction tx, byte[] address){
        List<byte[]> sourceAddresses = tx.getAllRelativeAddress();
        for (byte[] tmpAddress : sourceAddresses){
            if(Arrays.equals(tmpAddress, address)){
                return true;
            }
        }
        return false;
    }
}
